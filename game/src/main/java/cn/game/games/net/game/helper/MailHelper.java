package cn.game.games.net.game.helper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.GmMail;
import cn.game.games.cache.entity.Mail;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.data.mapper.GmMailMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.gm.GmHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.mail.MailModule;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.MailConfig;
import cn.game.protocol.generated.manager.MailManager;
import cn.game.util.DateUtil;

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
	/***GM 邮件的id*/
	public static final int GM_MAIL_ID =18;


	/**
	 * 全服邮件
	 */
  	static List<GmMail> globalMailList = new CopyOnWriteArrayList<>();


	  public static void initLoadGlobalMail() {
		  DAO.execute(GmMailMapper.class,"selectGlobalMailList").onSuccess(rs -> {;
			  if (rs != null) {
				  List<GmMail> list = (List<GmMail>) rs;
				  if (!list.isEmpty()){
					  globalMailList.clear();
					  globalMailList.addAll(list);
				  }
			  }
          }).onFailure(rs -> {
              log.error("加载全服邮件失败", rs);
          });

      }

	public static void sendMail(long receiverId, int mailId, String sender, String title, String content, byte type, List<Goods> attachmentList,
			boolean notify) {
		Mail mail = Mail.valueOf(receiverId, mailId, sender, title, content, type, attachmentList);
		// 先推本服的,其他服直接存库，跨服的一般实时性要求低一些
		if (PlayerManager.getInstance().hasCache(receiverId)) { // 在线，或者服务器中还有玩家缓存
			Player player = PlayerManager.getInstance().getPlayer(receiverId);
			MailModule mailModule = player.getMailModule();
			mailModule.sendOnline(mail, notify);
		} else {
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
		// 先推本服的,其他服直接存库，跨服的一般实时性要求低一些
		if (PlayerManager.getInstance().hasCache(receiverId)) { // 在线，或者服务器中还有玩家缓存
			Player player = PlayerManager.getInstance().getPlayer(receiverId);
			MailModule mailModule = player.getMailModule();
			mailModule.sendOnline(mail, notify);
		} else {
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
		for (GmMail mail : globalMailList){
			if (mail.getId() == gmMail.getId()){
				return;
			}
		}
		globalMailList.add(gmMail);
		List<Goods> attachmentList = GmHelper.getAttachment(gmMail);
		PlayerManager.getInstance().getAllPlayer().values().forEach(player -> {
			try {
				if (canAddMail(player, gmMail)) {
					sendMail(player.getPlayerId(), GM_MAIL_ID, "系统管理员", gmMail.getTitle(), gmMail.getContext(), MailHelper.NOTICE, attachmentList, true);
					player.getMailModule().setGlobalMailId(gmMail.getId());
				}
			} catch (ParseException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});
	}

  public static void addGlobalMail(int mailId) {
    DAO.execute(GmMailMapper.class, MapperConstant.selectByPrimaryKey, mailId)
        .onSuccess(
            r -> {
              if (r != null) {
                GmMail gmMail = (GmMail) r;
                addGlobalMail(gmMail);
              }
            });
  }

	public static void onLoginAddGlobalMail(Player 	player) {
		globalMailList.forEach(gmMail -> {
			try {
				if (canAddMail(player,gmMail)){
					List<Goods> attachmentList = GmHelper.getAttachment(gmMail);
					sendMail(player.getPlayerId(), GM_MAIL_ID, "系统管理员", gmMail.getTitle(), gmMail.getContext(), MailHelper.NOTICE, attachmentList, true);
					player.getMailModule().setGlobalMailId(gmMail.getId());
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
		if (gmMail.getServerids() != null){
			String[] serveridStr = gmMail.getServerids().replace("[","").replace("]","").split(",");
            for (String serverid : serveridStr) {
				if (serverid.isEmpty()) continue;
                serverids.add(serverid);
            }
			if (!serverids.isEmpty() && !serverids.contains(player.getServerId())){
				return false;
	        }
		}
//		全服邮件 时间校验方式 0 登录时间 1 注册时间',
		if (gmMail.getTimeCheckType() == 0 && (player.getLastLoginTimer() < DateUtil.getLongDate(gmMail.getSendStartTimer()))
				|| player.getLastLoginTimer()  > DateUtil.getLongDate(gmMail.getSendEndTimer())) {
            return false;
        }
		if (gmMail.getTimeCheckType() == 1 && (player.getCreateTimer() < DateUtil.getLongDate(gmMail.getSendStartTimer()))
				|| player.getCreateTimer()  > DateUtil.getLongDate(gmMail.getSendEndTimer())) {
			return false;
		}

		if (player.getLevel() < gmMail.getMinLevel() || player.getLevel() > gmMail.getMaxLevel()) {
            return false;
        }
            return true;
    }

    public static boolean removeGlobalMail(String gmMailId) {
       return globalMailList.removeIf(gmMail -> gmMail.getId().equals(gmMailId));
    }

    public static List<GmMail> getGlobalMailList() {
        return globalMailList;
    }

    public static void clearGlobalMail() {
        globalMailList.clear();
    }


}
