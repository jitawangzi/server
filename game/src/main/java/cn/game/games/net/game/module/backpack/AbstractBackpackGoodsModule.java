package cn.game.games.net.game.module.backpack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**

基于格子背包的物品模块

此模块使用格子背包系统管理背包类型的物品
*/
public abstract class AbstractBackpackGoodsModule<E extends Item> extends GoodsModule<E> {
	private Backpack backpack;

	@Override
	public void init() {
		super.init();
		// 确保BackpackSystem已经初始化
//		backpackSystem = ((BackpackModule) (player.getModule(BackpackModule.class))).getBackpackSystem();
//		if (backpackSystem == null) {
//			throw new IllegalStateException("BackpackSystem not initialized");
//		}
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case PLAYER_CREATE:
			// 初始化逻辑
			break;
		case NewDay:
			// 每日重置逻辑
			break;
		default:
			break;
		}
	}

	@Override
	public long getCount(int configId) {
		// 根据物品ID确定背包类型
//		BackpackType backpackType = getBackpackTypeForItem(configId);
//		Backpack backpack = backpackSystem.getBackpack(backpackType);
		long count = 0;

		for (Item item : backpack.getAllItems()) {
			if (item.getConfigId() == configId) {
				count += item.getCount();
			}
		}
		return count;
	}

	@Override
	public Object add(int configId, int count, OpType opType) {
		if (count <= 0) {
			throw new IllegalArgumentException("count must be greater than 0");
		}

		checkConfig(configId);

		// 创建临时Item对象
		Item tempItem = newInstance();
		tempItem.setPlayerId(playerId);
		tempItem.setId(genUid());
		tempItem.setConfigId(configId);
		tempItem.setType(ItemHelper.getGoodsType(configId));
		tempItem.setCount((long) count);
		tempItem.setCreateTimeMillis(System.currentTimeMillis());

		// 尝试添加到背包
		boolean success = backpack.addItem(tempItem, opType);

		if (!success) {
			// 背包已满，无法添加
			return null;
		}

		return tempItem;
	}

	@Override
	public boolean del(int configId, long count, OpType... args) {
		// 根据物品ID确定背包类型
//		BackpackType backpackType = getBackpackTypeForItem(configId);
//		Backpack backpack = backpackSystem.getBackpack(backpackType);
		long remainingCount = count;

		// 找到所有匹配的物品
		List<Integer> slotsToRemove = new ArrayList<>();
		for (int i = 0; i < backpack.getCapacity(); i++) {
			Item item = backpack.getItemBySlot(i);
			if (item != null && item.getConfigId() == configId) {
				slotsToRemove.add(i);
				if (item.getCount() >= remainingCount) {
					break;
				}
				remainingCount -= item.getCount();
			}
		}

		// 如果找不到足够的物品，返回失败
		if (remainingCount > 0) {
			return false;
		}

		// 从背包中移除物品
		remainingCount = count;
		for (int slot : slotsToRemove) {
			Item item = backpack.getItemBySlot(slot);
			if (item.getCount() <= remainingCount) {
				// 移除整个格子的物品
				backpack.removeItem(slot, (int) item.getCount(), args.length > 0 ? args[0] : OpType.None);
				remainingCount -= item.getCount();
			} else {
				// 部分移除
				backpack.removeItem(slot, (int) remainingCount, args.length > 0 ? args[0] : OpType.None);
				remainingCount = 0;
			}

			if (remainingCount == 0) {
				break;
			}
		}

		return true;
	}

	@Override
	public boolean del(long uid, OpType... args) {
		// 遍历所有背包类型查找物品
		for (BackpackType backpackType : BackpackType.values()) {
//			Backpack backpack = backpackSystem.getBackpack(backpackType);

			for (int i = 0; i < backpack.getCapacity(); i++) {
				Item item = backpack.getItemBySlot(i);
				if (item != null && item.getId() == uid) {
					return backpack.removeItem(i, (int) item.getCount(), args.length > 0 ? args[0] : OpType.None);
				}
			}
		}

		return false;
	}

	@Override
	public E get(int configId) {
		// 根据物品ID确定背包类型
//		BackpackType backpackType = getBackpackTypeForItem(configId);
//		Backpack backpack = backpackSystem.getBackpack(backpackType);

		for (int i = 0; i < backpack.getCapacity(); i++) {
			Item item = backpack.getItemBySlot(i);
			if (item != null && item.getConfigId() == configId) {
				return (E) item;
			}
		}

		return null;
	}

	@Override
	public E get(long uid) {
		// 遍历所有背包类型查找物品
//		for (BackpackType backpackType : BackpackType.values()) {
//			Backpack backpack = backpackSystem.getBackpack(backpackType);
			for (int i = 0; i < backpack.getCapacity(); i++) {
				Item item = backpack.getItemBySlot(i);
				if (item != null && item.getId() == uid) {
					return (E) item;
				}
			}
//		}

		return null;
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		// 由于这个模块处理所有背包类型的物品，我们需要一个通用类型
		// 或者根据具体情况返回
		return GoodsTypeEnum.Item; // 默认返回Item类型
	}

	@Override
	public void initAddCache(Item item) {
		// 对于格子背包，我们不使用缓存，因为物品存储在格子中
	}

	@Override
	public E removeFromCache(int id) {
		return null;
	}

	@Override
	public E removeFromCache(long id) {
		return null;
	}

	@Override
	public Collection<E> list() {
//		List<Item> allItems = new ArrayList<>();

		// 收集所有背包中的物品
//		for (BackpackType backpackType : BackpackType.values()) {
//			Backpack backpack = backpackSystem.getBackpack(backpackType);
//			allItems.addAll(backpack.getAllItems());
//		}

//		return allItems;
		return (Collection<E>) backpack.getAllItems();
	}

	@Override
	public RewardInfo toRewardInfo(Item item) {
		return RewardInfo.newBuilder().setItem(item.toItemInfo()).build();
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// 将所有背包中的物品添加到玩家信息中
	}

	@Override
	public void checkConfig(int id) {
// 根据物品类型调用不同的配置检查
		/*		GoodsTypeEnum goodsType = ItemHelper.getGoodsTypeEnum(id);
				switch (goodsType) {
				case Equipment:
		// 检查装备配置
					break;
				case Item:
		// 检查物品配置
					break;
				default:
		// 默认检查
					break;
				}*/
	}

	/**
	
	根据物品ID确定背包类型
	*/
	protected BackpackType getBackpackTypeForItem(int configId) {
		int goodsType = ItemHelper.getGoodsType(configId);

		if (goodsType == GoodsTypeEnum.Equipment.getId()) {
			return BackpackType.EQUIPMENT;
		} else if (goodsType == GoodsTypeEnum.Item.getId()) {
			return BackpackType.MATERIAL;
		} else {
			// 默认放入材料背包
			return BackpackType.MATERIAL;
		}
	}

	/**
	
	检查背包是否有足够空间容纳指定物品
	*/
	public boolean hasEnoughSpace(int configId, int count) {
		return backpack.hasEnoughSpace(configId, count);
	}
}