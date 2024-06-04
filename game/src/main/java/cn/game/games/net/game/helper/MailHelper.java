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
import cn.game.protocol.generated.config.MailConfig;
import cn.game.protocol.generated.manager.MailManager;

/**
 * @Description 邮件帮助类
 * @date 2020年10月15日 上午11:47:04
 * @author SYQ
 */
public class MailHelper {

	private static final Logger log = LoggerFactory.getLogger(MailHelper.class);

	/** 邮件类型，系统自动发的邮件，多语言的，不直接发文本内容，发文本id */
	public static final byte SYSTEM = 1;
	/** 邮件类型， 手动发的，一般为英文版，直接发内容 */
	public static final byte GM = 2;

	public static void sendMail(long receiverId, int mailId, String sender, String title, String content, byte type, List<Goods> attachmentList) {

		Mail mail = Mail.valueOf(receiverId, sender, title, content, type, attachmentList);
		if (PlayerManager.getInstance().hasCache(receiverId)) { // 在线，或者服务器中还有玩家缓存
			Player player = PlayerManager.getInstance().getPlayer(receiverId); 
			MailModule mailModule = player.getMailModule() ;
			mailModule.sendOnline(mail);
		} else {
			mail.insert() ; 
		}
	}

	public static void sendMail2(long receiverId, int mailId, String sender, String title, String content, byte type,
			List<Entry<Integer, Integer>> rewards) {
		List<Goods> attachmentList = new ArrayList<Goods>();
		for (Entry<Integer, Integer> entry : rewards) {
			Goods goods = new Goods();
			goods.setId(entry.getKey());
			goods.setCount(entry.getValue());
			attachmentList.add(goods);
		}
		sendMail(receiverId, mailId, sender, title, content, type, attachmentList);
	}
	/**
	 * @Description 发送多语言版的邮件
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
	public static void sendMailMultiLanguage(long receiverId, int mailId, int senderId, int titleId, int contentId, byte type,
			List<Entry<Integer, Integer>> rewards) {
		sendMail2(receiverId, mailId, senderId + "", titleId + "", contentId + "", type, rewards);
	}

	/** 
	 * 
	 * 获取公告邮件id，最大的id，表示最新的公告。 
	 * @return
	 */
	public static int getNoticeMailId() {
		List<MailConfig> typeList = MailManager.instance().getTypeList(1);
		return typeList.get(typeList.size() - 1).ID;
	}

	public static boolean isNoticeMail(Mail mail) {
		MailConfig mailConfig = MailManager.instance().getNullable(mail.getMailId());
		return mailConfig != null && mailConfig.Type == 1;
	}
	
}
