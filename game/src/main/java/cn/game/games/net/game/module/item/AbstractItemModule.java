package cn.game.games.net.game.module.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.game.games.cache.entity.Item;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.protocol.manual.OpType;

/**    
 * 这里通常处理能重叠的那些东西
 * 或者说一个配置表id只有一个实例的，没有uid的
 * 2024年2月19日 上午10:55:53
 * @author SYQ
 */
public abstract class AbstractItemModule<E extends Item> extends GoodsModule<E> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };

	// itemId => Item
	protected Map<Integer, E> id_items = new HashMap<>();

	/*	@Override
		protected void initFromDb(ListIterator<?> iterator) {
			List<T> list = (List<T>) iterator.next();
			for (T item : list) {
				initAddCache(item);
			}
		}*/
	@Override
	public void initAddCache(E item) {
		addCacheStackable(item);
	}
	@Override
	public void addCacheStackable(E item) {
		id_items.put(item.getConfigId(), item);
	}
	
	@Override
	public void addCacheNoStackable(E item) {
		
	}

	@Override
	public void removeCache(E item) {
		id_items.remove(item.getConfigId());
	}

	/**
	 * 增加一个道具数量
	 */
	@Override
	public Object add(int itemId, int count, OpType opType) {
		if (count <= 0) {
			throw new IllegalArgumentException("count must be greater than 0");
		}
		checkConfig(itemId);
//		ItemConfig itemConfig = ItemManager.instance().get(itemId);
		E item = id_items.get(itemId);
		if (item == null) {
			item = initAdd(itemId, count);
		} else {
			item.setCount(item.getCount() + count);
			item.update();
		}
		// 自动使用
//		if (itemConfig.getIsAutoUse()) {
//			ItemHelper.autoUse(playerId, itemId, count);
//		}
		Item addItem = new Item();
		addItem.setConfigId(itemId);
		addItem.setCount((long) count);
		return addItem;
	}

	/**
	 * 减少指定道具数量
	 */
	@Override
	public boolean del(int itemId, long count, OpType... args) {
		E item = id_items.get(itemId);
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
		E item = id_items.get(itemId);
		if (item == null)
			return 0;
		return item.getCount();
	}

	@Override
	public E get(int itemId) {
		return id_items.get(itemId);
	}


	/** 
	 * 少用
	 * @return
	 */
	public Map<Integer, E> getId_items() {
		return id_items;
	}
	@Override
	public E get(long uid) {
		throw new UnsupportedOperationException("不支持通过uid获取Item");
	}

	@Override
	public Collection<E> list() {
		return id_items.values();
	}

	@Override
	public long genUid() {
		return 0;
	}

}
