package cn.game.games.net.game.module.backpack;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

/**

背包模块
*/
public class BackpackModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };

	private BackpackSystem backpackSystem;

	@Override
	public void init() {
		super.init();
		backpackSystem = new BackpackSystem(playerId);
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case PLAYER_CREATE: {
// 玩家创建时的初始化逻辑
			break;
		}
		case NewDay: {
// 每日重置逻辑
			break;
		}
		default:
			break;
		}
	}

	/**
	
	获取背包系统
	*/
	public BackpackSystem getBackpackSystem() {
		return backpackSystem;
	}

	/**
	
	获取指定类型的背包
	*/
	public Backpack getBackpack(BackpackType type) {
		return backpackSystem.getBackpack(type);
	}

	/**
	
	添加物品
	*/
	public boolean addItem(Item item, OpType opType) {
		return backpackSystem.addItem(item, opType);
	}

	/**
	
	移除物品
	*/
	public boolean removeItem(BackpackType type, int slot, int count, OpType opType) {
		return backpackSystem.removeItem(type, slot, count, opType);
	}

	/**
	
	移动物品
	*/
	public boolean moveItem(BackpackType type, int fromSlot, int toSlot) {
		return backpackSystem.moveItem(type, fromSlot, toSlot);
	}

	/**
	
	交换物品
	*/
	public boolean swapItems(BackpackType type, int slot1, int slot2) {
		return backpackSystem.swapItems(type, slot1, slot2);
	}

	/**
	
	拆分物品
	*/
	public boolean splitItem(BackpackType type, int slot, int count) {
		return backpackSystem.splitItem(type, slot, count);
	}

	/**
	
	合并物品
	*/
	public boolean mergeItems(BackpackType type, int sourceSlot, int targetSlot) {
		return backpackSystem.mergeItems(type, sourceSlot, targetSlot);
	}

	/**
	
	锁定物品
	*/
	public boolean lockItem(BackpackType type, int slot) {
		return backpackSystem.lockItem(type, slot);
	}

	/**
	
	解锁物品
	*/
	public boolean unlockItem(BackpackType type, int slot) {
		return backpackSystem.unlockItem(type, slot);
	}

	/**
	
	扩展背包容量
	*/
	public boolean expandBackpack(BackpackType type, int additionalSlots) {
		return backpackSystem.expandBackpack(type, additionalSlots);
	}

	/**
	
	整理背包
	*/
	public void sortBackpack(BackpackType type) {
		backpackSystem.sortBackpack(type);
	}

	/**
	
	整理所有背包
	*/
	public void sortAllBackpacks() {
		backpackSystem.sortAllBackpacks();
	}

	/**
	
	检查背包是否有足够空间容纳指定物品
	*/
	public boolean hasEnoughSpace(BackpackType type, int configId, int count) {
		return backpackSystem.hasEnoughSpace(type, configId, count);
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
// 这里需要根据具体协议构建背包信息
// 背包模块不需要构建物品信息，因为这已经由BackpackGoodsModule处理
	}


	/**
	
	背包模块优先级相对较高
	*/
	@Override
	protected int getInitOrder() {
		return INIT_PRIORITY_HIGH;
	}
}
