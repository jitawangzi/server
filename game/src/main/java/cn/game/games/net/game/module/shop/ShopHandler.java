package cn.game.games.net.game.module.shop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.MonthCard;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.ShopItem;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.player.IdConstant;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.games.net.game.module.shop.monthcard.MonthCardModule;
import cn.game.protocol.generated.config.ChapterPacksConfig;
import cn.game.protocol.generated.config.FundPassConfig;
import cn.game.protocol.generated.config.FundPassRewardsConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HCBattleConfig;
import cn.game.protocol.generated.config.MonthCardConfig;
import cn.game.protocol.generated.config.RechargeConfig;
import cn.game.protocol.generated.config.ShopConfig;
import cn.game.protocol.generated.config.ShopItemConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.ChapterPacksManager;
import cn.game.protocol.generated.manager.FundPassManager;
import cn.game.protocol.generated.manager.FundPassRewardsManager;
import cn.game.protocol.generated.manager.HCBattleManager;
import cn.game.protocol.generated.manager.MonthCardManager;
import cn.game.protocol.generated.manager.RechargeManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.generated.manager.ShopManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRequest_15000010;
import cn.game.protocol.protobuf.ShopMsg.MonthCardBuyResponse_15000011;
import cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRewardRequest_15000012;
import cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRewardResponse_15000013;
import cn.game.protocol.protobuf.ShopMsg.MonthCardDayRewardRequest_15000014;
import cn.game.protocol.protobuf.ShopMsg.MonthCardDayRewardResponse_15000015;
import cn.game.protocol.protobuf.ShopMsg.MonthCardDoubleBonusRequest_15000016;
import cn.game.protocol.protobuf.ShopMsg.MonthCardDoubleBonusResponse_15000017;
import cn.game.protocol.protobuf.ShopMsg.ShopBoxOpenRequest_15000040;
import cn.game.protocol.protobuf.ShopMsg.ShopBoxOpenResponse_15000041;
import cn.game.protocol.protobuf.ShopMsg.ShopChapterPacksBuyRequest_15000020;
import cn.game.protocol.protobuf.ShopMsg.ShopChapterPacksBuyResponse_15000021;
import cn.game.protocol.protobuf.ShopMsg.ShopFundPassBuyRequest_15000030;
import cn.game.protocol.protobuf.ShopMsg.ShopFundPassBuyResponse_15000031;
import cn.game.protocol.protobuf.ShopMsg.ShopFundPassRewardRequest_15000032;
import cn.game.protocol.protobuf.ShopMsg.ShopFundPassRewardResponse_15000033;
import cn.game.protocol.protobuf.ShopMsg.ShopHeishiRefreshRequest_15000005;
import cn.game.protocol.protobuf.ShopMsg.ShopHeishiRefreshResponse_15000006;
import cn.game.protocol.protobuf.ShopMsg.ShopItemBuyRequest_15000003;
import cn.game.protocol.protobuf.ShopMsg.ShopItemBuyResponse_15000004;
import cn.game.protocol.protobuf.ShopMsg.ShopItemListRequest_15000001;
import cn.game.protocol.protobuf.ShopMsg.ShopItemListResponse_15000002;
import cn.game.protocol.protobuf.ShopMsg.ShopRechargeRequest_15000022;
import cn.game.protocol.protobuf.ShopMsg.ShopRechargeResponse_15000023;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;
import io.vertx.core.Future;

