package cn.game.games.net.game.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Mail;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.mail.MailModule;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.MailConfig;
import cn.game.protocol.generated.manager.MailManager;

/**
 * 邮件帮助类
 * 2020年10月15日 上午11:47:04
 * @author SYQ
 */
public class MailHelper {

	private static final Logger log = LoggerFactory.getLogger(MailHelper.class);

	/** 邮件类型， 公告邮件*/
	public static final byte NOTICE = 1;
	/** 邮件类型，系统自动发的邮件 */
	public static final byte SYSTEM = 2;
	/** 邮件类型，gm手动发的邮件 */
	public static final byte GM = 5;

	public static void sendMail(long receiverId, int mailId, String sender, String title, String content, byte type, List<Goods> attachmentList,
			boolean notify) {

		Mail mail = Mail.valueOf(receiverId, mailId, sender, title, content, type, attachmentList);
		if (PlayerManager.getInstance().hasCache(receiverId)) { // 在线，或者服务器中还有玩家缓存
			Player player = PlayerManager.getInstance().getPlayer(receiverId); 
			MailModule mailModule = player.getMailModule() ;
			mailModule.sendOnline(mail, notify);
		} else {
//			mail.insert() ; 
			DAO.insert(mail);
		}
	}

	public static void sendMail2(long receiverId, String sender, String title, String content, byte type,
			List<Entry<Integer, Integer>> rewards, boolean notify) {
		List<Goods> attachmentList = new ArrayList<Goods>();
		for (Entry<Integer, Integer> entry : rewards) {
			Goods goods = new Goods();
			goods.setId(entry.getKey());
			goods.setCount(entry.getValue());
			attachmentList.add(goods);
		}
		sendMail(receiverId, 0, sender, title, content, type, attachmentList, notify);
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
	public static void sendMailMultiLanguage(long receiverId, int senderId, int titleId, int contentId, byte type,
			List<Entry<Integer, Integer>> rewards) {
		sendMail2(receiverId, senderId + "", titleId + "", contentId + "", type, rewards, true);
	}

	public static void sendMail(long receiverId, int mailId, boolean notify) {

		sendMail(receiverId, mailId, null, notify);
	}

	/** 
	 * 发送邮件， 手动指定奖励内容，其余数据从配置表中读取。 
	 * @param receiverId
	 * @param mailId
	 * @param goods
	 */
	public static void sendMail(long receiverId, int mailId, List<Goods> goods, boolean notify) {

		Mail mail = Mail.valueOfMailId(receiverId, mailId, goods);
		if (PlayerManager.getInstance().hasCache(receiverId)) { // 在线，或者服务器中还有玩家缓存
			Player player = PlayerManager.getInstance().getPlayer(receiverId);
			MailModule mailModule = player.getMailModule();
			mailModule.sendOnline(mail, notify);
		} else {
//			mail.insert();
			DAO.insert(mail);
		}
	}

	/** 
	 * 
	 * 获取公告邮件id，最大的id，表示最新的公告。 
	 * @return
	 */
	public static int getNoticeMailId() {
		List<MailConfig> typeList = MailManager.instance().getTypeList(1);
		if (typeList == null) {
			return 0;
		}
		return typeList.get(typeList.size() - 1).ID;
	}

	public static boolean isNoticeMail(Mail mail) {
		MailConfig mailConfig = MailManager.instance().getNullable(mail.getMailId());
		return mailConfig != null && mailConfig.Type == 1;
	}
	
}
