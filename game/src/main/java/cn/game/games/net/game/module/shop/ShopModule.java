package cn.game.games.net.game.module.shop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.ShopItem;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.FixItemStoreConfig;
import cn.game.protocol.generated.config.FundPassConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeishiConfig;
import cn.game.protocol.generated.config.HunhuoConfig;
import cn.game.protocol.generated.config.RSGTreeShopConfig;
import cn.game.protocol.generated.config.RechargeStoreConfig;
import cn.game.protocol.generated.config.ShopConfig;
import cn.game.protocol.generated.config.ShopItemConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.DragonStoreManager;
import cn.game.protocol.generated.manager.FixItemStoreManager;
import cn.game.protocol.generated.manager.FundPassManager;
import cn.game.protocol.generated.manager.HeishiManager;
import cn.game.protocol.generated.manager.HunhuoManager;
import cn.game.protocol.generated.manager.RSGTreeShopManager;
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
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.LoginFinish,
			EventTypeEnum.NewDay, EventTypeEnum.NewWeek, EventTypeEnum.NewMonth, EventTypeEnum.LevelUp, EventTypeEnum.FuncOpen,
			EventTypeEnum.CostItem };

//	private Map<Long, ShopItem> itemsMap = new HashMap<Long, ShopItem>();
	/** key：shopId，value 商品 */
	private Multimap<Integer, ShopItem> shopItemsMap = ArrayListMultimap.create();
	/** 通行证里领完的奖励,key: 通行证id，购买过的 */
	private Map<Integer, List<Integer>> fundPassRewardsMap = new HashMap<Integer, List<Integer>>();
	private IntMapWrapper heishiRefreshTimesMap = new IntMapWrapper();

	/** 上次免费看广告开宝箱时间 */
	private int lastFreeOpenBoxTime;
	/** 每天免费开取次数 */
	private int freeOpenBoxCount;

