package cn.game.games.net.game.module.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.protocol.manual.ResourceConsumeEnum;

/**    
 * 这里通常处理能重叠的那些东西
 * @date 2024年2月19日 上午10:55:53
 * @author SYQ
 */
public abstract class AbstractItemModule<T extends Item> extends GoodsModule<T, Item> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };

	// itemId => Item
	protected Map<Integer, T> id_items = new HashMap<>();

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		List<T> list = (List<T>) iterator.next();
		for (T item : list) {
			initAddCache(item);
		}
	}
	@Override
	public void initAddCache(T item) {
		addCacheStackable(item);
	}
	@Override
	public  void addCacheStackable(T item) {
		id_items.put(item.getConfigId(), item);
	}
	
	@Override
	public  void addCacheNoStackable(T item) {
		
	}

	@Override
	public void removeCache(T item) {
		id_items.remove(item.getConfigId());
	}

	/**
	 * 增加一个道具数量
	 */
	@Override
	public T add(int itemId, int count) {
		if (count <= 0) {
			return null;
		}
		ItemHelper.checkConfig(itemId);
//		ItemConfig itemConfig = ItemManager.instance().get(itemId);
		T item = id_items.get(itemId);
		if (item == null) {
			item = (T) newInstance();
			setInstance(item, itemId, count);
			setInstanceAfter(item);
			item.insert();
			initAddCache(item);
		} else {
			item.setCount(item.getCount() + count);
			item.update();
		}
		// 自动使用
//		if (itemConfig.getIsAutoUse()) {
//			ItemHelper.autoUse(playerId, itemId, count);
//		}
		return item;
	}

	/**
	 * 减少指定道具数量
	 */
	@Override
	public boolean del(int itemId, int count, ResourceConsumeEnum... args) {
		T item = id_items.get(itemId);
		if (item == null)
			return false;
		if (item.getCount() < count) {
			return false;
		}
		item.setCount(item.getCount() - count);
		if (item.getCount() == 0) {
			removeCache(item);
			item.delete();
		} else {
			item.update();
		}
		return true;
	}

	@Override
	public boolean del(long uid, ResourceConsumeEnum... args) {
		throw new UnsupportedOperationException("不支持通过uid删除");
	}

	/**
	 * 获取指定道具数量
	 * 
	 * @param itemId 道具id
	 */
	@Override
	public long getCount(int itemId) {
		T item = id_items.get(itemId);
		if (item == null)
			return 0;
		return item.getCount();
	}

	@Override
	public T get(int itemId) {
		return id_items.get(itemId);
	}

	@Override
	public T get(long uid) {
		throw new UnsupportedOperationException("不支持通过uid获取Item");
	}

	@Override
	public Collection<T> list() {
		return id_items.values();
	}

}
