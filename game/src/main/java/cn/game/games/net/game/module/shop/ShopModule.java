package cn.game.games.net.game.module.shop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.ShopItem;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.ShopItemMapper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.FundPassConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeishiConfig;
import cn.game.protocol.generated.config.HunhuoConfig;
import cn.game.protocol.generated.config.RechargeStoreConfig;
import cn.game.protocol.generated.config.ShopConfig;
import cn.game.protocol.generated.config.ShopItemConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.FundPassManager;
import cn.game.protocol.generated.manager.HeishiManager;
import cn.game.protocol.generated.manager.HunhuoManager;
import cn.game.protocol.generated.manager.RechargeStoreManager;
import cn.game.protocol.generated.manager.ResidentPackManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.generated.manager.ShopManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.ShopMsg.FundPassInfo;
import cn.game.protocol.protobuf.ShopMsg.ShopGroupItemInfo;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;
import cn.game.util.Rnd;

public class ShopModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.LoginFinish, EventTypeEnum.NewDay,
			EventTypeEnum.NewWeek,EventTypeEnum.NewMonth,
			EventTypeEnum.LevelUp, EventTypeEnum.FuncOpen, EventTypeEnum.CostItem };

//	private Map<Long, ShopItem> itemsMap = new HashMap<Long, ShopItem>();
	/** key：shopId，value 商品 */
	private Multimap<Integer, ShopItem> shopItemsMap = ArrayListMultimap.create();
	/** 通行证里领完的奖励,key: 通行证id，购买过的 */
	private Map<Integer, List<Integer>> fundPassRewardsMap = new HashMap<Integer, List<Integer>>();
	@Deprecated
	@JsonIgnore
	private int heishiRefreshTimes;
	private IntMapWrapper heishiRefreshTimesMap = new IntMapWrapper();
	
	/** 上次免费看广告开宝箱时间 */
	private int lastFreeOpenBoxTime;
	/** 每天免费开取次数 */
	private int freeOpenBoxCount;

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class<?>[] { ShopItemMapper.class };
	}

	private void initAddCache(int shop, ShopItem item) {
//		itemsMap.put(item.getId(), item);
		shopItemsMap.put(shop, item);
	}

	private void removeCache(int shop, ShopItem item) {
		shopItemsMap.remove(shop, item);
	}


	public List<ShopItem> getShopItems(int shop) {
		return (List<ShopItem>) shopItemsMap.get(shop);
	}

	public ShopItem getShopItem(int shop, int itemId) {
		Collection<ShopItem> collection = shopItemsMap.get(shop);
		for (ShopItem shopItem : collection) {
			if (shopItem.getItemId() == itemId) {
				return shopItem;
			}
		}
		return null;
	}

	/*private void initShopItemGroup(int day, ShopItemGroupConfig config) {
		List<Integer> calcItemIdList = calcItemIdList(config);
		for (Integer itemId : calcItemIdList) {
			ShopItemConfig shopItemConfig = ShopItemManager.instance().get(itemId);
			ShopItem item = new ShopItem();
			item.setPlayerId(playerId);
			item.setGroupId(config.ID);
			item.setId(IdUtil.getId());
			item.setItemBuyTimes(0);
			item.setItemId(itemId);
			item.setCreateDay(day);
			int discount = randomDiscount(item.getItemId());
			item.setItemDiscount(discount);
	
			initAddCache(item);
			item.insert();
		}
	}
	
	private int randomDiscount(int itemId) {
		int discount = 0;
		ShopItemConfig shopItemConfig = ShopItemManager.instance().get(itemId);
		if (shopItemConfig.PurchaseType == 2) { // 带折扣的
			discount = Rnd.randomId(shopItemConfig.Discount);
		}
	
		return discount;
	}
	
	private List<Integer> calcItemIdList(ShopItemGroupConfig config) {
		List<Integer> ret = new ArrayList<>();
		if (config.ShopType == 1) { // 直接配置商品id的
			if (config.ID1.length > 0) {
				ret.add(config.ID1[0][0]);
			}
			if (config.ID2.length > 0) {
				ret.add(config.ID2[0][0]);
			}
			if (config.ID3.length > 0) {
				ret.add(config.ID3[0][0]);
			}
			if (config.ID4.length > 0) {
				ret.add(config.ID4[0][0]);
			}
			if (config.ID5.length > 0) {
				ret.add(config.ID5[0][0]);
			}
			if (config.ID6.length > 0) {
				ret.add(config.ID6[0][0]);
			}
		} else if (config.ShopType == 2) { // 随机商品id
			if (config.ID1.length > 0) {
				ret.add(Rnd.randomId(config.ID1));
			}
			if (config.ID2.length > 0) {
				ret.add(Rnd.randomId(config.ID2));
			}
			if (config.ID3.length > 0) {
				ret.add(Rnd.randomId(config.ID3));
			}
			if (config.ID4.length > 0) {
				ret.add(Rnd.randomId(config.ID4));
			}
			if (config.ID5.length > 0) {
				ret.add(Rnd.randomId(config.ID5));
			}
			if (config.ID6.length > 0) {
				ret.add(Rnd.randomId(config.ID6));
			}
		}
		return ret;
	}*/


	public int getLastFreeOpenBoxTime() {
		return lastFreeOpenBoxTime;
	}

	public IntMapWrapper getHeishiRefreshTimesMap() {
		return heishiRefreshTimesMap;
	}

	public void setHeishiRefreshTimesMap(IntMapWrapper heishiRefreshTimesMap) {
		this.heishiRefreshTimesMap = heishiRefreshTimesMap;
	}

	public void setLastFreeOpenBoxTime(int lastFreeOpenBoxTime) {
		this.lastFreeOpenBoxTime = lastFreeOpenBoxTime;
	}

	public int getFreeOpenBoxCount() {
		return freeOpenBoxCount;
	}

	public void setFreeOpenBoxCount(int freeOpenBoxCount) {
		this.freeOpenBoxCount = freeOpenBoxCount;
	}

	private void initFundPass() {
		Collection<FundPassConfig> list = FundPassManager.instance().list();
		for (FundPassConfig fundPassConfig : list) {
			if (fundPassConfig.Price.length == 0) {
				if (!this.fundPassRewardsMap.containsKey(fundPassConfig.ID)) {
					this.fundPassRewardsMap.put(fundPassConfig.ID, new ArrayList<Integer>());
				}
			}
		}
	}
	public Map<Integer, List<Integer>> getFundPassRewardsMap() {
		return fundPassRewardsMap;
	}

	private void initShop() {
		refreshShopNewDay();
		refreshShopNewWeek();
		refreshShopNewMonth();
	}

	private void refreshShopNewDay() {
		Collection<ShopConfig> shops = ShopManager.instance().list();
		for (ShopConfig shopConfig : shops) {
			if (shopConfig.Refresh == 1) {
				shopItemsMap.removeAll(shopConfig.ID);
			}
		}
		// 刷新黑市
		refreshHeishiItems(0, PlayerHelper.REFRESH_TYPE_DAY);
		heishiRefreshTimes = 0;
		heishiRefreshTimesMap.clear();
		// 刷新金币、钻石商店
		Collection<RechargeStoreConfig> rechargeStore = RechargeStoreManager.instance().list();
		for (RechargeStoreConfig rechargeStoreConfig : rechargeStore) {
			shopItemsMap.put(rechargeStoreConfig.Type, new ShopItem(rechargeStoreConfig.Item));
		}
		// 体力购买商店
		refreshStaminaItems();

		//刷新每日商店
		refreshEveryDayShop();
		
	}

	/**
	 * 每日礼包
	 */
	private void refreshEveryDayShop() {
		int shop = 12;
		refreshShopByShopType(shop);
	}

	/** 
	 * 刷新指定id的黑市，如果不指定id，则刷新所有
	 * @param shopId 
	 * @param 刷新类型 
	 */
	public void refreshHeishiItems(int shopId, int refreshType) {
		Collection<ShopConfig> shops = ShopManager.instance().list();
		for (ShopConfig shopConfig : shops) {
			if (shopConfig.Type != 2) {
				continue;
			}
			if (shopId > 0 && shopConfig.ID != shopId) {
				continue;
			}
			if (shopConfig.Refresh != refreshType) {
				continue;
			}
			// 刷新黑市
			int shop = shopConfig.ID;
			shopItemsMap.removeAll(shop);

			List<HeishiConfig> typeList = HeishiManager.instance().getShopIDTypeList(shop, 1);
			int fixCount = 0;
			if (typeList != null) {
				fixCount = typeList.size();
				for (HeishiConfig heishiConfig : typeList) {
					shopItemsMap.put(shop, new ShopItem(heishiConfig.Item));
				}
			}

			typeList = HeishiManager.instance().getShopIDTypeList(shop, 2);
			if (typeList != null) {
				List<HeishiConfig> randomWeighableElementsNonRepeating = Rnd.randomWeighableElementsNonRepeating(typeList,
						GlobalConst.HeishiShelvesCnt - fixCount);
				for (HeishiConfig heishiConfig2 : randomWeighableElementsNonRepeating) {
					shopItemsMap.put(shop, new ShopItem(heishiConfig2.Item));
				}
			}

		}
	}


	public void refreshStaminaItems() {
		// 刷新体力商店
		int shop = 5;
		shopItemsMap.removeAll(shop);

		// 刷新体力购买，写死id 5、6
		ShopItemConfig shopItemConfig = ShopItemManager.instance().getNullable(5);
		if (shopItemConfig != null) {
			shopItemsMap.put(shop, new ShopItem(shopItemConfig.ID));
		}
		shopItemConfig = ShopItemManager.instance().getNullable(6);
		if (shopItemConfig != null) {
			shopItemsMap.put(shop, new ShopItem(shopItemConfig.ID));
		}

	}

	public void refreshHunhuoItems(int shop) {
		// 刷新魂火商店
		shopItemsMap.removeAll(shop);
		List<HunhuoConfig> configs = HunhuoManager.instance().getShopIDList(shop);
		if (configs != null) {
			for (HunhuoConfig hunhuoConfig : configs) {
				shopItemsMap.put(shop, new ShopItem(hunhuoConfig.Item));
			}
		}
	}

	private void refreshShopNewWeek() {
		refreshHunhuoItems(10);
		refreshHunhuoItems(15);
		//刷新 每周礼包
		int shop = 13;
		refreshShopByShopType(shop);

		// 刷新黑市
		refreshHeishiItems(0, PlayerHelper.REFRESH_TYPE_WEEK);
	}

	private void refreshShopByShopType(int shop) {
		shopItemsMap.removeAll(shop);
		ResidentPackManager.instance().list().stream().filter(residentPackConfig -> residentPackConfig.ShopID == shop)
				.forEach(residentPackConfig -> {
					shopItemsMap.put(shop,new ShopItem(residentPackConfig.ShopItemId));
					if (!ServerContext.getInstance().getRunMode().isProduction()){
						log.info(String.format("refreshShopByShopType shopId:%d, itemId:%d, pid:%d", shop,residentPackConfig.ShopItemId,player.getPlayerId()));
					}
				});
	}

	/*private void refreshShop() {
		Set<Integer> keySet = new HashSet<Integer>(groupItemsMap.keySet());
		for (Integer group : keySet) {
			ShopItemGroupConfig groupConfig = ShopItemGroupManager.instance().get(group);
			if (groupConfig.ResetType == 1) { // 按天重置
				Collection<ShopItem> groupItems = groupItemsMap.get(group);
				int createDay = 0;
				for (ShopItem item : groupItems) {
					createDay = item.getCreateDay();
					break;
				}
				int nowDay = DateUtil.getDay();
				boolean needRefresh = nowDay - createDay >= groupConfig.ResetParameter;
				if (!needRefresh) {
					continue;
				}
				if (groupConfig.ShopType == 1) { // 直接配置商品id的
					for (ShopItem item : groupItems) {
						int discount = randomDiscount(item.getItemId());
						if (item.getItemBuyTimes() > 0 || item.getItemDiscount() != discount) {
							item.setItemBuyTimes(0);
							item.setItemDiscount(discount);
							item.update();
						}
					}
				} else if (groupConfig.ShopType == 2) {
					// 随机商品id的，一般商品id会变,先删除旧的在创建新的
					for (ShopItem item : groupItems) {
						item.delete();
					}
					for (ShopItem item : groupItems) {
						removeCache(item);
					}
					groupItemsMap.removeAll(group);
	
					initShopItemGroup(nowDay, groupConfig);
				}
			}
	
		}
	}*/

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		this.fundPassRewardsMap.forEach((k, v) -> {
			FundPassInfo.Builder fb = FundPassInfo.newBuilder();
			fb.setId(k) ; 
			fb.addAllRewardIds(v);
			builder.addFundPass(fb.build());
		});
		builder.putAllHeishiFreshTimes(heishiRefreshTimesMap.getMap());
