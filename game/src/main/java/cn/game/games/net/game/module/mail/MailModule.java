package cn.game.games.net.game.module.mail;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.entity.Mail;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.MailMapper;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.util.DAO;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.MailConfig;
import cn.game.protocol.generated.manager.MailManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.MailMsg.MailInfo;
import cn.game.protocol.protobuf.MailMsg.MailNewPush_12010001;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

public class MailModule extends BasePlayerModule  {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.LoginSuccess };

	@JsonIgnore
	private Map<Long, Mail>	mails = new HashMap<>();

	/** 公告邮件比较特殊，保存一个不删除。设置删除标记位，为了服务器一次更新只发一次公告邮件  */
	@JsonIgnore
	private Mail notice;
	/**
	 * 领取过的全服邮件版本号
	 */
	private long globalMailId;

	public void sendOnline(Mail mail, boolean notify) {

		addMail(mail);
		if (notify) {
			MailInfo mailInfo = PbBuilder.buildMailInfo(mail);
			player.getGameClient().sendProtocol(MailNewPush_12010001.newBuilder().setMail(mailInfo).build());
		}
	}

	private void addMail(Mail mail) {

		if (MailHelper.isNoticeMail(mail)) {
			if (notice != null) {
				delete(notice.getId());
				notice = mail;
			} else {
				notice = mail;
			}
		}
		mails.put(mail.getId(), mail);
		DAO.insert(mail);
//		mail.insert() ; 
	}

	public Mail get(long id) {

		return this.mails.get(id);
	}

	public boolean delete(long id) {
		Mail remove = this.mails.remove(id); 
		if (remove!= null) {
			if (remove == notice) {
				remove.setIsDeleted(true);
			} else {
				remove.delete();
				DAO.delete(remove);
			}
		}
		return false;
	}

	public Collection<Mail> list() {

		return this.mails.values();
	}

	public boolean hasNoRead() {

		for (Mail mail : this.mails.values())
		{
			if (!mail.getSee())
			{
				return true ; 
			}
		}
		return false;
	}

	public Mail see(long id) {

		Mail mail = get(id);
		if (mail == null) {
			return null;
		}
		if (!mail.getSee()) {
			mail.setSee(true);
			mail.setSeeTime((int) (System.currentTimeMillis() / 1000));
//			mail.update(); 
			DAO.update(mail);
		}
		return mail ; 
	}

	public void seeBatch() {
		Collection<Mail> values = this.mails.values();
		for (Mail mail : values) {
			if (!mail.getSee()) {
				see(mail.getId());
			}
		}
	}

	public List<RewardInfo> receive(long id) {
		List<RewardInfo> list = new ArrayList<>();

		Mail mail = get(id);
		if (mail != null && !mail.getReceive()) {
			List<Goods> attachmentList = mail.getAttachmentList();
			if (attachmentList != null && !attachmentList.isEmpty() || mail.getMailId() > 0) {

				mail.setReceive(true);
				mail.setReceiveTime(DateUtil.currentTimeSeconds());

				if (!mail.getSee()) {
					mail.setSee(true);
					mail.setSeeTime(DateUtil.currentTimeSeconds());
				}

				/*for (Goods goods : attachmentList) {
					List<RewardItem> addResources = PlayerHelper.addResources(player, goods.getId(), goods.getCount(), false);
					list.addAll(addResources);
				}*/
				if (attachmentList != null && !attachmentList.isEmpty()) {

					List<AbstractMap.Entry<Integer, Integer>> rewards = new ArrayList<AbstractMap.Entry<Integer, Integer>>(attachmentList.size());
					for (Goods goods : attachmentList) {
						rewards.add(new AbstractMap.SimpleEntry(goods.getId(), goods.getCount()));
					}
					list = PlayerHelper.addResources(player, rewards, OpType.Mail);
				} else {
					MailConfig mailConfig = MailManager.instance().get(mail.getMailId());
					list = PlayerHelper.addResources(player, mailConfig.Reward, OpType.Mail);
				}
//				mail.update() ; 
				DAO.update(mail);
			}
		}
		return list;
	}

	public List<RewardInfo> receiveBatch() {

		List<RewardInfo> list = new ArrayList<RewardInfo>();
		Collection<Mail> values = this.mails.values();

		for (Mail mail : values) {
			if (!mail.getReceive()) {
				List<RewardInfo> receive = receive(mail.getId());
				list.addAll(receive);
			}
		}
		PlayerHelper.mergeRewards(list);
		return list;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {

		switch (event.getType()) {

			case LoginSuccess : {
			checkNoticeMail();
			break;
		}
		}

	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class<?>[] { MailMapper.class };
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		List<Mail> mails = (List<Mail>) iterator.next(); 
		for (Mail mail : mails)
		{
			this.mails.put(mail.getId(), mail) ; 
		}
	}
	@Override
	public void initFromDbAfter() {
		// 检查过期的
		int nowTimeSeconds = DateUtil.currentTimeSeconds();
		List<Long> deleteIds = new ArrayList<>();
		for (Mail mail : this.mails.values()) {
			MailConfig mailConfig = MailManager.instance().getNullable(mail.getMailId());
			if (mailConfig != null) {
				if (mailConfig.Expiration > 0 && nowTimeSeconds - mail.getCreateTime() > mailConfig.Expiration) {
					deleteIds.add(mail.getId());
				}
				if (mailConfig.Type == 1) {
					notice = mail;
				}
			}
		}
		for (Long id : deleteIds) {
			delete(id);
		}
		if (mails.size() > GlobalConst.MailMax) {
			int delCount = mails.size() - GlobalConst.MailMax;
			List<Mail> list = new ArrayList<>(mails.values());
			Collections.sort(list, new Comparator<Mail>() {
				@Override
				public int compare(Mail o1, Mail o2) {
					return o1.getCreateTime() - o2.getCreateTime();
				}
			});

			for (int i = 0; i < delCount; i++) {
				delete(list.get(i).getId());
			}
		}
	}

	private void checkNoticeMail() {

		//检测 是否有新的全服邮件待领取
		MailHelper.onLoginAddGlobalMail(player);

		int noticeMailId = MailHelper.getNoticeMailId();
		if (noticeMailId > 0) {
//			MailConfig mailConfig = MailManager.instance().get(noticeMailId); 
			if (notice == null) {
				Mail mail = Mail.valueOfMailId(playerId, noticeMailId);
				addMail(mail);
			} else {
				// 更新公告邮件
				if (notice.getMailId() != null && notice.getMailId() != noticeMailId) {
					Mail mail = Mail.valueOfMailId(playerId, noticeMailId);
					this.mails.remove(notice.getId());
					notice = null;
					addMail(mail);
				}
			}
		}
	};

	@Override
	public void buildPlayerAllInfo(Builder builder) {

	}
	
	@Override
	public boolean alwaysStoreDataInStandaloneTable() {
		return true ;
	}

	public long getGlobalMailId() {
		return globalMailId;
	}

	public void setGlobalMailId(long globalMailId) {
		this.globalMailId = globalMailId;
	}
}
