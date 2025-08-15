package cn.game.games.net.game.helper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.IdUtil;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.module.mail.MailRankInfo;
import cn.game.games.net.game.module.rank.RankEntry;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.util.GameUtil;
import cn.game.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.cache.id.DistributedObjectType;
import cn.game.games.cache.entity.GmMail;
import cn.game.games.cache.entity.Mail;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.data.mapper.GmMailMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.gm.GmHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.MailConfig;
import cn.game.protocol.generated.manager.MailManager;
import cn.game.util.DateUtil;
import cn.game.util.ServerType;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;

/**
 * 邮件帮助类
 * 2020年10月15日 上午11:47:04
 * @author SYQ
 */
public class MailHelper {

	private static final Logger log = LoggerFactory.getLogger(MailHelper.class);

	/** 全服邮件 */
	static List<GmMail> globalMailList = new CopyOnWriteArrayList<>();

	/** 
	 * 只提供邮件id的发送方法，相关数据从配置表读取，其他默认
	 * @param receiverId
	 * @param mailId
	 * @param notify
	 */
	public static Future<@Nullable Object> sendMail(long receiverId, int mailId, boolean notify) {
		if (mailId == 0) {
			return Future.failedFuture("mailId cannot be 0");
		}
		return sendMail(receiverId, mailId, null, null, null, null, 0, null, notify);
	}
	

	/** 
	 * 只提供邮件id的发送方法，相关数据从配置表读取，其他默认。
	 * 可能需要外部参数来格式化邮件内容。
	 * @param receiverId
	 * @param mailId
	 * @param contentArguments 邮件内容参数，可能需要外部参数来格式化邮件内容。
	 * @param notify
	 */
	public static Future<@Nullable Object> sendMail(long receiverId, int mailId, boolean notify, Object... contentArguments) {
		if (mailId == 0) {
			return Future.failedFuture("mailId cannot be 0");
		}
		return sendMail(receiverId, mailId, contentArguments, null, null, null, 0, null, notify);
	}
	/** 
	 * 发送邮件， 手动指定奖励内容，其余数据从配置表中读取。 
	 * @param receiverId
	 * @param mailId
	 * @param goods
	 */
	public static Future<Object> sendMail(long receiverId, int mailId, List<Goods> goods, boolean notify) {
		if (mailId == 0) {
			return Future.failedFuture("mailId cannot be 0");
		}
		return sendMail(receiverId, mailId, null, null, null, null, 0, goods, notify);
	}
	
	/** 
	 * 通用的发送邮件方法，所有的邮件都通过这个方法发送。需要指定全部参数
	 * 尽量不要直接调用这个方法，使用简化参数的方法。 
	 * @param receiverId
	 * @param mailId
	 * @param contentArguments
	 * @param sender
	 * @param title
	 * @param content
	 * @param type
	 * @param attachmentList
	 * @param notify
	 * @return
	 */
	public static Future<@Nullable Object> sendMail(long receiverId, int mailId, Object[] contentArguments, String sender, String title,
			String content, int type, List<Goods> attachmentList, boolean notify) {
		GameServerInterface remoteInterfaceProxy = ServerHelper.getRemoteInterfaceProxy(ServerType.Game, GameServerInterface.class,
				DistributedObjectType.PLAYER, receiverId);
		return remoteInterfaceProxy.addMail(receiverId, mailId, contentArguments, sender, title, content, type, attachmentList, notify)
				.onFailure(e -> {
					log.error("发送邮件失败，receiverId = {}, mailId = {}, sender = {}, title = {}, content = {}, type = {}, attachmentList = {}",
							receiverId, mailId, sender, title, content, type, attachmentList, e);
				});
	}

	/** 
	 * 玩家给玩家发邮件
	 * @param receiverId
	 * @param sender
	 * @param title
	 * @param content
	 * @return
	 */
	public static Future<Object> sendMailFromPlayer(long receiverId, String sender, String title, String content) {
		return sendMail(receiverId, 0, null, sender, title, content, 3, null, true);
	}

