package cn.game.games.net.game.module.shop;

import java.util.Collection;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.MonthCard;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.ShopItem;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.player.IdConstant;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.shop.monthcard.MonthCardModule;
import cn.game.protocol.generated.config.MonthCardConfig;
import cn.game.protocol.generated.config.ShopGiftConfig;
import cn.game.protocol.generated.config.ShopItemConfig;
import cn.game.protocol.generated.manager.MonthCardManager;
import cn.game.protocol.generated.manager.ShopGiftManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ShopMsg.AdvertiseWatchFinishRequest_15000030;
import cn.game.protocol.protobuf.ShopMsg.AdvertiseWatchFinishResponse_15000031;
import cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRequest_15000010;
import cn.game.protocol.protobuf.ShopMsg.MonthCardBuyResponse_15000011;
import cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRewardRequest_15000012;
import cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRewardResponse_15000013;
import cn.game.protocol.protobuf.ShopMsg.MonthCardDayRewardRequest_15000014;
import cn.game.protocol.protobuf.ShopMsg.MonthCardDayRewardResponse_15000015;
import cn.game.protocol.protobuf.ShopMsg.ShopGiftBuyRequest_15000020;
import cn.game.protocol.protobuf.ShopMsg.ShopGiftBuyResponse_15000021;
import cn.game.protocol.protobuf.ShopMsg.ShopGroupItemListRequest_15000001;
import cn.game.protocol.protobuf.ShopMsg.ShopGroupItemListResponse_15000002;
import cn.game.protocol.protobuf.ShopMsg.ShopItemBuyRequest_15000003;
import cn.game.protocol.protobuf.ShopMsg.ShopItemBuyResponse_15000004;
import io.vertx.core.Future;

@Component
public class ShopHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x15;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.ShopGroupItemListRequest_15000001, this::shopItemGroupList);
		putInvoker(PbProtocol.ShopItemBuyRequest_15000003, this::buyShopItem);
		putInvoker(PbProtocol.MonthCardBuyRequest_15000010, this::buyMonthCard);
		putInvoker(PbProtocol.MonthCardBuyRewardRequest_15000012, this::monthCardBuyReward);
		putInvoker(PbProtocol.MonthCardDayRewardRequest_15000014, this::monthCardDayReward);
		putInvoker(PbProtocol.ShopGiftBuyRequest_15000020, this::buyShopGift);
		putInvoker(PbProtocol.AdvertiseWatchFinishRequest_15000030, this::advertise);
	}

	private void buyShopItem(NetClient client, Object message) {
		ShopItemBuyRequest_15000003 req = (ShopItemBuyRequest_15000003) message;
		ShopItemBuyResponse_15000004.Builder resp = ShopItemBuyResponse_15000004.newBuilder();
		long id = Long.parseLong(req.getId());
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		ShopModule shopModule = player.getShopModule();
		ShopItem shopItem = shopModule.getShopItem(id);
		if (shopItem == null) {
			client.sendProtocol(resp, ErrorMsgEnum.shop_item_not_exist.getId());
			return;
		}
		ShopItemConfig shopItemConfig = ShopItemManager.instance().get(shopItem.getItemId());
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

			PlayerHelper.addResources(player.getPlayerId(), itemsAdd);

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
		}
	}

	private void shopItemGroupList(NetClient client, Object message) {
		ShopGroupItemListRequest_15000001 req = (ShopGroupItemListRequest_15000001) message;
		ShopGroupItemListResponse_15000002.Builder resp = ShopGroupItemListResponse_15000002.newBuilder();
		int group = req.getGroup();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		ShopModule shopModule = player.getShopModule();
		Collection<ShopItem> shopItems = shopModule.getShopItems(group);
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
		boolean checkCondition = PlayerHelper.checkCondition(player.getPlayerId(), monthCardConfig.ConditionID);
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
		PlayerHelper.addResources(player.getPlayerId(), monthCardConfig.PurchaseRewards);

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
		PlayerHelper.addResources(player.getPlayerId(), monthCardConfig.DailyRewards);

		monthCard.setIsDayRewards(true);
		monthCard.update();

		client.sendProtocol(resp.build());

	}

	private void buyShopGift(NetClient client, Object message) {
		ShopGiftBuyRequest_15000020 req = (ShopGiftBuyRequest_15000020) message;
		ShopGiftBuyResponse_15000021.Builder resp = ShopGiftBuyResponse_15000021.newBuilder();
		int id = req.getId();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		PlayerModule playerModule = player.getPlayerModule();
		boolean hasId = playerModule.hasId(IdConstant.SHOP_GIFT, id);
		if (hasId) {
			client.sendProtocol(resp, ErrorMsgEnum.shop_gift_repeated.getId());
			return;
		}
		ShopGiftConfig shopGiftConfig = ShopGiftManager.instance().get(id);
		boolean checkCondition = PlayerHelper.checkCondition(player.getPlayerId(), shopGiftConfig.Condition, null);
		if (!checkCondition) {
			client.sendProtocol(resp, ErrorMsgEnum.shop_gift_condition.getId());
			return;
		}
		int[] cost = shopGiftConfig.Price;
		
		Future<Boolean> pay = player.pay(cost); 
		pay.onComplete(t -> {
			if (t.result()) {
				playerModule.addId(IdConstant.SHOP_GIFT, id);
				PlayerHelper.addResources(player.getPlayerId(), shopGiftConfig.Item);
				client.sendProtocol(resp.build());
			}else {
				client.sendProtocol(resp,ErrorMsgEnum.unknown.getId());
			}
		}) ;
	}

	private void advertise(NetClient client, Object message) {
		AdvertiseWatchFinishRequest_15000030 req = (AdvertiseWatchFinishRequest_15000030) message;
		AdvertiseWatchFinishResponse_15000031.Builder resp = AdvertiseWatchFinishResponse_15000031.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
//		Consumer<?> adsAction = player.getAdsAction();
//		if (adsAction != null) {
//			adsAction.accept(null);
//			player.setAdsAction(null);
//		}
		client.sendProtocol(resp.build());
	}
}

