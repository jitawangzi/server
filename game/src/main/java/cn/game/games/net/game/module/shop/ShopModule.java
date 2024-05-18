package cn.game.games.net.game.module.shop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import cn.game.games.cache.entity.ShopItem;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.ShopItemMapper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.FundPassConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeishiConfig;
import cn.game.protocol.generated.config.RechargeStoreConfig;
import cn.game.protocol.generated.config.ShopConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.FundPassManager;
import cn.game.protocol.generated.manager.HeishiManager;
import cn.game.protocol.generated.manager.RechargeStoreManager;
import cn.game.protocol.generated.manager.ShopManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.ShopMsg.FundPassInfo;
import cn.game.util.Rnd;

public class ShopModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.LoginFinish, EventTypeEnum.NewDay,
			EventTypeEnum.NewWeek,
			EventTypeEnum.LevelUp, EventTypeEnum.FuncOpen, EventTypeEnum.CostItem };

//	private Map<Long, ShopItem> itemsMap = new HashMap<Long, ShopItem>();
	/** key：shopId，value 商品 */
	private Multimap<Integer, ShopItem> shopItemsMap = ArrayListMultimap.create();
	/** 通行证里领完的奖励,key: 通行证id，购买过的 */
	private Map<Integer, List<Integer>> fundPassRewardsMap = new HashMap<Integer, List<Integer>>();

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

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		List<ShopItem> list = (List<ShopItem>) iterator.next();
		for (ShopItem item : list) {
//			initAddCache(item);
//			itemsMap.put(item.getId(), item);
		}
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
	}

	private void refreshShopNewDay() {
		Collection<ShopConfig> shops = ShopManager.instance().list();
		for (ShopConfig shopConfig : shops) {
			if (shopConfig.Refresh == 1) {
				shopItemsMap.removeAll(shopConfig.ID);
			}
		}
		// 刷新黑市
		int shop = 2;
		List<HeishiConfig> typeList = HeishiManager.instance().getTypeList(1);
		HeishiConfig heishiConfig = typeList.get(0);
		shopItemsMap.put(shop, new ShopItem(heishiConfig.Item));

		typeList = HeishiManager.instance().getTypeList(2);
		List<HeishiConfig> randomWeighableElementsNonRepeating = Rnd.randomWeighableElementsNonRepeating(typeList, GlobalConst.HeishiShelvesCnt - 1);
		for (HeishiConfig heishiConfig2 : randomWeighableElementsNonRepeating) {
			shopItemsMap.put(shop, new ShopItem(heishiConfig2.Item));
		}
		// 刷新金币、钻石商店
		Collection<RechargeStoreConfig> rechargeStore = RechargeStoreManager.instance().list();
		for (RechargeStoreConfig rechargeStoreConfig : rechargeStore) {
			shopItemsMap.put(rechargeStoreConfig.Type, new ShopItem(rechargeStoreConfig.Item));
		}

	}

	private void refreshShopNewWeek() {

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
			break;
		}
		case NewWeek: {
			refreshShopNewWeek();
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
					if (fundPassConfig.Exp) {
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

	@Override
	public void initFromDbAfter() {

	}
}