	@Deprecated
	public static void sendMail2(long receiverId, String sender, String title, String content, byte type,
			List<Entry<Integer, Integer>> rewards, boolean notify) {
		List<Goods> attachmentList = new ArrayList<Goods>();
		for (Entry<Integer, Integer> entry : rewards) {
			Goods goods = new Goods();
			goods.setId(entry.getKey());
			goods.setCount(entry.getValue());
			attachmentList.add(goods);
		}
		sendMail(receiverId, 0, null, sender, title, content, type, attachmentList, notify);
	}

	/**
	 * 发送多语言版的邮件
	 * @param receiverId
	 * @param senderId
	 *            发件人文本id
	 * @param titleId
	 *            标题文本id
	 * @param contentId
	 *            内容文本id
	 * @param type
	 *            1，多语言版，2 普通版
	 * @param rewards
	 */
	@Deprecated
	public static void sendMailMultiLanguage(long receiverId, int senderId, int titleId, int contentId, byte type,
			List<Entry<Integer, Integer>> rewards) {
		sendMail2(receiverId, senderId + "", titleId + "", contentId + "", type, rewards, true);
	}

	/** 
	 * 
	 * 获取公告邮件id，最大的id，表示最新的公告。 
	 * @return
	 */
	public static int getNoticeMailId() {
		List<MailConfig> typeList = MailManager.instance().getTypeList(1);
		if (typeList.isEmpty()) {
			return 0;
		}
		return typeList.get(typeList.size() - 1).ID;
	}

	/** 
	 * 是否是公告类型的邮件，通过配置表的邮件类型区分。 
	 * @param mail
	 * @return
	 */
	public static boolean isNoticeMail(Mail mail) {
		MailConfig mailConfig = MailManager.instance().getNullable(mail.getMailId());
		return mailConfig != null && mailConfig.Type == 1;
	}

