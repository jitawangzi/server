package cn.game.games.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.game.core.util.IdUtil;
import cn.game.games.cache.entity.Item;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.ResourceConsumeEnum;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 代表玩家拥有的所有物品
 * @date 2024年2月5日 下午7:18:21
 * @author SYQ
 */
public abstract class GoodsModule<E extends Item, T extends Item> extends BasePlayerModule {

	public abstract long getCount(int configId);

	public abstract Object add(int configId, int count);

	public abstract T newInstance();

	public abstract RewardInfo toRewardInfo(E reward);

	protected Item setInstance(T item, int configId, int count) {
		
		item.setPlayerId(playerId);
		item.setId(IdUtil.getId());
		item.setConfigId(configId);
		item.setType(ItemHelper.getGoodsType(item.getConfigId()));
		item.setCount((long) count);
		item.setCreateTime(new Date());
		return item;
	}

	public void setInstanceAfter(E item) {

	}

	public List<RewardInfo> addReward(int configId, int count) {
		List<RewardInfo> list = new ArrayList<RewardInfo>(1);
		Object object = add(configId, count);
		if (object == null) {
			return list;
		}
		if (object instanceof Item) {
			Item item = (Item) object;
			list.add(toRewardInfo((E) item));
		} else if (object instanceof List) {
			List<Item> items = (List<Item>) object;
			for (Item item : items) {
				list.add(toRewardInfo((E) item));
			}
		}
		return list;
	}


	public void add(int configId) {
		add(configId, 1);
	}

	public abstract boolean del(int configId, int count, ResourceConsumeEnum... args);

	public abstract boolean del(long uid, ResourceConsumeEnum... args);

	public boolean isEnough(int configId, int count) {
		if (count <= 0) {
			return true;
		}
		return getCount(configId) >= count;
	}

	public boolean isEnough(int configId) {
		return getCount(configId) >= 1;
	}

	public abstract T get(int configId);

	public abstract T get(long uid);

	/** 
	 * 这个模块处理的物品类型
	 * @return
	 */
	public abstract GoodsTypeEnum getGoodsTypeEnum();

	public abstract void initAddCache(E item);
	
	public abstract void addCacheStackable(E item);
	public abstract void addCacheNoStackable(E item);

	public abstract void removeCache(E item);

	public Collection<E> list() {
		return Collections.EMPTY_LIST;
	}
	/**
	 *物品模块优先级相对较高
	 */
	@Override
	protected int getInitOrder() {
		return INIT_PRIORITY_HIGH;
	}
}