@Component
public class ShopHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x15;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.ShopItemListRequest_15000001, this::shopItemList);
		putInvoker(PbProtocol.ShopItemBuyRequest_15000003, this::buyShopItem);
		putInvoker(PbProtocol.MonthCardBuyRequest_15000010, this::buyMonthCard);
		putInvoker(PbProtocol.MonthCardBuyRewardRequest_15000012, this::monthCardBuyReward);
		putInvoker(PbProtocol.MonthCardDayRewardRequest_15000014, this::monthCardDayReward);
		putInvoker(PbProtocol.ShopChapterPacksBuyRequest_15000020, this::buyChapterPacks);
		putInvoker(PbProtocol.ShopRechargeRequest_15000022, this::recharge);
		putInvoker(PbProtocol.MonthCardDoubleBonusRequest_15000016, this::doubleBonus);
		putInvoker(PbProtocol.ShopFundPassBuyRequest_15000030, this::fundPassBuy);
		putInvoker(PbProtocol.ShopFundPassRewardRequest_15000032, this::fundPassReward);
		putInvoker(PbProtocol.ShopHeishiRefreshRequest_15000005, this::heishiRefresh);
		putInvoker(PbProtocol.ShopBoxOpenRequest_15000040, this::openBox);
//		putInvoker(PbProtocol.AdvertiseWatchFinishRequest_15000030, this::advertise);
	}

	private void openBox(NetClient client, Object message) {
		ShopBoxOpenRequest_15000040 req = (ShopBoxOpenRequest_15000040) message;
		ShopBoxOpenResponse_15000041.Builder resp = ShopBoxOpenResponse_15000041.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		ShopModule shopModule = player.getShopModule();
		boolean watchAds = req.getWatchAds();
		if (watchAds) {
			if (shopModule.getFreeOpenBoxCount() >= GlobalConst.BoxAdvertNum) {
				client.sendProtocol(resp.build(), ErrorMsgEnum.times_limit.getId());
				return;
			}
			if (DateUtil.currentTimeSeconds() - shopModule.getLastFreeOpenBoxTime() < GlobalConst.BoxAdvertTime * 60 * 60) {
				client.sendProtocol(resp.build(), ErrorMsgEnum.cd_time_error.getId());
				return;
			}
			shopModule.setLastFreeOpenBoxTime(DateUtil.currentTimeSeconds());
			shopModule.setFreeOpenBoxCount(shopModule.getFreeOpenBoxCount() + 1);
			player.handleEvent(EventTypeEnum.WatchAds);
		} else {
			boolean delResources = PlayerHelper.delResources(player, GlobalConst.BoSpend, OpType.BoxOpen);
			if (!delResources) {
				client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
				return;
			}
		}
		int[][] boxRandomId = GlobalConst.BoxRandomId;
		int idIndex = 0;

		ChapterModule chapterModule = player.getChapterModule();
		int mainBattleHighest = chapterModule.getMainBattleHighest();

		int chapter = 0;
		List<HCBattleConfig> battleTypeList = HCBattleManager.instance().getBattleTypeList(11);
		for (HCBattleConfig battleConfig : battleTypeList) {
			if (battleConfig.preBattle == mainBattleHighest) {
				chapter = battleConfig.Chapter;
				break;
			}
		}
		for (int i = 0; i < boxRandomId.length; i++) {
			if (chapter > boxRandomId[i][0]) {
				idIndex = i + 1;
			}
		}
		if (idIndex >= boxRandomId.length - 1) {
			idIndex = boxRandomId.length - 1;
		}
		List<RewardInfo> reward = PlayerHelper.addReward(player, boxRandomId[idIndex][1], OpType.BoxOpen);
		resp.addAllRewards(reward);
		client.sendProtocol(resp.build());
	}
	private void heishiRefresh(NetClient client, Object message) {
		ShopHeishiRefreshRequest_15000005 req = (ShopHeishiRefreshRequest_15000005) message;
		ShopHeishiRefreshResponse_15000006.Builder resp = ShopHeishiRefreshResponse_15000006.newBuilder();
		int shopId = req.getShopId();
		ShopConfig shopConfig = ShopManager.instance().get(shopId);
		if (shopConfig.Type != 2) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.Shop)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		ShopModule shopModule = player.getShopModule();
		IntMapWrapper heishiRefreshTimesMap = shopModule.getHeishiRefreshTimesMap();
		int heishiRefreshTimes = heishiRefreshTimesMap.getValue(shopId);
		int heishiPayTimes = heishiRefreshTimes - GlobalConst.HeishiFreeRefresh;
		if (heishiRefreshTimes < GlobalConst.HeishiFreeRefresh) {
			player.handleEvent(EventTypeEnum.WatchAds);
		}else {
			if (heishiPayTimes >= GlobalConst.HeishiPayfrseh.length) {
				client.sendProtocol(resp.build(), ErrorMsgEnum.times_limit.getId());
				return;
			}
			boolean delResources = PlayerHelper.delResources(player, Asset.diamond.ID, GlobalConst.HeishiPayfrseh[heishiPayTimes], OpType.HeishiFresh);
			if (!delResources) {
				client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.getId());
				return;
			}
		}
		heishiRefreshTimesMap.add(shopId);
