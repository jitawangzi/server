package cn.game.games.net.game.module.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import cn.game.games.cache.entity.ItemNoStack;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.protocol.manual.OpType;

/**    
 * 这里通常处理不能重叠的那些东西
 * 2024年2月19日 上午10:55:53
 * @author SYQ
 */
public abstract class AbstractItemNoStackModule<T extends ItemNoStack> extends GoodsModule<T, ItemNoStack>
{
	// uid => T
	protected Map<Long, T> uid_items = new HashMap<>();
	// configId => List<T> ,通常用来判断有没有某种东西
	@JsonIgnore
	protected Multimap<Integer, T> id_items = ArrayListMultimap.create();

	/*	@Override
		protected void initFromDb(ListIterator<?> iterator) {
			List<T> list = (List<T>) iterator.next();
			for (T item : list) {
				addCacheNoStackable(item);
			}
		}*/
	@Override
	public void initFromDbAfter() {
		for (T item : uid_items.values()) {
			addCacheStackable(item);
		}
	};
	
	@Override
	public void initAddCache(T item) {
		addCacheNoStackable(item);
		addCacheStackable(item);
	}
	@Override
	public  void addCacheNoStackable(T item) {
		uid_items.put(item.getId(), item);
	}
	@Override
	public  void addCacheStackable(T item) {
		id_items.put(item.getConfigId(), item);
	}

	@Override
	public void removeCache(T item) {
		id_items.remove(item.getConfigId(), item);
		uid_items.remove(item.getId());
	}

	@Override
	public List<T> add(int itemId, int count, OpType opType) {
		if (count <= 0) {
			return null;
		}
		checkConfig(itemId);
		// TODO 检查id，是不是存在，涉及到多个表。
		List<T> ret = new ArrayList<T>();
		for (int i = 0; i < count; i++) {
			T item = (T) newInstance();
			setInstance(item, itemId, 1);
			setInstanceAfter(item);
			initAddCache(item);
			item.insert();
			ret.add(item);
		}
		return ret;
	}

	@Override
	public boolean del(int itemId, int count, OpType... args) {
		throw new UnsupportedOperationException("不支持通过配置表id删除不能重叠的物品");
	}

	@Override
	public boolean del(long uid, OpType... args) {

		T item = uid_items.get(uid);
		if (item == null)
			return false;
		removeCache(item);
		item.delete();
		player.handleEvent(EventTypeEnum.CostUidItem, uid, item.getConfigId());
		return true;
	}

	@Override
	public long getCount(int itemId) {
		Collection<T> item = id_items.get(itemId);
		return item == null ? 0 : item.size();
	}

	@Override
	public T get(long uid) {
		return uid_items.get(uid);
	}

	@Override
	public Collection<T> list() {
		return id_items.values();
	}

	@Override
	public T get(int itemId) {
		throw new UnsupportedOperationException("不支持通过配置表id获取不能重叠的物体");
	}

	public Collection<T> getByConfigId(int configId) {
		return id_items.get(configId);
	}

	public int getSizeDeduplication() {
		return id_items.keys().size();
	}
}
