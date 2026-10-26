package cn.game.games.net.game.module.shop.monthcard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.games.cache.entity.MonthCard;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.module.activity.impl.player.SevenDaysSignin;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.generated.config.MonthCardConfig;
import cn.game.protocol.generated.config.SevenDaysSigninConfig;
import cn.game.protocol.generated.enume.WelfareTypeEnum;
import cn.game.protocol.generated.manager.MonthCardManager;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.DateUtil;
import cn.game.util.GameUtil;

public class MonthCardModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.NewDay };

	/** 一张月卡奖励周期(天) */
//	public static final int rechargeDays = 30;
	/** 双月卡期间，是否领取过奖励 */
	private boolean doubleBonus;

	private Map<Integer, MonthCard> monthCards = new HashMap<>();

	public MonthCard getMonthCard(int cardId) {
		return monthCards.get(cardId);
	}

	/**
	 * 买月卡
	 * 
	 * @param cardId
	 */
	public MonthCard buyMonthCard(int cardId) {
		long buyTime = DateUtil.getDayHourTimestamp(0);
		MonthCardConfig monthCardConfig = MonthCardManager.instance().get(cardId);
		long expireTime = 0;
		if (monthCardConfig.Expiration > 0) {
			expireTime = buyTime + monthCardConfig.Expiration * DateUtil.DAY_MILLIS;
		}
		MonthCard newCard = MonthCard.valueOf(this.playerId, cardId, buyTime, expireTime);
		monthCards.put(cardId, newCard);
		if (cardId == 1){//购买月卡， 检查是否有月卡签到
//			checkMothCardSignReward();
		}
		return newCard;
	}

	/**
	 * 月卡修改：购买月卡跳转、当天未买签到后，买了返还一天的双倍奖励
	 */
	private void checkMothCardSignReward() {
		SevenDaysSignin sevenDaysSignin = (SevenDaysSignin) player.getActivityModule().get(26);
		if (sevenDaysSignin == null) return;
		if (!sevenDaysSignin.isSignin()) return;
		SevenDaysSigninConfig config = sevenDaysSignin.getSevenDaysSigninConfig();
		int addRadio = player.getWelfareValue(WelfareTypeEnum.MonthClock);
		int[][] drops =  GameUtil.arrayZoomBy10k(config.Item, addRadio);
		drops =  GameUtil.subItems(drops, config.Item);
		List<Goods> dropItems = Goods.valueOf(drops);
		MailHelper.sendMail(player.getPlayerId(), 20, dropItems, true);
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {

		switch (event.getType()) {
		// 正常应该是在功能开启时初始化商店。
		case NewDay: {
//			resetDayReward();
			checkExpire();
		}
		}

	}

	@Override
	public void initFromDbAfter() {
		checkExpire();
	}

	private void checkExpire() {
		// 检查过期之间，先发奖励
		sendRewardMail(false);

		long nowTime = System.currentTimeMillis();
		Collection<MonthCard> values = monthCards.values(); 
		List<Integer> removeList = new ArrayList<>();
		for (MonthCard card : values) {
			if (card.getExpireTime() > 0 && card.getExpireTime() <= nowTime) {
				removeList.add(card.getMonthCardId()); 
			}
		}
		for (Integer integer : removeList) {
			monthCards.remove(integer) ; 
			doubleBonus = false;
		}
	};

    /** @param notify 是否推送邮件；常规跨天链路保持原异步投递行为 */
    public void sendRewardMail(boolean notify) { sendRewardMail(notify, false); }

    /** 充值履约直接复用本实例邮件模块，等待邮件插入终态，避免等待同邮箱 RPC。
     * @param notify 是否推送新增邮件；不代表最终购买成功 */
    public void sendPaymentRewardMail(boolean notify) { sendRewardMail(notify, true); }

    /** @param notify 是否推送 @param confirmed 是否等待本实例邮件的实际插入结果 */
    private void sendRewardMail(boolean notify, boolean confirmed) {
		long rewardTime = DateUtil.currentTimeMillis();
		for (MonthCard monthCard : monthCards.values()) {
			long expireTime = monthCard.getExpireTime();
			if (expireTime > 0) {
				if (expireTime < rewardTime) {
					rewardTime = expireTime;
				}
			}
			long diffInDays = DateUtil.diffDays(monthCard.getLastRewardTime(), rewardTime);

			if (diffInDays <= 0) {
				continue;
			}
			MonthCardConfig monthCardConfig = MonthCardManager.instance().get(monthCard.getMonthCardId());
			List<Goods> attachmentList = new ArrayList<>();
			for (int i = 0; i < diffInDays; i++) {
				attachmentList.add(Goods.valueOf(monthCardConfig.DailyRewards));
			}
            if (confirmed) {
                cn.game.core.util.AsyncUtils.await(sendPaymentMail(attachmentList, notify));
            } else {
                MailHelper.sendMail(playerId, 7, attachmentList, notify);
            }
			monthCard.setLastRewardTime(DateUtil.currentTimeMillis());
		}
	}

    /** @param attachments 按既有每日奖励计算的附件 @param notify 是否通知 @return 本实例邮件插入的实际结果 */
    protected io.vertx.core.Future<Object> sendPaymentMail(List<Goods> attachments, boolean notify) {
        cn.game.games.cache.entity.Mail mail = cn.game.games.cache.entity.Mail.valueOfMailId(playerId, 7, attachments);
        return player.getMailModule().sendOnline(mail, notify);
    }
	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (MonthCard monthCard : monthCards.values()) {
			builder.addMonthCards(monthCard.toProto());
		}
		if (doubleBonus) {
			builder.setMonthCardDoubleBonus(false);
		} else {
			builder.setMonthCardDoubleBonus(canDoubleBonus());
		}
	}

	public boolean canDoubleBonus() {

		boolean all = true;
		Collection<MonthCardConfig> list = MonthCardManager.instance().list();
		for (MonthCardConfig monthCardConfig : list) {
			if (!monthCards.containsKey(monthCardConfig.ID)) {
				all = false;
			}
		}
		return all;
	}

	public boolean hasMonthCard() {
		return !monthCards.isEmpty();
	}
	public boolean hasMonthCard(int id) {
		return monthCards.containsKey(id);
	}

	public boolean isDoubleBonus() {
		return doubleBonus;
	}

	public void setDoubleBonus(boolean doubleBonus) {
		this.doubleBonus = doubleBonus;
	}

	public Map<Integer, MonthCard> getMonthCards() {
		return monthCards;
	}

}