	public static void addGlobalMail(GmMail gmMail) {
		for (GmMail mail : globalMailList) {
			if (mail.getId() == gmMail.getId()) {
				return;
			}
		}
		globalMailList.add(gmMail);
		globalMailList.sort(Comparator.comparingLong(GmMail::getId));
		List<Goods> attachmentList = GmHelper.getAttachment(gmMail);
		PlayerManager.getInstance().getAllPlayer().values().forEach(player -> {
			try {
				if (canAddMail(player, gmMail)) {
					sendMail(player.getPlayerId(), 0, null, "系统管理员", gmMail.getTitle(), gmMail.getContext(),gmMail.getMailopttype(),
							attachmentList, true);
					player.getMailModule().setGlobalMailId(gmMail.getId());
					log.info(String.format("addGlobalMail playerId = %s, mailId = %s", player.getPlayerId(), gmMail.getId()));
				}
			} catch (ParseException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});
	}

	public static void addGlobalMail(long mailId) {
		DAO.execute(GmMailMapper.class, MapperConstant.selectByPrimaryKey, mailId).onSuccess(r -> {
			if (r != null) {
				GmMail gmMail = (GmMail) r;
				addGlobalMail(gmMail);
			}
		});
	}

	public static void onLoginAddGlobalMail(Player player) {
		globalMailList.forEach(gmMail -> {
			try {
				if (canAddMail(player, gmMail)) {
					List<Goods> attachmentList = GmHelper.getAttachment(gmMail);
					sendMail(player.getPlayerId(), 0, null, "系统管理员", gmMail.getTitle(), gmMail.getContext(),  gmMail.getMailopttype(),
							attachmentList, true);
					player.getMailModule().setGlobalMailId(gmMail.getId());
					log.info(String.format("onLoginAddGlobalMail playerId = %s, mailId = %s", player.getPlayerId(), gmMail.getId()));
				}
			} catch (ParseException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});
	}

	private static boolean canAddMail(Player player, GmMail gmMail) throws ParseException {
		long now = System.currentTimeMillis();
		if (now < DateUtil.getLongDate(gmMail.getSendStartTimer()) || now > DateUtil.getLongDate(gmMail.getSendEndTimer())) {
			return false;
		}
		if (player.getMailModule().getGlobalMailId() >= gmMail.getId()) {
			return false;
		}

		List<String> serverids = new ArrayList<>();
		if (gmMail.getServerids() != null) {
			String[] serveridStr = gmMail.getServerids().replace("[", "").replace("]", "").split(",");
			for (String serverid : serveridStr) {
				if (serverid.isEmpty())
					continue;
				serverids.add(serverid);
			}
			if (!serverids.isEmpty() && !serverids.contains(player.getServerId())) {
				return false;
			}
		}
//		全服邮件 时间校验方式 0 登录时间 1 注册时间',
		if (gmMail.getTimeCheckType() == 0 && (player.getLastLoginTimer() < DateUtil.getLongDate(gmMail.getSendStartTimer()))
				|| player.getLastLoginTimer() > DateUtil.getLongDate(gmMail.getSendEndTimer())) {
			return false;
		}
		if (gmMail.getTimeCheckType() == 1 && (player.getCreateTimer() < DateUtil.getLongDate(gmMail.getSendStartTimer()))
				|| player.getCreateTimer() > DateUtil.getLongDate(gmMail.getSendEndTimer())) {
			return false;
		}

		if (player.getLevel() < gmMail.getMinLevel() || player.getLevel() > gmMail.getMaxLevel()) {
			return false;
		}
		return true;
	}

	public static void initLoadGlobalMail() {
		DAO.execute(GmMailMapper.class, "selectGlobalMailList").onSuccess(rs -> {
			;
			if (rs != null) {
				List<GmMail> list = (List<GmMail>) rs;
				if (!list.isEmpty()) {
					globalMailList.clear();
					globalMailList.addAll(list);
					globalMailList.sort(Comparator.comparingLong(GmMail::getId));
				}
			}
		}).onFailure(rs -> {
			log.error("加载全服邮件失败", rs);
		});

	}

	public static boolean removeGlobalMail(Integer gmMailId) {
		return globalMailList.removeIf(gmMail -> gmMail.getId() == gmMailId);
	}

	public static List<GmMail> getGlobalMailList() {
		return globalMailList;
	}

	public static void clearGlobalMail() {
		globalMailList.clear();
	}

	/**
	 * 添加全服邮件  用于功能添加全服邮件
	 * 目的是消息通知  全服通知
	 * 特点为 玩家不可见 无各类信息 无等级限制  无需审批   content 根据功能自定义 解析
	 */
	public static void addGlobalGmMail( String content,String serverIds,
	                          long sendStartTime, long sendEndTime,byte mailType ) {
		if(mailType<=1)
		{
			throw  new RuntimeException("邮件类型错误");
		}
		GmMail gmMail = new GmMail();
		gmMail.setId(IdUtil.getId());
		gmMail.setTitle("");
		gmMail.setContext(content);
		gmMail.setCreateTime(new Date());
		gmMail.setSendName("");
		// 全服邮件
		gmMail.setServerids(serverIds);
		gmMail.setSendStartTimer(DateUtil.getTimeByPattern(new Date(sendStartTime ), DateUtil.pattern_en));
		gmMail.setSendEndTimer(DateUtil.getTimeByPattern(new Date(sendEndTime ), DateUtil.pattern_en));
		gmMail.setMinLevel(0);
		gmMail.setMaxLevel(999);
		gmMail.setOptFlag((byte) 1);
		gmMail.setTimeCheckType((byte) 2);
		gmMail.setMailopttype(mailType);
		gmMail.insert().onSuccess(r -> {
			addGlobalMail(gmMail);
			// 通知其他节点 添加新的全服邮件
			VxHolder.broadcastRemoteServer(ServerType.Game, ServerMsg.NotifyAddGlobalGmMailRequest_7d000060.newBuilder().setAddGmMailId(gmMail.getId()).build());
		});
	}

}
