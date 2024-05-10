package cn.game.games.net.game.module.shop;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.MonthCard;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.ShopItem;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.player.IdConstant;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.shop.monthcard.MonthCardModule;
import cn.game.protocol.generated.config.ChapterPacksConfig;
import cn.game.protocol.generated.config.MonthCardConfig;
import cn.game.protocol.generated.config.RechargeConfig;
import cn.game.protocol.generated.config.ShopItemConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.ChapterPacksManager;
import cn.game.protocol.generated.manager.MonthCardManager;
import cn.game.protocol.generated.manager.RechargeManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRequest_15000010;
import cn.game.protocol.protobuf.ShopMsg.MonthCardBuyResponse_15000011;
import cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRewardRequest_15000012;
import cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRewardResponse_15000013;
import cn.game.protocol.protobuf.ShopMsg.MonthCardDayRewardRequest_15000014;
import cn.game.protocol.protobuf.ShopMsg.MonthCardDayRewardResponse_15000015;
import cn.game.protocol.protobuf.ShopMsg.ShopChapterPacksBuyRequest_15000020;
import cn.game.protocol.protobuf.ShopMsg.ShopChapterPacksBuyResponse_15000021;
import cn.game.protocol.protobuf.ShopMsg.ShopItemBuyRequest_15000003;
import cn.game.protocol.protobuf.ShopMsg.ShopItemBuyResponse_15000004;
import cn.game.protocol.protobuf.ShopMsg.ShopItemListRequest_15000001;
import cn.game.protocol.protobuf.ShopMsg.ShopItemListResponse_15000002;
import cn.game.protocol.protobuf.ShopMsg.ShopRechargeRequest_15000022;
import cn.game.protocol.protobuf.ShopMsg.ShopRechargeResponse_15000023;
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
//		putInvoker(PbProtocol.AdvertiseWatchFinishRequest_15000030, this::advertise);
	}

	private void recharge(NetClient client, Object message) {
		ShopRechargeRequest_15000022 req = (ShopRechargeRequest_15000022) message;
		ShopRechargeResponse_15000023.Builder resp = ShopRechargeResponse_15000023.newBuilder();
		int id = req.getId();
		RechargeConfig rechargeConfig = RechargeManager.instance().get(id); 
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		Future<Boolean> pay = player.pay(rechargeConfig.PurchaseParameter);
		pay.onComplete(t -> {
			if (t.result()) {
				List<RewardInfo> resources = PlayerHelper.addResources(player, rechargeConfig.Item);
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
		if (shopItem.getItemBuyTimes() > 0) {
			client.sendProtocol(resp, ErrorMsgEnum.shop_item_buy_count_max.getId());
			return;
		}
		ShopItemConfig shopItemConfig = ShopItemManager.instance().get(itemId);

		final int[] itemsAdd = shopItemConfig.Item;
		Supplier<Boolean> addItemAction = () -> {
			List<RewardInfo> resources = PlayerHelper.addResources(player, itemsAdd);
//			if (shopItemConfig.PurchaseCnt > 0) {
				shopItem.setItemBuyTimes(shopItem.getItemBuyTimes() + 1);
//				shopItem.update();
//			}
			player.handleEvent(EventTypeEnum.BuyItems, shopId, itemId, 1);
			resp.addAllRewards(resources);
			client.sendProtocol(resp);
			return true;
		};

		Future<Boolean> pay = player.pay(shopItemConfig.PurchaseParameter);
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
		boolean checkCondition = PlayerHelper.checkCondition(player, monthCardConfig.ConditionID);
		if (!checkCondition) {
			client.sendProtocol(resp, ErrorMsgEnum.month_card_condition.getId());
			return;
		}
		int[] cost = monthCardConfig.Price;
		
		Future<Boolean> pay = player.pay(cost); 
		pay.onComplete(t -> {
			if (t.result()) {
				MonthCard newMonthCard = monthCardModule.buyMonthCard(id);
				resp.setMonthCard(newMonthCard.toProto());
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
			client.sendProtocol(resp, ErrorMsgEnum.month_card_reward_repeated.getId());
			return;
		}
		MonthCardConfig monthCardConfig = MonthCardManager.instance().get(id);
		PlayerHelper.addResources(player, monthCardConfig.PurchaseRewards);

		monthCard.setIsBuyRewards(true);
		monthCard.update();

		client.sendProtocol(resp.build());

	}
	private void monthCardDayReward(NetClient client, Object message) {

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
			client.sendProtocol(resp, ErrorMsgEnum.month_card_reward_repeated.getId());
			return;
		}
		MonthCardConfig monthCardConfig = MonthCardManager.instance().get(id);
		PlayerHelper.addResources(player, monthCardConfig.DailyRewards);

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
		Future<Boolean> pay = player.pay(chapterPacksConfig.PurchaseParameter);
		pay.onComplete(t -> {
			if (t.result()) {
				playerModule.addId(IdConstant.CHAPTER_PACK, id);
				List<RewardInfo> resources = PlayerHelper.addResources(player, chapterPacksConfig.Item);
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