//		shopModule.setHeishiRefreshTimes(heishiRefreshTimes + 1);
		shopModule.refreshHeishiItems(shopId);

		List<ShopItem> shopItems = shopModule.getShopItems(shopId);
		for (ShopItem shopItem : shopItems) {
			resp.addItems(shopItem.toProto());
		}
		client.sendProtocol(resp.build());
	}
	private void fundPassBuy(NetClient client, Object message) {
		ShopFundPassBuyRequest_15000030 req = (ShopFundPassBuyRequest_15000030) message;
		ShopFundPassBuyResponse_15000031.Builder resp = ShopFundPassBuyResponse_15000031.newBuilder();
		int id = req.getId();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.Passport)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		ShopModule shopModule = player.getShopModule();
		Map<Integer, List<Integer>> fundPassRewardsMap = shopModule.getFundPassRewardsMap();
		if (fundPassRewardsMap.containsKey(id)) {
			client.sendProtocol(resp, ErrorMsgEnum.repeat_request.getId());
			return;
		}
		FundPassConfig fundPassConfig = FundPassManager.instance().get(id);
		Future<Boolean> pay = player.pay(PayType.FundPass, id, fundPassConfig.Price);

		pay.onComplete(t -> {
			if (t.result()) {
				fundPassRewardsMap.put(id, new ArrayList<Integer>());
				client.sendProtocol(resp.build());
			} else {
				client.sendProtocol(resp, ErrorMsgEnum.unknown.getId());
			}
		});
	}
	private void fundPassReward(NetClient client, Object message) {
		ShopFundPassRewardRequest_15000032 req = (ShopFundPassRewardRequest_15000032) message;
		ShopFundPassRewardResponse_15000033.Builder resp = ShopFundPassRewardResponse_15000033.newBuilder();
		List<Integer> idList = req.getIdList();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.Passport)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		ShopModule shopModule = player.getShopModule();
		Map<Integer, List<Integer>> fundPassRewardsMap = shopModule.getFundPassRewardsMap();
		for (int id : idList) {
			FundPassRewardsConfig fundPassRewardsConfig = FundPassRewardsManager.instance().get(id);
			if (!fundPassRewardsMap.containsKey(fundPassRewardsConfig.Index)) {
				client.sendProtocol(resp, ErrorMsgEnum.fundpass_not_buy.getId());
				return;
			}
			List<Integer> list = fundPassRewardsMap.get(fundPassRewardsConfig.Index);
			if (list.contains(id)) {
				client.sendProtocol(resp, ErrorMsgEnum.repeat_request.getId());
				return;
			}
			FundPassConfig fundPassConfig = FundPassManager.instance().get(fundPassRewardsConfig.Index); 
			
			if (fundPassRewardsConfig.LvCondition > 0
					&& player.getPlayerModule().getExpLevelMap().getValue(fundPassConfig.ExpType) < fundPassRewardsConfig.LvCondition) {
				client.sendProtocol(resp, ErrorMsgEnum.level_not_enough.getId());
				return;
			}
			boolean checkCondition = PlayerHelper.checkCondition(player, fundPassRewardsConfig.Condition);
			if (!checkCondition) {
				client.sendProtocol(resp, ErrorMsgEnum.condition_check_error.getId());
				return;
			}
			list.add(id);
			resp.addAllRewards(PlayerHelper.addResources(player, fundPassRewardsConfig.Reward, OpType.FundPass));
		}
		client.sendProtocol(resp.build());
	}

	private void doubleBonus(NetClient client, Object message) {
		MonthCardDoubleBonusRequest_15000016 req = (MonthCardDoubleBonusRequest_15000016) message;
		MonthCardDoubleBonusResponse_15000017.Builder resp = MonthCardDoubleBonusResponse_15000017.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		MonthCardModule monthCardModule = player.getModule(MonthCardModule.class);
		if (monthCardModule.isDoubleBonus()) {
			client.sendProtocol(resp, ErrorMsgEnum.repeat_request.getId());
			return;
		}
		if (!monthCardModule.canDoubleBonus()) {
			client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		monthCardModule.setDoubleBonus(true);

		resp.addAllRewards(PlayerHelper.addResources(player, GlobalConst.DoubleBonus, OpType.MonthCardDoubleBonus));

		client.sendProtocol(resp.build());
	}

	private void recharge(NetClient client, Object message) {
		ShopRechargeRequest_15000022 req = (ShopRechargeRequest_15000022) message;
		ShopRechargeResponse_15000023.Builder resp = ShopRechargeResponse_15000023.newBuilder();
		int id = req.getId();
		RechargeConfig rechargeConfig = RechargeManager.instance().get(id); 
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		Future<Boolean> pay = player.pay(PayType.Recharge, id, rechargeConfig.PurchaseParameter);
		pay.onComplete(t -> {
			if (t.result()) {
				List<RewardInfo> resources = PlayerHelper.addResources(player, rechargeConfig.Item, OpType.ShopTrade);
				resp.addAllRewards(resources);
				client.sendProtocol(resp.build());
			} else {
				client.sendProtocol(resp, ErrorMsgEnum.unknown.getId());
			}
		});
	}

	private void buyShopItem(NetClient client, Object message) {
		ShopItemBuyRequest_15000003 req = (ShopItemBuyRequest_15000003) message;
		ShopItemBuyResponse_15000004.Builder resp = ShopItemBuyResponse_15000004.newBuilder();
		int shopId = req.getShopId();
		int itemId = req.getItemId();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		ShopModule shopModule = player.getShopModule();
		ShopItem shopItem = shopModule.getShopItem(shopId, itemId);
		if (shopItem == null) {
			client.sendProtocol(resp, ErrorMsgEnum.shop_item_not_exist.getId());
			return;
		}
		ShopItemConfig shopItemConfig = ShopItemManager.instance().get(itemId);
		if (shopItemConfig.ShopItemQuota > 0 && shopItem.getItemBuyTimes() >= shopItemConfig.ShopItemQuota) {
			client.sendProtocol(resp, ErrorMsgEnum.shop_item_buy_count_max.getId());
			return;
		}

		final int[][] itemsAdd = shopItemConfig.Item;
		Supplier<Boolean> addItemAction = () -> {
			List<RewardInfo> resources = PlayerHelper.addResources(player, itemsAdd, OpType.ShopTrade);
//			if (shopItemConfig.PurchaseCnt > 0) {
				shopItem.setItemBuyTimes(shopItem.getItemBuyTimes() + 1);
//				shopItem.update();
//			}
			player.handleEvent(EventTypeEnum.BuyItems, shopId, itemId, 1);
			resp.addAllRewards(resources);
			client.sendProtocol(resp);
			GameLogger.shoptrade(player, shopId, itemId);

			if (shopId == 12 || shopId == 13 || shopId == 14) {
//				GameLogger.acti
			}
			return true;
		};

		Future<Boolean> pay = player.pay(PayType.ShopItem, itemId, shopItemConfig.PurchaseParameter);
		pay.onComplete(t -> {
			if (t.result()) {
				addItemAction.get();
			} else {
				client.sendProtocol(resp, ErrorMsgEnum.unknown.getId());
			}
		});

		/*ShopItemConfig shopItemConfig = ShopItemManager.instance().get(shopItem.getItemId());
		if (shopItemConfig.PurchaseCnt > 0 && shopItem.getItemBuyTimes() >= shopItemConfig.PurchaseCnt) {
			client.sendProtocol(resp, ErrorMsgEnum.shop_item_buy_count_max.getId());
			return;
		}
		int[][] items = shopItemConfig.Item;
		if (shopItemConfig.PurchaseType == 4 && shopItem.getItemBuyTimes() == 0) {
			items = ShopHelper.multipleCount(items, 2);
		}
		
		final int[][] itemsAdd = items;
		Supplier<Boolean> addItemAction = () -> {
		
			PlayerHelper.addResources(player, itemsAdd);
		
			if (shopItemConfig.PurchaseCnt > 0) {
				shopItem.setItemBuyTimes(shopItem.getItemBuyTimes() + 1);
				shopItem.update();
			}
			client.sendProtocol(resp);
			return true; 
		};
		if (shopItemConfig.PurchaseType == 3 && shopItem.getItemBuyTimes() == 0) { // 首次免费
			addItemAction.get();
		} else {
			int[] cost = shopItemConfig.PurchaseParameter;
			if (shopItem.getItemDiscount() > 0) {
				cost = ShopHelper.discount(cost, shopItem.getItemDiscount());
			}
			Future<Boolean> pay = player.pay(cost); 
			pay.onComplete(t -> {
				if (t.result()) {
					addItemAction.get();
				}else {
					client.sendProtocol(resp,ErrorMsgEnum.unknown.getId());
				}
			}) ;
		}*/
	}

	private void shopItemList(NetClient client, Object message) {
		ShopItemListRequest_15000001 req = (ShopItemListRequest_15000001) message;
		ShopItemListResponse_15000002.Builder resp = ShopItemListResponse_15000002.newBuilder();
		int shop = req.getShopId();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		ShopModule shopModule = player.getShopModule();
		Collection<ShopItem> shopItems = shopModule.getShopItems(shop);
		for (ShopItem shopItem : shopItems) {
			resp.addItems(shopItem.toProto());
		}
		client.sendProtocol(resp.build());
	}

	private void buyMonthCard(NetClient client, Object message) {
		MonthCardBuyRequest_15000010 req = (MonthCardBuyRequest_15000010) message;
		MonthCardBuyResponse_15000011.Builder resp = MonthCardBuyResponse_15000011.newBuilder();
		int id = req.getId();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		MonthCardModule monthCardModule = player.getModule(MonthCardModule.class);
		MonthCard monthCard = monthCardModule.getMonthCard(id);
		if (monthCard != null) {
			client.sendProtocol(resp, ErrorMsgEnum.month_card_repeated.getId());
			return;
		}
		MonthCardConfig monthCardConfig = MonthCardManager.instance().get(id); 
		int[] cost = monthCardConfig.Price;
		
		Future<Boolean> pay = player.pay(PayType.MonthCard, id, cost);
		pay.onComplete(t -> {
			if (t.result()) {
				MonthCard newMonthCard = monthCardModule.buyMonthCard(id);
				resp.setMonthCard(newMonthCard.toProto());
				List<RewardInfo> resources = PlayerHelper.addResources(player, monthCardConfig.PurchaseRewards, OpType.MonthCardBuy);
				resp.addAllRewards(resources);

				monthCardModule.sendRewardMail(true);

				client.sendProtocol(resp.build());
			}else {
				client.sendProtocol(resp,ErrorMsgEnum.unknown.getId());
			}
		}) ;
	}

	private void monthCardBuyReward(NetClient client, Object message) {

		MonthCardBuyRewardRequest_15000012 req = (MonthCardBuyRewardRequest_15000012) message;
		MonthCardBuyRewardResponse_15000013.Builder resp = MonthCardBuyRewardResponse_15000013.newBuilder();

		int id = req.getId();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		MonthCardModule monthCardModule = player.getModule(MonthCardModule.class);
		MonthCard monthCard = monthCardModule.getMonthCard(id);
		if (monthCard == null) {
			client.sendProtocol(resp, ErrorMsgEnum.month_card_not_exist.getId());
			return;
		}
		if (monthCard.getIsBuyRewards()) {
			client.sendProtocol(resp, ErrorMsgEnum.repeat_request.getId());
			return;
		}
		MonthCardConfig monthCardConfig = MonthCardManager.instance().get(id);
		PlayerHelper.addResources(player, monthCardConfig.PurchaseRewards, OpType.MonthCardBuy);

		monthCard.setIsBuyRewards(true);
		monthCard.update();

		client.sendProtocol(resp.build());

	}

	@Deprecated
	private void monthCardDayReward(NetClient client, Object message) {
		// 直接发邮件了
		MonthCardDayRewardRequest_15000014 req = (MonthCardDayRewardRequest_15000014) message;
		MonthCardDayRewardResponse_15000015.Builder resp = MonthCardDayRewardResponse_15000015.newBuilder();

		int id = req.getId();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		MonthCardModule monthCardModule = player.getModule(MonthCardModule.class);
		MonthCard monthCard = monthCardModule.getMonthCard(id);
		if (monthCard == null) {
			client.sendProtocol(resp, ErrorMsgEnum.month_card_not_exist.getId());
			return;
		}
		if (monthCard.getIsDayRewards()) {
			client.sendProtocol(resp, ErrorMsgEnum.repeat_request.getId());
			return;
		}
		MonthCardConfig monthCardConfig = MonthCardManager.instance().get(id);
		PlayerHelper.addResources(player, monthCardConfig.DailyRewards, OpType.MonthCardDay);

		monthCard.setIsDayRewards(true);
		monthCard.update();

		client.sendProtocol(resp.build());

	}

	private void buyChapterPacks(NetClient client, Object message) {
		ShopChapterPacksBuyRequest_15000020 req = (ShopChapterPacksBuyRequest_15000020) message;
		ShopChapterPacksBuyResponse_15000021.Builder resp = ShopChapterPacksBuyResponse_15000021.newBuilder();
		int id = req.getId();
		ChapterPacksConfig chapterPacksConfig = ChapterPacksManager.instance().get(id);
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.ChapterGift)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		if (!PlayerHelper.checkCondition(player, chapterPacksConfig.Condition)) {
			client.sendProtocol(resp, ErrorMsgEnum.condition_check_error.getId());
			return;
		}
		PlayerModule playerModule = player.getPlayerModule();
		boolean hasId = playerModule.hasId(IdConstant.CHAPTER_PACK, id);
		if (hasId) {
			client.sendProtocol(resp, ErrorMsgEnum.shop_gift_repeated.getId());
			return;
		}
		Future<Boolean> pay = player.pay(PayType.ChapterPacks, id, chapterPacksConfig.PurchaseParameter);
		pay.onComplete(t -> {
			if (t.result()) {
				playerModule.addId(IdConstant.CHAPTER_PACK, id);
				List<RewardInfo> resources = PlayerHelper.addResources(player, chapterPacksConfig.Item, OpType.ChapterGift);
				resp.addAllRewards(resources);
				client.sendProtocol(resp.build());
			}else {
				client.sendProtocol(resp,ErrorMsgEnum.unknown.getId());
			}
		}) ;
	}

	/*	private void advertise(NetClient client, Object message) {
			AdvertiseWatchFinishRequest_15000030 req = (AdvertiseWatchFinishRequest_15000030) message;
			AdvertiseWatchFinishResponse_15000031.Builder resp = AdvertiseWatchFinishResponse_15000031.newBuilder();
			Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
			Consumer<?> adsAction = player.getAdsAction();
			if (adsAction != null) {
				adsAction.accept(null);
				player.setAdsAction(null);
			}
			client.sendProtocol(resp.build());
		}*/
}

