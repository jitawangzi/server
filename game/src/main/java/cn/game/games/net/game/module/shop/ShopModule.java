package cn.game.games.net.game.module.shop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import cn.game.core.util.IdUtil;
import cn.game.games.cache.entity.ShopItem;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.ShopItemMapper;
import cn.game.protocol.generated.config.ShopItemConfig;
import cn.game.protocol.generated.config.ShopItemGroupConfig;
import cn.game.protocol.generated.manager.ShopItemGroupManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.DateUtil;
import cn.game.util.Rnd;

public class ShopModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };

	private Map<Long, ShopItem> itemsMap = new HashMap<Long, ShopItem>();
	
	@JsonIgnore
	private Multimap<Integer, ShopItem> groupItemsMap = ArrayListMultimap.create();

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class<?>[] { ShopItemMapper.class };
	}

	private void initAddCache(ShopItem item) {
		itemsMap.put(item.getId(), item);
		groupItemsMap.put(item.getGroupId(), item);
	}

	private void removeCache(ShopItem item) {
		itemsMap.remove(item.getId());
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		List<ShopItem> list = (List<ShopItem>) iterator.next();
		for (ShopItem item : list) {
//			initAddCache(item);
			itemsMap.put(item.getId(), item);
		}
	}
	
	@Override
	public void initFromDbAfter() {
		for (ShopItem item : itemsMap.values()) {
			groupItemsMap.put(item.getGroupId(), item);
		}
	};

	public Collection<ShopItem> getShopItems(int group) {
		return groupItemsMap.get(group);
	}
//	public Collection<ShopItem> getGroupCreateDay(int group) {
//		return groupItemsMap.get(group);
//	}

	public ShopItem getShopItem(long uid) {
		return itemsMap.get(uid);
	}

	public void initShop() {
		int day = DateUtil.getDay();
		Collection<ShopItemGroupConfig> list = ShopItemGroupManager.instance().list(); 
		for (ShopItemGroupConfig config : list) {
			initShopItemGroup(day, config);
		}
		
	}

	private void initShopItemGroup(int day, ShopItemGroupConfig config) {
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
	}

	private void refreshShop() {
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
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {

	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		// 正常应该是在功能开启时初始化商店。
		case PLAYER_CREATE: {
			initShop();
		}
		case NewDay: {
			refreshShop();
		}

		}
	}
}
