package cn.game.games.net.game.module.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.entity.Mail;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.data.mapper.MailMapper;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.award.RewardHelper;
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
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;

/** 邮件独立表状态与查询；分页只读取已装载邮箱，写入口沿用显式DbEntity存储。 */
public class MailModule extends BasePlayerModule  {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.LoginSuccess };

	@JsonIgnore
	private Map<Long, Mail> mails = new ConcurrentHashMap<>();

	/** 公告邮件比较特殊，保存一个不删除。设置删除标记位，为了服务器一次更新只发一次公告邮件  */
	@JsonIgnore
	private volatile Mail notice;
	/**
	 * 领取过的全服邮件版本号
	 */
	private volatile long globalMailId;

	public Future<@Nullable Object> sendOnline(Mail mail, boolean notify) {

		Future<@Nullable Object> mailFuture = addMail(mail);
		if (notify) {
			MailInfo mailInfo = PbBuilder.buildMailInfo(mail);
			player.getGameClient().sendProtocol(MailNewPush_12010001.newBuilder().setMail(mailInfo).build());
		}
		return mailFuture; 
	}

	private Future<@Nullable Object> addMail(Mail mail) {

		if (MailHelper.isNoticeMail(mail)) {
			if (notice != null) {
				delete(notice.getId());
				notice = mail;
			} else {
				notice = mail;
			}
		}
		mails.put(mail.getId(), mail);
		return mail.insert();
	}

	public Mail get(long id) {

		return this.mails.get(id);
	}

	public boolean delete(long id) {
//		Mail mail = get(id);
//		if(mail!=null&&mail.getType()==MailType.DASHENG_XUN_SHAN.getValue()) {
//			return true;
//		}
		Mail remove = this.mails.get(id); 
		return delete(remove);
	}

	public boolean delete(Mail mail) {
		if (mail != null) {
			if (!mail.getSee()) {
				// 未查看邮件不能删除
				return false; 
			}
			this.mails.remove(mail.getId()) ; 
			if (mail == notice) {
				mail.setIsDeleted(true);
			} else {
				mail.delete();
			}
			return true;
		}
		return false;
	}
	/** 
	 * 按类型删除邮件
	 * @param type
	 * @return
	 */
	public void deleteByType(int type) {
		List<Mail> delList = new ArrayList<>();
		for (Mail mail : this.mails.values()) {
			if (mail.getType() == type) {
				delList.add(mail);
			}
		}
		for (Mail mail : delList) {
			delete(mail);
		}
	}
	public void deleteAll() {
		List<Mail> delList = new ArrayList<>(this.mails.values());
		for (Mail mail : delList) {
			delete(mail);
		}
	}

	public Collection<Mail> list() {

		return this.mails.values();
	}

	/**
	 * 查询当前玩家邮箱，不更新已读、领取、数据库或其他模块。
	 * @param filter 0全部、1未读、2当前可领取
	 * @param size 已解析的有限页大小
	 * @param cursor 相同玩家及筛选的分页游标
	 * @return 有序邮件和后续游标
	 */
	public MailPage.Result page(int filter, int size, String cursor) {
		return MailPage.select(mails.values(), playerId, filter, size, cursor, DateUtil.currentTimeSeconds());
	}

	/**
	 * 兼容没有任何已知字段的旧请求，保留完整ALL列表；新客户端须显式设置筛选或页大小启用有界分页。
	 * @return 当前玩家可见邮件的稳定倒序列表，不更新实体
	 */
	public List<Mail> legacyList() {
		return mails.values().stream().filter(mail -> mail.getPlayerId() == playerId && !mail.getIsDeleted())
				.sorted(Comparator.comparingInt(Mail::getCreateTime).thenComparingLong(Mail::getId).reversed()).toList();
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
			mail.update();
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

	/**
	 * 按与筛选一致的邮件资格领取，合格项才标记已读及已领并发起原独立表异步更新。
	 * @param id 当前玩家邮箱内的唯一邮件ID
	 * @return 实际奖励结果；不合格时为空，本返回不代表异步数据库更新已确认
	 */
	public List<RewardInfo> receive(long id) {
		List<RewardInfo> list = new ArrayList<>();

		Mail mail = get(id);
		// 筛选和实际领取共用资格，避免空奖励、坏配置或过期邮件先被标记领取。
		if (mail != null && mail.getPlayerId() == playerId && MailRewards.claimable(mail, DateUtil.currentTimeSeconds())) {
			List<Goods> attachmentList = MailRewards.attachments(mail);
			{

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
					list = PlayerHelper.addResources(player, attachmentList, OpType.Mail);
				} else {
					MailConfig mailConfig = MailManager.instance().get(mail.getMailId());
					list = PlayerHelper.addResources(player, mailConfig.Reward, OpType.Mail);
				}
				mail.update();
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
		RewardHelper.mergeRewards(list);
		return list;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {

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
			int expireTime = mail.getExpireTime(); 
			if (expireTime <= 0) {
				continue; 
			}
			MailConfig mailConfig = MailManager.instance().getNullable(mail.getMailId());
			if (mailConfig != null) {
				if (mailConfig.Type == 1) {
					notice = mail;
					continue; 
				}
				if (nowTimeSeconds  > expireTime) {
					deleteIds.add(mail.getId());
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
				if (notice.getMailId() != 0 && notice.getMailId() != noticeMailId) {
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
