package cn.game.games.net.game.module.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import cn.game.games.cache.entity.ItemNoStack;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.util.DAO;
import cn.game.protocol.manual.OpType;

/**    
 * 这里通常处理不能重叠的那些东西
 * 2024年2月19日 上午10:55:53
 * @author SYQ
 */
public abstract class AbstractItemNoStackModule<E extends ItemNoStack> extends GoodsModule<E>
{
	// uid => T
	protected Map<Long, E> uid_items = new HashMap<>();
	// configId => List<T> ,通常用来判断有没有某种东西
	@JsonIgnore
	protected transient Multimap<Integer, E> id_items = HashMultimap.create();

	/*	@Override
		protected void initFromDb(ListIterator<?> iterator) {
			List<T> list = (List<T>) iterator.next();
			for (T item : list) {
				addCacheNoStackable(item);
			}
		}*/
	@Override
	public void initFromDbAfter() {
		for (E item : uid_items.values()) {
			id_items.put(item.getConfigId(), item);
		}
	};
	
	@Override
	public void initAddCache(E item) {
		uid_items.put(item.getId(), item);
		id_items.put(item.getConfigId(), item);
	}

	@Override
	public E removeFromCache(int id) {
		throw new UnsupportedOperationException("不支持通过配置表id删除不能重叠的物品");
	}

	@Override
	public E removeFromCache(long id) {
		E remove = uid_items.remove(id);
		id_items.remove(remove.getConfigId(), remove) ; 
		return remove; 
	}

	@Override
	public Object add(int itemId, int count, OpType opType) {
		if (count <= 0) {
			throw new IllegalArgumentException("count must be greater than 0");
		}
		checkConfig(itemId);
		// TODO 检查id，是不是存在，涉及到多个表。
		List<E> ret = new ArrayList<E>();
		for (int i = 0; i < count; i++) {
			E item = initAdd(itemId, 1);
			ret.add(item);
		}
		return ret;
	}

	@Override
	public boolean del(int itemId, long count, OpType... args) {
		throw new UnsupportedOperationException("不支持通过配置表id删除不能重叠的物品");
	}

	@Override
	public boolean del(long uid, OpType... args) {

		return del(uid, true, args) != null;
	}

	private E del(long uid, boolean updateDb, OpType... args) {

		E item = uid_items.get(uid);
		if (item == null)
			return null;
		removeFromCache(item.getId());

		if (updateDb && alwaysStoreDataInStandaloneTable()) {
			item.delete();
		}
		player.fireAndHandleEvent(EventTypeEnum.CostUidItem, uid, item.getConfigId());
		return item;
	}

	public boolean delBatch(List<Long> uidList, OpType... opType) {
		if (uidList == null || uidList.isEmpty()) {
			return true;
		}
		List<E> items = new ArrayList<>();
		for (long id : uidList) {
			E item = del(id, false, opType);
			if (item != null) {
				items.add(item);
			}
		}
		if (items.isEmpty()) {
			return false;
		}
		if (alwaysStoreDataInStandaloneTable()) {
			DAO.deleteBatch(items.get(0).getMapperClass(), items);
		}
		return true;
	}

	@Override
	public long getCount(int itemId) {
		Collection<E> item = id_items.get(itemId);
		return item == null ? 0 : item.size();
	}

	@Override
	public E get(long uid) {
		return uid_items.get(uid);
	}

	@Override
	public Collection<E> list() {
		return uid_items.values();
	}

	@Override
	public E get(int itemId) {
		throw new UnsupportedOperationException("不支持通过配置表id获取不能重叠的物体");
	}

	public Collection<E> getByConfigId(int configId) {
		return id_items.get(configId);
	}

	/** 
	 * 获取 配置表id--对象集合 的映射
	 * @return
	 */
	public Multimap<Integer, E> getId_items() {
		return id_items;
	}

	public int getSizeDeduplication() {
		return id_items.keys().size();
	}

	public Map<Long, E> getUid_items() {
		return uid_items;
	}

}
