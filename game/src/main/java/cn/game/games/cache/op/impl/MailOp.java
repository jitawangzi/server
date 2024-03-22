package cn.game.games.cache.op.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import cn.game.games.cache.entity.Mail;
import cn.game.games.cache.op.face.IMailOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.MailMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.util.DAO;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class MailOp extends BasePlayerModule implements IMailOp {

	private Map<Long, Mail>	mails;

	@Override
	
	public void init() {
		mails = new HashMap<>();
	}

	@Override
	public int initLoadData(List<Mail> mails) {

		if (mails!=null)
		{
			for (Mail mail : mails)
			{
//				if (mail.getOverdueTime()==0)
//				{
//					delete(mail.getId()) ; 
//					continue ; 
//				}
				this.mails.put(mail.getId(), mail) ; 
			}
		}
		return 0;
	}

	@Override
	public void sendOnline(Mail mail) {

		mails.put(mail.getId(), mail) ; 
		MailHelper.insert(mail);

	}

	@Override
	public Mail get(long id) {

		return this.mails.get(id);
	}

	@Override
	public void update(Mail mail) {

		DAO.execute(MailMapper.class, MapperConstant.updateByPrimaryKey, mail);
	}
	@Override
	public void updateSelective(Mail mail) {
		DAO.execute(MailMapper.class, MapperConstant.updateByPrimaryKeySelective, mail);
	}

	@Override
	public void delete(long id) {

		this.mails.remove(id) ; 
		DAO.execute(MailMapper.class, MapperConstant.deleteByPrimaryKey, id);
	}

	@Override
	public Collection<Mail> list() {

		return this.mails.values();
	}

	@Override
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

	@Override
	public void see(long id) {

		Mail mail = get(id);
		if (mail == null) {
			return;
		}
		if (!mail.getSee()) {
			mail.setSee(true);
			mail.setSeeTime((int) (System.currentTimeMillis() / 1000));
			Mail update = new Mail();
			update.setId(mail.getId());
			update.setSee(mail.getSee());
			update.setSeeTime(mail.getSeeTime());
			updateSelective(update);
		}
	}

	@Override
	public void seeBatch() {
		Collection<Mail> values = this.mails.values();
		for (Mail mail : values) {
			if (!mail.getSee()) {
				see(mail.getId());
			}
		}
	}

	@Override
	public List<RewardInfo> receive(long id) {
		List<RewardInfo> list = new ArrayList<RewardInfo>();

		Mail mail = get(id);
		if (mail != null && !mail.getReceive()) {
			List<Goods> attachmentList = mail.getAttachmentList();
			if (attachmentList != null) {

				mail.setReceive(true);
				mail.setReceiveTime((int) (System.currentTimeMillis() / 1000));

				Mail update = new Mail();
				if (!mail.getSee()) {
					mail.setSee(true);
					mail.setSeeTime((int) (System.currentTimeMillis() / 1000));

					update.setSee(mail.getSee());
					update.setSeeTime(mail.getSeeTime());
				}
				update.setId(mail.getId());
				update.setReceive(mail.getReceive());
				update.setReceiveTime(mail.getReceiveTime());
				updateSelective(update);

				/*for (Goods goods : attachmentList) {
					List<RewardItem> addResources = PlayerHelper.addResources(playerId, goods.getId(), goods.getCount(), false);
					list.addAll(addResources);
				}*/
				List<Map.Entry<Integer,Integer>> rewards = new ArrayList<Map.Entry<Integer,Integer>>(attachmentList.size()) ;
				for (Goods goods : attachmentList) {
					rewards.add(new Map.Entry<Integer, Integer>() {
						@Override
						public Integer setValue(Integer value) {
							return null;
						}

						@Override
						public Integer getValue() {
							try {
								return goods.getCount();
							} catch (Exception e) {
								e.printStackTrace();
							}
							throw new NullPointerException();
						}

						@Override
						public Integer getKey() {
							try {
								return goods.getId();
							} catch (Exception e) {
								e.printStackTrace();
							}
							throw new NullPointerException();
						}
					});
				}
				//合并奖励
				list = PlayerHelper.addResources(playerId, rewards);
			}
		}
		return list;
	}

	@Override
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}
}
