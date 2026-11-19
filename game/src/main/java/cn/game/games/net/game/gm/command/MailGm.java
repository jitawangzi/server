package cn.game.games.net.game.gm.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.game.core.id.IdUtil;
import cn.game.games.cache.entity.Mail;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.gm.GmAction;
import cn.game.games.net.game.gm.GmGroup;
import cn.game.games.net.game.gm.GmParam;
import cn.game.games.net.game.gm.GmScope;
import cn.game.games.net.game.gm.GmSecurityTier;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.mail.MailModule;
import cn.game.games.net.game.module.mail.MailType;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;
import cn.game.util.Rnd;

/**
 * 邮件系统调试GM指令分组
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "mail", description = "邮件系统调试指令")
public class MailGm extends AbstractGm {

	private static final Logger log = LoggerFactory.getLogger(MailGm.class);

	/**
	 * 给当前玩家发送测试邮件（支持配置模板或自定义标题内容与附件）
	 *
	 * @param player 目标在线玩家
	 * @param mailId 邮件配置表ID（0为自定义邮件）
	 * @param title 邮件标题
	 * @param content 邮件内容
	 * @param itemId 附加道具或资源ID（0表示无附件）
	 * @param count 附加道具数量
	 * @return 执行结果描述
	 */
	@GmAction(name = "send", description = "给当前玩家发送测试邮件", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String send(Player player,
			@GmParam(description = "邮件配置表ID(0为自定义邮件)", required = false, defaultValue = "0") int mailId,
			@GmParam(description = "邮件标题", required = false, defaultValue = "测试邮件") String title,
			@GmParam(description = "邮件内容", required = false, defaultValue = "这是一封自动化测试邮件") String content,
			@GmParam(description = "附加道具或资源ID(0表示无附件)", required = false, defaultValue = "0") int itemId,
			@GmParam(description = "附加道具数量", required = false, defaultValue = "0") int count) {
		if (player == null) {
			return "目标玩家为空";
		}
		String safeTitle = title != null ? title : "GM邮件";
		String safeContent = content != null ? content : "";
		List<Goods> attachmentList = null;
		if (itemId > 0 && count > 0) {
			attachmentList = new ArrayList<>();
			attachmentList.add(new Goods(itemId, count));
		}
		MailModule mailModule = player.getMailModule();
		if (mailModule != null) {
			Mail mail;
			try {
				mail = Mail.valueOf(player.getPlayerId(), mailId, null, "系统管理员", safeTitle, safeContent,
						MailType.System.getValue(), attachmentList);
			} catch (Throwable t) {
				log.warn("调用Mail.valueOf失败，降级构造自定义测试邮件: playerId={}, mailId={}", player.getPlayerId(), mailId, t);
				mail = new Mail();
				long id;
				try {
					id = IdUtil.getId();
				} catch (Throwable idEx) {
					id = DateUtil.currentTimeMillis() * 1000L + Rnd.nextInt(1000);
				}
				mail.setId(id);
				mail.setPlayerId(player.getPlayerId());
				mail.setMailId(mailId);
				mail.setTitle(safeTitle);
				mail.setContent(safeContent);
				mail.setSender("系统管理员");
				mail.setType((byte) MailType.System.getValue());
				mail.setAttachmentList(attachmentList);
				mail.setCreateTime(DateUtil.currentTimeSeconds());
			}
			boolean notify = player.getGameClient() != null;
			mailModule.sendOnline(mail, notify);
			return "成功向玩家发送邮件: id=" + mail.getId() + ", title=" + safeTitle;
		} else {
			MailHelper.sendMail(player.getPlayerId(), mailId, null, "系统管理员", safeTitle, safeContent,
					MailType.System.getValue(), attachmentList, true);
			return "已通过远程接口下发邮件: title=" + safeTitle;
		}
	}

	/**
	 * 领取指定或全部邮件附件奖励
	 *
	 * @param player 目标在线玩家
	 * @param mailId 邮件唯一ID（0表示全部领取）
	 * @return 执行结果描述
	 */
	@GmAction(name = "receive", description = "领取当前玩家邮件附件奖励(0表示一键全部领取)", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String receive(Player player,
			@GmParam(description = "邮件唯一ID(0为一键全部领取)", required = false, defaultValue = "0") long mailId) {
		MailModule mailModule = player != null ? player.getMailModule() : null;
		if (mailModule == null) {
			return "邮件模块未初始化";
		}
		if (mailId == 0) {
			List<RewardInfo> rewards = mailModule.receiveBatch();
			return "已一键领取全部邮件奖励，共获得 " + (rewards != null ? rewards.size() : 0) + " 种奖励项";
		} else {
			List<RewardInfo> rewards = mailModule.receive(mailId);
			return "已领取邮件 " + mailId + " 奖励: " + (rewards != null ? rewards.size() : 0) + " 种奖励项";
		}
	}

	/**
	 * 标记指定或全部邮件为已读
	 *
	 * @param player 目标在线玩家
	 * @param mailId 邮件唯一ID（0表示全部标为已读）
	 * @return 执行结果描述
	 */
	@GmAction(name = "read", description = "将邮件标为已读(0表示一键全部标为已读)", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String read(Player player,
			@GmParam(description = "邮件唯一ID(0为一键全部标为已读)", required = false, defaultValue = "0") long mailId) {
		MailModule mailModule = player != null ? player.getMailModule() : null;
		if (mailModule == null) {
			return "邮件模块未初始化";
		}
		if (mailId == 0) {
			mailModule.seeBatch();
			return "已将所有未读邮件标为已读";
		} else {
			Mail mail = mailModule.see(mailId);
			return mail != null ? ("邮件 " + mailId + " 已标为已读") : ("邮件不存在: " + mailId);
		}
	}

	/**
	 * 删除指定邮件
	 *
	 * @param player 目标在线玩家
	 * @param mailId 邮件唯一ID
	 * @return 执行结果描述
	 */
	@GmAction(name = "delete", description = "删除指定邮件", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String delete(Player player,
			@GmParam(description = "邮件唯一ID") long mailId) {
		MailModule mailModule = player != null ? player.getMailModule() : null;
		if (mailModule == null) {
			return "邮件模块未初始化";
		}
		boolean deleted = mailModule.delete(mailId);
		return deleted ? ("已删除邮件: " + mailId) : ("删除失败或邮件不存在/未读: " + mailId);
	}

	/**
	 * 清空当前玩家全部邮件
	 *
	 * @param player 目标在线玩家
	 * @return 执行结果描述
	 */
	@GmAction(name = "clear", description = "清空当前玩家的所有邮件", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public String clear(Player player) {
		MailModule mailModule = player != null ? player.getMailModule() : null;
		if (mailModule != null) {
			mailModule.deleteAll();
			return "已清空当前玩家的所有邮件";
		}
		return "邮件模块未初始化";
	}

	/**
	 * 查询当前玩家邮件列表概况
	 *
	 * @param player 目标在线玩家
	 * @return 邮件概况结果
	 */
	@GmAction(name = "list", description = "查询当前玩家邮件列表概况", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public Map<String, Object> list(Player player) {
		Map<String, Object> res = new LinkedHashMap<>();
		MailModule mailModule = player != null ? player.getMailModule() : null;
		if (mailModule == null) {
			res.put("error", "邮件模块未初始化");
			return res;
		}
		Collection<Mail> mails = mailModule.list();
		if (mails == null) {
			res.put("totalCount", 0);
			res.put("unreadCount", 0L);
			res.put("unclaimedCount", 0L);
			res.put("mails", List.of());
			return res;
		}
		long unreadCount = 0L;
		long unclaimedCount = 0L;
		List<Map<String, Object>> mailRows = new ArrayList<>();
		for (Mail m : mails) {
			if (m == null) {
				continue;
			}
			boolean seen = Boolean.TRUE.equals(m.getSee());
			boolean received = Boolean.TRUE.equals(m.getReceive());
			if (!seen) {
				unreadCount++;
			}
			if (!received && m.getAttachmentList() != null && !m.getAttachmentList().isEmpty()) {
				unclaimedCount++;
			}
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("id", m.getId());
			item.put("mailId", m.getMailId());
			item.put("title", m.getTitle() != null ? m.getTitle() : "");
			item.put("see", seen);
			item.put("receive", received);
			item.put("createTime", m.getCreateTime());
			mailRows.add(item);
		}
		res.put("totalCount", mailRows.size());
		res.put("unreadCount", unreadCount);
		res.put("unclaimedCount", unclaimedCount);
		res.put("mails", mailRows);
		return res;
	}
}