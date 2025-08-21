package cn.game.games.net.game.module.recharge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.game.core.util.AsyncUtils;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.ShopItem;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.activity.impl.player.FirstChargeActivity;
import cn.game.games.net.game.module.player.IdConstant;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.shop.ShopModule;
import cn.game.games.net.game.module.shop.limitedtimegift.LimitedTimeGiftModule;
import cn.game.games.net.game.module.shop.monthcard.MonthCardModule;
import cn.game.games.net.game.module.shop.xianshilibao.XianShiLiBaoModule;
import cn.game.games.net.game.module.vip.VipModule;
import cn.game.protocol.generated.config.ActivityJQBConfig;
import cn.game.protocol.generated.config.ActivityXianShiLiBaoConfig;
import cn.game.protocol.generated.config.ChapterPacksConfig;
import cn.game.protocol.generated.config.MonthCardConfig;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.config.RechargeConfig;
import cn.game.protocol.generated.config.ShopItemConfig;
import cn.game.protocol.generated.config.VIPConfig;
import cn.game.protocol.generated.manager.ActivityJQBManager;
import cn.game.protocol.generated.manager.ActivityXianShiLiBaoManager;
import cn.game.protocol.generated.manager.ChapterPacksManager;
import cn.game.protocol.generated.manager.MonthCardManager;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.generated.manager.RechargeManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.generated.manager.VIPManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.log.LoggerType;
import io.vertx.core.Future;

public enum PayType {
	/** 首次充值 */
	FirstCharge(1){
		@Override
		public boolean offlinePay(Player player, PayItem payItem) {
			int activityId = payItem.getPaySubIds().get(0);
			int chargeId = payItem.getPayId();
			FirstChargeActivity activityBase = (FirstChargeActivity) player.getActivityModule().get(activityId);
			activityBase.buy(chargeId);
			return true;
		}
	},

	/** 金钱豹爆爆 */
	ActivityJQB(2){
		@Override
		public boolean offlinePay(Player player, PayItem payItem) {
			ActivityJQBConfig activityJQBConfig = ActivityJQBManager.instance().get(payItem.getPayId());
			Quest quest = player.getQuestModule().get(activityJQBConfig.taskID);
			QuestConfig config = QuestManager.instance().get(quest.getId());
			PlayerHelper.addReward(player,config.Reward, OpType.ActivityJQB);
			player.getActivityModule().get(activityJQBConfig.ActivityiD).checkRefreshActivity();
			return true;
		}
	},

	/** 章节礼包 */
	ChapterPacks(3){
		@Override
		public boolean offlinePay(Player player, PayItem payItem) {
			ChapterPacksConfig chapterPacksConfig = ChapterPacksManager.instance().get(payItem.getPayId());
			PlayerModule playerModule = player.getPlayerModule();
			playerModule.addId(IdConstant.CHAPTER_PACK, payItem.getPayId());
			PlayerHelper.addResources(player, chapterPacksConfig.Item, OpType.ChapterGift);
			return true;
		}
	},

	/** 月卡 */
	MonthCard(4){
		@Override
		public boolean offlinePay(Player player, PayItem payItem) {
			MonthCardConfig monthCardConfig = MonthCardManager.instance().get(payItem.getPayId());
			MonthCardModule monthCardModule = player.getModule(MonthCardModule.class);
			monthCardModule.buyMonthCard(payItem.getPayId());
			PlayerHelper.addResources(player, monthCardConfig.PurchaseRewards, OpType.MonthCardBuy);
			monthCardModule.sendRewardMail(false);
			return true;
		}
	},

