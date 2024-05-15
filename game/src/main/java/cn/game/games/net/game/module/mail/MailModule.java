package cn.game.games.net.game.module.mail;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.entity.Mail;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.MailMapper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.util.DAO;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

public class MailModule extends BasePlayerModule  {

	@JsonIgnore
	private Map<Long, Mail>	mails = new HashMap<>();

	public void sendOnline(Mail mail) {

		mails.put(mail.getId(), mail) ; 
		DAO.insert(mail);
//		mail.insert() ; 
	}

	public Mail get(long id) {

		return this.mails.get(id);
	}

	public void delete(long id) {
		Mail remove = this.mails.remove(id); 
		if (remove!= null) {
			remove.delete() ; 
			DAO.delete(remove);
		}
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
		List<RewardInfo> list = new ArrayList<RewardInfo>();

		Mail mail = get(id);
		if (mail != null && !mail.getReceive()) {
			List<Goods> attachmentList = mail.getAttachmentList();
			if (attachmentList != null) {

				mail.setReceive(true);
				mail.setReceiveTime(DateUtil.currentTimeSeconds());

				if (!mail.getSee()) {
					mail.setSee(true);
					mail.setSeeTime(DateUtil.currentTimeSeconds());
				}
//				mail.update() ; 
				DAO.update(mail);
				/*for (Goods goods : attachmentList) {
					List<RewardItem> addResources = PlayerHelper.addResources(player, goods.getId(), goods.getCount(), false);
					list.addAll(addResources);
				}*/
				List<AbstractMap.Entry<Integer,Integer>> rewards = new ArrayList<AbstractMap.Entry<Integer,Integer>>(attachmentList.size()) ;
				for (Goods goods : attachmentList) {
					rewards.add(new AbstractMap.SimpleEntry(goods.getId(),goods.getCount())); 
				}
				//合并奖励
				list = PlayerHelper.addResources(player, rewards, OpType.Mail);
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
		return list;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {

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
		int expiredStartTime = (int) (DateUtil.currentTimeSeconds() - DateUtil.DAY_MILLIS * 10);
		Iterator<Mail> iterator = this.mails.values().iterator();
		while (iterator.hasNext()) {
			Mail mail = (Mail) iterator.next();
			if (mail.getCreateTime() < expiredStartTime) {
//				mail.delete() ; 
				DAO.delete(mail);
				iterator.remove(); 
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
}