//		builder.setHeishiFreshTimes(heishiRefreshTimes);
		int remaining = lastFreeOpenBoxTime + GlobalConst.BoxAdvertTime * 60 * 60 - DateUtil.currentTimeSeconds();
		if (remaining < 0) {
			remaining = 0;
		}
		builder.setNextFreeOpenBoxTime(remaining);
		// 合并游戏使用。
		Collection<ShopConfig> list = ShopManager.instance().list();
		for (ShopConfig shopConfig : list) {
			cn.game.protocol.protobuf.ShopMsg.ShopGroupItemInfo.Builder groupBuilder = ShopGroupItemInfo.newBuilder(); 
			groupBuilder.setShopId(shopConfig.ID) ; 
			Collection<ShopItem> shopItems = getShopItems(shopConfig.ID);
			for (ShopItem shopItem : shopItems) {
				groupBuilder.addItems(shopItem.toProto());
			}
		}
		

	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case FuncOpen: {
			InitialUI func = event.getParameter(0);
			if (func == InitialUI.Shop) {
				initShop();
			} else if (func == InitialUI.Passport) {
				initFundPass();
			}
			break;
		}
		case LoginFinish: {
			initFundPass();
			break;
		}
		case NewDay: {
			refreshShopNewDay();
			freeOpenBoxCount = 0;
			break;
		}
		case NewWeek: {
			refreshShopNewWeek();
			break;
		}
		case NewMonth:{
			refreshShopNewMonth();
			break;
		}
		case CostItem: {
			int id = event.getIntParameter(0);
			int count = event.getIntParameter(1);
			if (id == Asset.playerEnergy.ID) {
				boolean addExp = false;
				Set<Integer> keySet = fundPassRewardsMap.keySet();
				for (Integer pass : keySet) {
					FundPassConfig fundPassConfig = FundPassManager.instance().get(pass);
					if (fundPassConfig.Exp && fundPassConfig.ExpType == Asset.FundPass.ID) {
						addExp = true;
						break;
					}
				}
				if (addExp) {
					PlayerHelper.addResources(player, Asset.FundPass.ID, count, OpType.None);
				}
			}
			break;
		}
		}
	}

	private void refreshShopNewMonth() {
		refreshShopByShopType(14);
	}
}