	/** 商店商品 */
	ShopItem(5){
		@Override
		public boolean offlinePay(Player player, PayItem payItem) {
			int itemId = payItem.getPayId();
			int shopId = payItem.getPaySubIds().get(0);
			ShopModule shopModule = player.getShopModule();
			ShopItem shopItem = shopModule.getShopItem(shopId, itemId);
			ShopItemConfig shopItemConfig = ShopItemManager.instance().get(itemId);
			PlayerHelper.addResources(player, shopItemConfig.Item, OpType.ShopTrade);
			shopItem.setItemBuyTimes(shopItem.getItemBuyTimes() + 1);
			player.handleEvent(EventTypeEnum.BuyItems, shopId, itemId, 1);
			GameLogger.shoptrade(player, shopId, itemId);
			return true;
		}
	},

	/** 通行证 */
	FundPass(6){
		@Override
		public boolean offlinePay(Player player, PayItem payItem) {
			int id = payItem.getPayId();
			ShopModule shopModule = player.getShopModule();
			Map<Integer, List<Integer>> fundPassRewardsMap = shopModule.getFundPassRewardsMap();
			fundPassRewardsMap.put(id, new ArrayList<Integer>());
			return true;
		}
	},

	/** 普通充值兑换 */
	Recharge(7){
		@Override
		public boolean offlinePay(Player player, PayItem payItem) {
			int id = payItem.getPayId();
			RechargeConfig rechargeConfig = RechargeManager.instance().get(id);
			PlayerHelper.addResources(player, rechargeConfig.Item, OpType.ShopTrade);
			return true;
		}
	},

	/** Vip礼包 */
	VipGift(8){
		@Override
		public boolean offlinePay(Player player, PayItem payItem) {
			int buyId = payItem.getPayId();
			VipModule vipModule = player.getVipModule();
			vipModule.getBuyGiftList().add(buyId);
			VIPConfig buyConfig = VIPManager.instance().get(buyId);
			PlayerHelper.addReward(player,buyConfig.RandomGivenId,OpType.vipGiftReward);
			return true;
		}
	},
	/**限时礼包购买*/
	XianShiLiBao(9) {
		public boolean offlinePay(Player player, PayItem payItem) {
			int id = payItem.getPayId();
			ActivityXianShiLiBaoConfig activityXianShiLiBaoConfig = ActivityXianShiLiBaoManager.instance().get(id);
			XianShiLiBaoModule xianShiLiBaoModule = player.getModule(XianShiLiBaoModule.class);
			xianShiLiBaoModule.addBuyId(activityXianShiLiBaoConfig);
			return true;
		}
	},
	/**每日优惠礼包购买*/
	DayGift(10) {
		public boolean offlinePay(Player player, PayItem payItem) {
			int id = payItem.getPayId();

//			ActivityXianShiLiBaoConfig activityXianShiLiBaoConfig = ActivityXianShiLiBaoManager.instance().get(id);
//			XianShiLiBaoModule xianShiLiBaoModule = player.getModule(XianShiLiBaoModule.class);
//			xianShiLiBaoModule.addBuyId(activityXianShiLiBaoConfig);
			return false;
		}
	},
	/**限时礼包购买*/
	LimitedTimeGift(11) {
		public boolean offlinePay(Player player, PayItem payItem) {
			int id = payItem.getPayId();
			LimitedTimeGiftModule module = player.getModule(LimitedTimeGiftModule.class);
			Future<List<RewardInfo>> buy = module.buy(id,false);
			buy.onFailure(err -> {
				LoggerType.Stdout.logger.error("玩家"+player.getPlayerId()+"离线充值购买限时礼包失败", err);
			});
			return AsyncUtils.await(buy)!=null; 
		}
	},
	;


	private int id;

	PayType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	/**
	 * 玩家离线充值  玩家可能已经下线了，但是腾讯那边充值回调过来了，这时候 玩家重新登录的时候 对于这笔订单需要补单
	 * @param player 玩家
	 * @param payItem 补单的信息
	 */
	public boolean offlinePay(Player player, PayItem payItem){
		LoggerType.Stdout.logger.error("%s 玩家离线充值 未定义 pid:%d",payItem.getPayType(),player.getPlayerId());
		return false;
	}

}