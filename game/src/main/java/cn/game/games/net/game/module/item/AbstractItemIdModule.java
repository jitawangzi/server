package cn.game.games.net.game.module.item;

import java.util.Set;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.player.IdConstant;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.protocol.manual.OpType;

/**    
 * 通常只有一个id，并且这个id不能重复，重复获取时会转成其他的资源。 
 * 2025年1月10日 14:18:34
 * @author SYQ
 * @param <T>
 */
public abstract class AbstractItemIdModule<T extends Item> extends GoodsModule<T, Item> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };

	/**
	 * 增加一个道具数量
	 */
	@Override
	public Object add(int itemId, int count, OpType opType) {
		if (count <= 0) {
			return null;
		}
		if (count != 1) {
			throw new UnsupportedOperationException("不支持count != 1");
		}
		checkConfig(itemId);
		PlayerModule playerModule = player.getPlayerModule();
		if (playerModule.hasId(getIdType(), itemId)) {
			return addRepeated(itemId);
		}
		Item addItem = newInstance();
		addItem.setConfigId(itemId);
		addItem.setCount((long) count);
		playerModule.addId(getIdType(), itemId);
		return addItem;
	}

	/** 
	 * 获取id类型
	 * @return
	 */
	public abstract IdConstant getIdType();

	/** 
	 * 重复获取时，转化为某种资源
	 * @return
	 */
	public abstract Object addRepeated(int itemId);

	/**
	 * 减少指定道具数量
	 */
	@Override
	public boolean del(int itemId, long count, OpType... args) {
		throw new UnsupportedOperationException("不支持del删除: " + itemId);

	}

	@Override
	public boolean del(long uid, OpType... args) {
		throw new UnsupportedOperationException("不支持通过uid删除");
	}

	/**
	 * 获取指定道具数量
	 * 
	 * @param itemId 道具id
	 */
	@Override
	public long getCount(int itemId) {
		Set<Integer> idsSet = player.getPlayerModule().getIdsSet(getIdType());
		return idsSet.contains(itemId) ? 1 : 0;
	}

	@Override
	public T get(int itemId) {
		throw new UnsupportedOperationException("不支持通过id获取Item");
	}
	@Override
	public T get(long uid) {
		throw new UnsupportedOperationException("不支持通过uid获取Item");
	}

	@Override
	public void initAddCache(T item) {
	}

	@Override
	public void addCacheStackable(T item) {
	}

	@Override
	public void addCacheNoStackable(T item) {

	}

	@Override
	public void removeCache(T item) {
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {

	}
}
