package cn.game.games.net.game.gm.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.id.IdUtil;
import cn.game.core.net.process.Processor;
import cn.game.games.cache.entity.Mail;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerWriteGuard;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.mail.MailModule;
import cn.game.games.net.game.module.mail.MailType;
import cn.game.games.util.DAO;
import cn.game.util.DateUtil;
import cn.game.util.ObjUtil;
import cn.game.util.Rnd;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;

/**
 * 运维向在线/离线玩家检索、内存与DB一致性核对及安全补发邮件服务（支持无DB单测环境优雅降级）
 *
 * @author shuyongqiang
 */
public class OpsPlayerQueryService {

	private static final Logger log = LoggerFactory.getLogger(OpsPlayerQueryService.class);

	/**
	 * 综合检索在线内存与数据库中的玩家列表（支持按 playerId / 昵称 / accountId 搜索）
	 *
	 * @param keyword 搜索关键字（可为空，为空时优先列出在线角色及近期 DB 角色）
	 * @param limit 返回上限（1~100，默认 30）
	 * @return 检索结果 Map
	 */
	public Map<String, Object> searchPlayers(String keyword, int limit) {
		int effectiveLimit = Math.max(1, Math.min(100, limit > 0 ? limit : 30));
		String kw = keyword != null ? keyword.trim() : "";
		String kwLower = kw.toLowerCase(Locale.ROOT);

		Map<Long, Map<String, Object>> merged = new LinkedHashMap<>();
		for (Player p : PlayerManager.getInstance().getAllPlayer().values()) {
			if (p == null || p.getData() == null) {
				continue;
			}
			PlayerData d = p.getData();
			if (!kwLower.isEmpty() && !matchesPlayerData(d, kwLower)) {
				continue;
			}
			Map<String, Object> row = toSummaryMap(d, p.isOnline() && !p.isIslogouting(), "MEMORY");
			row.put("isLogouting", p.isIslogouting());
			row.put("moduleCount", p.getModules() != null ? p.getModules().size() : 0);
			merged.put(d.getPlayerId(), row);
		}

		boolean isProd = GmSecurityPolicy.currentRunMode().isProduction();
		boolean dbAvailable = isDbAvailable();
		String dbNote;
		if (!dbAvailable) {
			dbNote = isProd
					? "当前环境未加载数据库上下文，仅展示在线内存角色（生产环境仅支持精确检索离线角色）"
					: "当前环境未加载数据库上下文，仅展示在线内存角色";
		} else if (isProd) {
			dbNote = "生产环境仅支持按 PlayerId / UID / 精确昵称 / 精确账号检索离线玩家（禁止全表模糊扫描）";
		} else {
			dbNote = "ok（非生产全库轻量模糊检索）";
		}

		if (dbAvailable && merged.size() < effectiveLimit) {
			try {
				if (isProd) {
					// 生产环境：仅当给出非空精确查询关键字时走精确条件查询，绝不扫描全表
					if (!kw.isEmpty()) {
						Long parsedId = tryParsePositiveLong(kw);
						if (parsedId != null && !merged.containsKey(parsedId)) {
							PlayerData byId = DAO.executeSync(PlayerDataMapper.class, "selectSummaryByPrimaryKey", parsedId);
							if (byId != null) {
								merged.put(byId.getPlayerId(), toSummaryMap(byId, false, "DB"));
							}
						}
						if (merged.size() < effectiveLimit) {
							int remaining = effectiveLimit - merged.size();
							@SuppressWarnings("unchecked")
							List<PlayerData> exactRows = (List<PlayerData>) DAO.executeSync(
									PlayerDataMapper.class, "selectSummaryByExactMatch", parsedId, kw, kw, remaining);
							if (exactRows != null) {
								for (PlayerData row : exactRows) {
									if (row != null && !merged.containsKey(row.getPlayerId())) {
										merged.put(row.getPlayerId(), toSummaryMap(row, false, "DB"));
									}
								}
							}
						}
					}
				} else {
					// 非生产环境：支持全库轻量模糊检索（不拉取 modules BLOB）
					Long parsedId = !kw.isEmpty() ? tryParsePositiveLong(kw) : null;
					if (parsedId != null && !merged.containsKey(parsedId)) {
						PlayerData byId = DAO.executeSync(PlayerDataMapper.class, "selectSummaryByPrimaryKey", parsedId);
						if (byId != null) {
							merged.put(byId.getPlayerId(), toSummaryMap(byId, false, "DB"));
						}
					}
					if (merged.size() < effectiveLimit) {
						@SuppressWarnings("unchecked")
						List<PlayerData> fuzzyRows = (List<PlayerData>) DAO.executeSync(
								PlayerDataMapper.class, "searchSummaryFuzzy", kw, effectiveLimit);
						if (fuzzyRows != null) {
							for (PlayerData row : fuzzyRows) {
								if (row == null || merged.containsKey(row.getPlayerId())) {
									continue;
								}
								merged.put(row.getPlayerId(), toSummaryMap(row, false, "DB"));
								if (merged.size() >= effectiveLimit) {
									break;
								}
							}
						}
					}
				}
			} catch (Throwable t) {
				dbAvailable = false;
				dbNote = "数据库检索降级: " + t.getMessage();
				log.warn("运维检索离线玩家降级 keyword={}", kw, t);
			}
		}

		List<Map<String, Object>> list = new ArrayList<>(merged.values());
		list.sort(Comparator
				.comparing((Map<String, Object> m) -> !Boolean.TRUE.equals(m.get("online")))
				.thenComparingLong(m -> ((Number) m.getOrDefault("playerId", 0L)).longValue()));
		if (list.size() > effectiveLimit) {
			list = new ArrayList<>(list.subList(0, effectiveLimit));
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("dbAvailable", dbAvailable);
		result.put("dbNote", dbNote);
		result.put("total", list.size());
		result.put("players", list);
		return result;
	}

	/**
	 * 获取指定玩家只读详单（包含在线内存信息，及 DB 可用时的内存/DB 一致性核对）
	 *
	 * @param playerId 目标玩家 ID
	 * @return 玩家详情与一致性报告 Map
	 */
	public Map<String, Object> getPlayerDetail(long playerId) {
		if (playerId <= 0) {
			return Map.of("ok", false, "found", false, "message", "playerId 必须大于 0");
		}
		Player memPlayer = PlayerManager.getInstance().getPlayer(playerId);
		PlayerData memData = memPlayer != null ? memPlayer.getData() : null;
		PlayerData dbData = null;
		boolean dbAvailable = isDbAvailable();
		String dbError = "";

		if (dbAvailable) {
			try {
				dbData = DAO.executeSync(PlayerDataMapper.class, "selectSummaryByPrimaryKey", playerId);
			} catch (Throwable t) {
				dbAvailable = false;
				dbError = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
				log.warn("只读查询玩家DB存档失败 playerId={}", playerId, t);
			}
		}

		if (memData == null && dbData == null) {
			Map<String, Object> notFound = new LinkedHashMap<>();
			notFound.put("ok", false);
			notFound.put("found", false);
			notFound.put("playerId", playerId);
			notFound.put("dbAvailable", dbAvailable);
			notFound.put("message", dbAvailable ? ("未找到玩家: " + playerId) : ("玩家不在线且数据库未就绪: " + playerId));
			return notFound;
		}

		Map<String, Object> detail = new LinkedHashMap<>();
		detail.put("ok", true);
		detail.put("found", true);
		detail.put("playerId", playerId);
		detail.put("online", memPlayer != null && memPlayer.isOnline() && !memPlayer.isIslogouting());
		detail.put("inMemory", memPlayer != null);
		detail.put("inDb", dbData != null);
		detail.put("dbAvailable", dbAvailable);
		if (!dbError.isEmpty()) {
			detail.put("dbError", dbError);
		}
		detail.put("memorySummary", memData != null ? toSummaryMap(memData, memPlayer.isOnline() && !memPlayer.isIslogouting(), "MEMORY") : null);
		detail.put("dbSummary", dbData != null ? toSummaryMap(dbData, false, "DB") : null);

		if (memPlayer != null && memPlayer.getModules() != null) {
			List<String> modNames = new ArrayList<>(memPlayer.getModules().keySet());
			Collections.sort(modNames);
			detail.put("moduleNames", modNames);
		} else {
			detail.put("moduleNames", Collections.emptyList());
		}

		// 在线内存 vs DB 关键标量一致性校验（辅助排查未落盘/回档异常）
		List<Map<String, Object>> driftList = new ArrayList<>();
		if (memData != null && dbData != null) {
			compareScalar(driftList, "level", memData.getLevel(), dbData.getLevel());
			compareScalar(driftList, "vipLevel", memData.getVipLevel(), dbData.getVipLevel());
			compareScalar(driftList, "name", memData.getName(), dbData.getName());
			compareScalar(driftList, "unionId", memData.getUnionId(), dbData.getUnionId());
			compareScalar(driftList, "offlineTime", memData.getOfflineTime(), dbData.getOfflineTime());
		}
		detail.put("consistencyDrifts", driftList);
		detail.put("consistent", driftList.isEmpty());
		return detail;
	}

	/**
	 * 面向在线/离线玩家发送受控运维补偿邮件（需 OPERATOR 及以上权限）
	 *
	 * @param playerId 目标玩家 ID
	 * @param title 邮件标题
	 * @param content 邮件正文
	 * @param itemId 附件物品/资源 ID（<=0 表示纯文本邮件）
	 * @param count 附件数量
	 * @return 发送结果 Map
	 */
	public Map<String, Object> sendOpsMail(long playerId, String title, String content, int itemId, int count) {
		if (playerId <= 0) {
			return Map.of("ok", false, "message", "playerId 必须大于 0");
		}
		String safeTitle = (title != null && !title.isBlank()) ? title.trim() : "系统补偿邮件";
		String safeContent = content != null ? content : "";
		List<Goods> attachmentList = null;
		if (itemId > 0) {
			if (count <= 0) {
				return Map.of("ok", false, "message", "指定了附件 itemId 时数量 count 必须大于 0");
			}
			if (!new ConfigHealthService().existsGoodsId(itemId)) {
				return Map.of("ok", false, "message", "无效的附件物品/资源 ID: " + itemId);
			}
			attachmentList = new ArrayList<>();
			attachmentList.add(new Goods(itemId, count));
		}

		boolean dbAvailable = isDbAvailable();
		Player onlinePlayer = PlayerManager.getInstance().getPlayer(playerId);
		if (onlinePlayer != null && onlinePlayer.getMailModule() != null) {
			long mailUid;
			try {
				mailUid = IdUtil.getId();
			} catch (Throwable t) {
				log.debug("IdUtil 未初始化，运维补偿邮件退回本地临时 UID", t);
				mailUid = DateUtil.currentTimeMillis() * 1000L + Rnd.nextInt(1000);
			}
			final long finalMailUid = mailUid;
			final List<Goods> finalAttachments = attachmentList;

			try {
				Processor processor = dbAvailable ? ServerContext.getInstance().getProcessor() : null;
				Future<Object> mailFuture = sendOnlineViaPlayerQueue(
						onlinePlayer, processor, finalMailUid, safeTitle, safeContent, finalAttachments);
				if (dbAvailable && mailFuture != null) {
					mailFuture.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
				}
				return Map.of("ok", true, "channel", "ONLINE_MAIL_MODULE", "playerId", playerId,
						"mailUid", finalMailUid, "title", safeTitle);
			} catch (Throwable t) {
				log.error("在线玩家排队写入运维补偿邮件失败 playerId={}", playerId, t);
				return Map.of("ok", false, "message", "在线补偿邮件落库或排队执行失败: " + t.getMessage());
			}
		}

		if (!dbAvailable) {
			return Map.of("ok", false, "message", "目标玩家不在线且数据库/远程集群未就绪");
		}
		try {
			PlayerData exists = DAO.executeSync(PlayerDataMapper.class, "selectSummaryByPrimaryKey", playerId);
			if (exists == null) {
				return Map.of("ok", false, "message", "目标离线玩家不存在: " + playerId);
			}
			Future<Object> remoteFuture = MailHelper.sendMail(playerId, 0, null, "系统管理员", safeTitle, safeContent,
					MailType.System.getValue(), attachmentList, true);
			if (remoteFuture != null) {
				remoteFuture.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
			}
			return Map.of("ok", true, "channel", "REMOTE_OFFLINE_MAIL", "playerId", playerId, "title", safeTitle);
		} catch (Throwable t) {
			log.warn("发送运维离线补偿邮件失败 playerId={}", playerId, t);
			return Map.of("ok", false, "message", "发送离线补偿邮件失败: " + t.getMessage());
		}
	}

	private Future<Object> sendOnlineViaPlayerQueue(Player onlinePlayer, Processor processor, long mailUid,
			String safeTitle, String safeContent, List<Goods> finalAttachments) throws Exception {
		CompletableFuture<Future<Object>> dispatchResult = new CompletableFuture<>();
		Runnable writeTask = () -> {
			try {
				PlayerWriteGuard.run(onlinePlayer, () -> {
					MailModule mailModule = onlinePlayer.getMailModule();
					Mail mail = new Mail();
					ObjUtil.setDefaultValue(mail);
					mail.setId(mailUid);
					mail.setPlayerId(onlinePlayer.getPlayerId());
					mail.setMailId(0);
					mail.setTitle(safeTitle);
					mail.setContent(safeContent);
					mail.setSender("系统管理员");
					mail.setType((byte) MailType.System.getValue());
					mail.setSee(false);
					mail.setReceive(false);
					mail.setIsDeleted(false);
					mail.setAttachmentList(finalAttachments);
					mail.setCreateTime(DateUtil.currentTimeSeconds());
					boolean notify = onlinePlayer.getGameClient() != null && onlinePlayer.getGameClient().isActive();
					dispatchResult.complete(mailModule.sendOnline(mail, notify));
				});
			} catch (Throwable t) {
				dispatchResult.completeExceptionally(t);
			}
		};
		if (processor != null) {
			processor.process(onlinePlayer.getPlayerId(), writeTask);
			return dispatchResult.get(5, TimeUnit.SECONDS);
		}
		writeTask.run();
		return dispatchResult.get(5, TimeUnit.SECONDS);
	}

	private boolean isDbAvailable() {
		try {
			return SpringContextLoader.getContext() != null;
		} catch (Throwable t) {
			log.warn("检查Spring数据库上下文状态失败", t);
			return false;
		}
	}

	private Long tryParsePositiveLong(String s) {
		if (s == null || s.isEmpty() || s.length() > 19) {
			return null;
		}
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c < '0' || c > '9') {
				return null;
			}
		}
		try {
			long v = Long.parseLong(s);
			return v > 0 ? v : null;
		} catch (NumberFormatException e) {
			log.debug("解析正整数 PlayerId 失败: {}", s, e);
			return null;
		}
	}

	private boolean matchesPlayerData(PlayerData d, String kwLower) {
		if (String.valueOf(d.getPlayerId()).contains(kwLower)) {
			return true;
		}
		if (d.getName() != null && d.getName().toLowerCase(Locale.ROOT).contains(kwLower)) {
			return true;
		}
		if (d.getAccountId() != null && d.getAccountId().toLowerCase(Locale.ROOT).contains(kwLower)) {
			return true;
		}
		return String.valueOf(d.getUid()).contains(kwLower);
	}

	private Map<String, Object> toSummaryMap(PlayerData d, boolean online, String source) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("playerId", d.getPlayerId());
		map.put("uid", d.getUid());
		map.put("accountId", d.getAccountId() != null ? d.getAccountId() : "");
		map.put("name", d.getName() != null ? d.getName() : "");
		map.put("serverId", d.getServerId() != null ? d.getServerId() : "");
		map.put("level", d.getLevel());
		map.put("vipLevel", d.getVipLevel());
		map.put("combat", d.getCombat());
		map.put("unionId", d.getUnionId());
		map.put("unionName", d.getUnionName() != null ? d.getUnionName() : "");
		map.put("isGm", d.getIsGm());
		map.put("createDate", d.getCreateDate() != null ? d.getCreateDate() : "");
		map.put("offlineTime", d.getOfflineTime());
		map.put("online", online);
		map.put("source", source);
		return map;
	}

	private void compareScalar(List<Map<String, Object>> list, String field, Object memVal, Object dbVal) {
		String a = memVal != null ? String.valueOf(memVal) : "";
		String b = dbVal != null ? String.valueOf(dbVal) : "";
		if (!a.equals(b)) {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("field", field);
			item.put("memory", memVal);
			item.put("db", dbVal);
			list.add(item);
		}
	}
}
