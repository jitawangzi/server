package cn.game.games.net.game.module.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.game.games.cache.entity.Item;
import cn.game.games.cache.entity.ItemOnlyOne;
import cn.game.games.core.GoodsModule;
import cn.game.protocol.manual.OpType;

/**    
 * 这里处理那种不能重复获取的物品-----一个配置表id只有一个实例，数量始终为1
 * 重复获取时，可能转换成某种资源，或者有持续时间的，延长持续时间
 * 2025年6月30日 16:59:37
 * @author SYQ
 * @param <E>
 */
public abstract class AbstractItemOnlyOneModule<E extends ItemOnlyOne> extends GoodsModule<E>
{

	// itemId => Item
	protected Map<Integer, E> idItems = new HashMap<>();
	
	@Override
	public void initAddCache(E item) {
		idItems.put(item.getConfigId(), item);
	}

	@Override
	public void removeCache(E item) {
		idItems.remove(item.getConfigId(), item);
	}

	@Override
	public void addCacheStackable(E item) {

	}

	@Override
	public void addCacheNoStackable(E item) {

	}

	@Override
	public Object add(int itemId, int count, OpType opType) {
		if (count <= 0) {
			throw new IllegalArgumentException("count must be greater than 0");
		}
		if (count > 1) {
			count = 1;
		}
		checkConfig(itemId);

		if (getCount(itemId) > 0) {
			return addRepeated(itemId);
		}

		E item = initAdd(itemId, count);
		return item;
	}

	/** 
	 * 重复添加时的处理逻辑
	 * @param itemId
	 * @return
	 */
	public abstract Item addRepeated(int itemId);

	@Override
	public boolean del(int itemId, long count, OpType... args) {
		return idItems.remove(itemId) != null;
	}

	@Override
	public boolean del(long uid, OpType... args) {
		throw new UnsupportedOperationException("不支持通过uid删除不能重叠的物品");
	}

	@Override
	public long getCount(int itemId) {
		E item = idItems.get(itemId);
		return item == null ? 0 : item.getCount();
	}

	@Override
	public E get(long uid) {
		throw new UnsupportedOperationException("不支持通过uid获取物品");
	}

	@Override
	public Collection<E> list() {
		return idItems.values();
	}

	@Override
	public E get(int itemId) {
		return idItems.get(itemId);
	}

	/** 
	 * 获取 配置表id--对象 的映射
	 * @return
	 */
	public Map<Integer, E> getIdItems() {
		return idItems;
	}

	@Override
	public long genUid() {
		return 0;
	}

}
