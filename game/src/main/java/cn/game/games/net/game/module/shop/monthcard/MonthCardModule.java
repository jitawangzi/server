package cn.game.games.net.game.module.shop.monthcard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.games.cache.entity.MonthCard;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.module.activity.impl.player.SevenDaysSignin;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.config.MonthCardConfig;
import cn.game.protocol.generated.config.SevenDaysSigninConfig;
import cn.game.protocol.generated.enume.WelfareTypeEnum;
import cn.game.protocol.generated.manager.ActivityManager;
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
		newCard.insert();
		if (cardId == 1){//购买月卡， 检查是否有月卡签到
			checkMothCardSignReward();
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
		int[][] drops =  GameUtil.arrayAddition(config.Item, addRadio);
		drops =  GameUtil.subItems(drops, config.Item);
		List<Goods> dropItems = Goods.valueOf(drops);
		MailHelper.sendMail(player.getPlayerId(), 20, dropItems, true);
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {

		switch (event.getType()) {
		// 正常应该是在功能开启时初始化商店。
		case NewDay: {
//			resetDayReward();
			checkExpire();
		}
		}

	}

	private void resetDayReward() {
		for (MonthCard monthCard : monthCards.values()) {
			if (monthCard.getIsDayRewards()) {
				monthCard.setIsDayRewards(false);
				monthCard.update();
			}
		}
	}

//	@Override
//	protected void initFromDb(ListIterator<?> iterator) {
//		List<MonthCard> cardList = (List<MonthCard>) iterator.next();
//		for (MonthCard card : cardList) {
//			monthCards.put(card.getMonthCardId(), card);
//		}
//	}
	
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
				card.delete();
				removeList.add(card.getMonthCardId()); 
			}
		}
		for (Integer integer : removeList) {
			monthCards.remove(integer) ; 
			doubleBonus = false;
		}
	};

	public void sendRewardMail(boolean notify) {
		long rewardTime = System.currentTimeMillis();
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
			MailHelper.sendMail(playerId, 7, attachmentList, notify);
			monthCard.setLastRewardTime(System.currentTimeMillis());
		}
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