//	@Override
//	public Class<?>[] defaultDbMapperClass() {
//		return new Class<?>[] { ShopItemMapper.class };
//	}

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

	public int getLastFreeOpenBoxTime() {
		return lastFreeOpenBoxTime;
	}

	public IntMapWrapper getHeishiRefreshTimesMap() {
		return heishiRefreshTimesMap;
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

	/** 
	 * 初始化所有的商店
	 */
	private void initShop() {
		Collection<ShopConfig> list = ShopManager.instance().list();
		for (ShopConfig shopConfig : list) {
			refreshShop(shopConfig.ID);
		}
	}

	/** 
	 * 初始化或者重新初始化商店
	 * @param shopId
	 */
	private void refreshShop(int shopId) {
		ShopConfig shopConfig = ShopManager.instance().get(shopId);
		// 优先使用通用的刷新方法
		if (shopConfig.ItemRefreshType == 1) {
			refreshFixItems(shopId);
			return ; 
		}
		// 特殊规则自定义刷新方法
		switch (shopConfig.Type) {
		case 1: {
			break;
		}
		case 2: {
			// 刷新黑市
			refreshHeishiItems(shopId);
			break;
		}
		case 3:
		case 4: {
			// 刷新金币、钻石商店
			shopItemsMap.removeAll(shopId);
			Collection<RechargeStoreConfig> rechargeStore = RechargeStoreManager.instance().list();
			for (RechargeStoreConfig rechargeStoreConfig : rechargeStore) {
				shopItemsMap.put(rechargeStoreConfig.Type, new ShopItem(rechargeStoreConfig.Item));
			}
			break;
		}
		case 5: {
			// 体力购买商店
			refreshStaminaItems(shopId);
			break;
		}
		case 10: {
			refreshHunhuoItems(shopId);
			break;
		}
		case 11: {
			break;
		}
		case 12:
		case 13:
		case 14: {
			refreshGift(shopId);
			break;
		}
		case 15: {
			// 大道争锋商店
			refreshHunhuoItems(shopId);
			break;
		}
		case 16: {
			// 神通商店？
			break;
		}
		case 17: {
//			refreshGuildShop(shopId);
			break;
		}
		case 18: {
			// 刷新人参果树商店
			refreshGinsengTreeItems(shopId);
			break;
		}
		case 19: {
			refreshGemTowerItems(shopId);
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected  shop type: " + shopConfig.Type);
		}

	}

	/** 
	 * 按照商店的刷新类型  刷新整个商店
	 * @param refreshType 日、月、周
	 */
	private void refreshShopByRefreshType(int refreshType) {
		List<ShopConfig> refreshList = ShopManager.instance().getRefreshList(refreshType);
		for (ShopConfig shopConfig : refreshList) {
			refreshShop(shopConfig.ID);
		}
	}
	/** 
	 * 不刷新整个商店，只重置商店中的商品购买次数
	 * @param refreshType 1天  2周  3月
	 */
	private void refreshShopItemBuyCount(int refreshType) {
		List<ShopConfig> refreshList = ShopManager.instance().getRefreshList(4);
		for (ShopConfig shopConfig : refreshList) {
			List<ShopItem> shopItems = getShopItems(shopConfig.ID); 
			for (ShopItem shopItem : shopItems) {
				ShopItemConfig shopItemConfig = ShopItemManager.instance().get(shopItem.getItemId()); 
				if (shopItemConfig.ResetType == refreshType) {
					shopItem.setItemBuyTimes(0);
				}
			}
		}
	}

	/** 
	 * 根据商店类型来进行刷新
	 * @param shopType
	 */
	public void refreshShopByShopType(int shopType) {
		Collection<ShopConfig> list = ShopManager.instance().list();
		for (ShopConfig shopConfig : list) {
			if (shopConfig.Type == shopType) {
				refreshShop(shopConfig.ID);
			}
		}
	}

	/** 
	 * 根据类型获取配置，一般是获取商店表id
	 * @param shopType
	 * @return
	 */
	public List<ShopConfig> getShopConfigListByType(int shopType) {
		List<ShopConfig> list = new ArrayList<>();
		Collection<ShopConfig> shopConfigs = ShopManager.instance().list();
		for (ShopConfig shopConfig : shopConfigs) {
			if (shopConfig.Type == shopType) {
				list.add(shopConfig);
			}
		}
		return list;
	}

	/** 
	 * 刷新指定id的黑市，如果不指定id，则刷新所有
	 * @param shopId 
	 * @param 刷新类型 
	 */
	public void refreshHeishiItems(int shop) {

		// 刷新黑市
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

	public void refreshStaminaItems(int shop) {
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

	public void refreshGinsengTreeItems(int shop) {
		if (!player.isFuncOpen(InitialUI.RSGTree)) {
			return;
		}
		shopItemsMap.removeAll(shop);
		int level = player.getLevel(Asset.RSGTreeExp);
		List<RSGTreeShopConfig> list = RSGTreeShopManager.instance().list();
		for (RSGTreeShopConfig rsgTreeShopConfig : list) {
			if (level >= rsgTreeShopConfig.Condition) {
				ShopItemConfig shopItemConfig = ShopItemManager.instance().getNullable(rsgTreeShopConfig.Item);
				if (shopItemConfig != null) {
					shopItemsMap.put(shop, new ShopItem(shopItemConfig.ID));
				}
			}
		}
	}

	/** 
	 * 人参果树升级时，解锁新增的商店物品
	 */
	private void addGinsengTreeItems(int shop) {
		int level = player.getLevel(Asset.RSGTreeExp);
		List<RSGTreeShopConfig> list = RSGTreeShopManager.instance().list();
		for (RSGTreeShopConfig rsgTreeShopConfig : list) {
			if (level == rsgTreeShopConfig.Condition) {
				ShopItemConfig shopItemConfig = ShopItemManager.instance().getNullable(rsgTreeShopConfig.Item);
				if (shopItemConfig != null) {
					shopItemsMap.put(shop, new ShopItem(shopItemConfig.ID));
				}
			}
		}
	}

	public void refreshGemTowerItems(int shop) {
		// 刷新爬塔商店
		shopItemsMap.removeAll(shop);
		int level = player.getLevel();
		DragonStoreManager.instance().list().forEach(shopConfig -> {
			if (level >= shopConfig.LevelUnlock) {
				shopItemsMap.put(shop, new ShopItem(shopConfig.Item));
			}
		});
	}

	public void refreshGemTowerItems_LevelUp(int shop) {
		// 刷新爬塔商店
		int level = player.getLevel();
		DragonStoreManager.instance().list().forEach(shopConfig -> {
			if (level >= shopConfig.LevelUnlock) {
				List<ShopItem> tmp = (List<ShopItem>) shopItemsMap.get(shop);
				for (int i = 0; i < tmp.size(); i++) {
					ShopItem item = tmp.get(i);
					if (item.getItemId() == shopConfig.Item) {
						return;
					}
				}
				shopItemsMap.put(shop, new ShopItem(shopConfig.Item));
			}
		});
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
	
	/** 
	 * 刷新固定商品的商店
	 * @param shop
	 */
	public void refreshFixItems(int shop) {
		shopItemsMap.removeAll(shop);
		List<FixItemStoreConfig> itemList = FixItemStoreManager.instance().getShopIDList(shop);
		for (FixItemStoreConfig config : itemList) {
			shopItemsMap.put(shop, new ShopItem(config.Item));
		}
	}

	private void refreshGift(int shop) {
		shopItemsMap.removeAll(shop);
		ResidentPackManager.instance()
				.list()
				.stream()
				.filter(residentPackConfig -> residentPackConfig.ShopID == shop)
				.forEach(residentPackConfig -> {
					shopItemsMap.put(shop, new ShopItem(residentPackConfig.ShopItemId));
				});
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		this.fundPassRewardsMap.forEach((k, v) -> {
			FundPassInfo.Builder fb = FundPassInfo.newBuilder();
			fb.setId(k);
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
			groupBuilder.setShopId(shopConfig.ID);
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
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case FuncOpen: {
			InitialUI func = event.getParameter(0);
			if (func == InitialUI.Shop) {
				initShop();
			} else if (func == InitialUI.Passport) {
				initFundPass();
			} else if (func == InitialUI.RSGTree) {
				refreshShopByShopType(18);
			}
			break;
		}
		case LoginFinish: {
			initFundPass();
			break;
		}
		case NewDay: {
			freeOpenBoxCount = 0;
			heishiRefreshTimesMap.clear();
			refreshShopByRefreshType(1);
			refreshShopItemBuyCount(1);
			break;
		}
		case NewWeek: {
			refreshShopByRefreshType(2);
			refreshShopItemBuyCount(2);
			break;
		}
		case NewMonth: {
			refreshShopByRefreshType(3);
			refreshShopItemBuyCount(3);
			break;
		}
		case LevelUp: {
			int exp = event.getIntParameter(0);
			if (exp == Asset.playerExp.ID) {
				List<ShopConfig> shopConfigListByType = getShopConfigListByType(19);
				for (ShopConfig shopConfig : shopConfigListByType) {
					refreshGemTowerItems_LevelUp(shopConfig.ID);
				}
			} else if (exp == Asset.RSGTreeExp.ID) {
				List<ShopConfig> shopConfigListByType = getShopConfigListByType(18);
				for (ShopConfig shopConfig : shopConfigListByType) {
					addGinsengTreeItems(shopConfig.ID);
				}
			}
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

}
