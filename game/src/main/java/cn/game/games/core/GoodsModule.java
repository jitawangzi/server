package cn.game.games.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import cn.game.core.id.IdUtil;
import cn.game.games.cache.entity.Item;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.module.award.RewardHelper;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

/**    
 * 代表玩家拥有的所有物品
 * 2024年2月5日 下午7:18:21
 * @author SYQ
 */
public abstract class GoodsModule<E extends Item> extends BasePlayerModule {

	public abstract long getCount(int configId);

	/** 
	 * 是否有某种东西
	 * @param configId
	 * @return
	 */
	public boolean has(int configId) {
		return getCount(configId) > 0;
	}

	/** 
	 * 返回新增的物品，注意可重叠的物品。 
	 * 新增的物品，一般是做显示用的，不要直接用于逻辑
	 * @param configId
	 * @param count
	 * @param opType
	 * @return  只能返回Item类型，或者List<Item>、List<List<Item>>类型。 
	 */
	public abstract Object add(int configId, int count, OpType opType);

	/** 
	 * 检查配置表id是否合法。 
	 * @param id
	 */
	protected abstract void checkConfig(int id);

	protected abstract E newInstance();

	/** 
	 * 初始化指定的物品流程
	 * @param configId
	 * @param count
	 * @return
	 */
	protected E initAdd(int configId, int count) {

		E item = newInstance();
		setInstance(item, configId, count);
		setInstanceExt(item);
		initAddCache(item);
		if (alwaysStoreDataInStandaloneTable()) {
			item.insert();
		}
		return item;
	}

	protected long genUid() {
		return IdUtil.getId();
	}

	/** 
	 * 类型转换问题，暂时废弃了
	 * @param reward
	 * @return
	 */
	@Deprecated
	public RewardInfo toRewardInfo(E reward) {
		return null;
	};

	protected Item setInstance(E item, int configId, int count) {
		
		item.setPlayerId(playerId);
		item.setId(genUid());
		item.setConfigId(configId);
		item.setType(ItemHelper.getGoodsType(item.getConfigId()));
		item.setCount((long) count);
		item.setCreateTimeMillis(System.currentTimeMillis());
		return item;
	}

	public void setInstanceExt(E item) {

	}

	/** 
	 * 给物品设置过期时间
	 * @param expiredSeconds
	 */
	public void expired(Item item, int expiredSeconds) {
		if (expiredSeconds <= 0) {
			return ; // 不过期
		}
		if (item.getExpiredTime() > 0) { // 已有过期时间的，叠加过期时间
			item.setExpiredTime(item.getExpiredTime() + expiredSeconds);
		}else {
			item.setExpiredTime(DateUtil.currentTimeSeconds() + expiredSeconds);
		}
		if (item.getExpiredTimeTask() > 0) {
			player.cancelTimer(item.getExpiredTimeTask());
		}
//		物品过期
		long expiredTimeTask =  player.setTimerTask((item.getExpiredTime() - DateUtil.currentTimeSeconds()) * 1000, r -> {
			delItem(item, OpType.Expired);
		});
		item.setExpiredTimeTask(expiredTimeTask);
	}

	/** 
	 * 增加物品
	 * @param configId
	 * @param count
	 * @param opType
	 * @return  物品的proto类型
	 */
	public List<RewardInfo> addReward(int configId, int count, OpType opType) {
		Object object = add(configId, count, opType);
		return RewardHelper.toRewardList(object);
	}

	public Object add(int configId, OpType opType) {
		return add(configId, 1, opType);
	}

	public abstract boolean del(int configId, long count, OpType... args);

	public abstract boolean del(long uid, OpType... args);
	
	public boolean delItem(Item item, OpType... args) {
		if (item.getId() > 0) {
			return del(item.getId(), args);
		}
		boolean ret =  del(item.getConfigId(), item.getCount(), args) ; 
		if (ret) {
			delItemAfter(item, args);
		}
		return ret; 
	}
	public void delItemAfter(Item item, OpType... args) {
	}

	public boolean isEnough(int configId, int count) {
		if (count < 0) {// 非法数据
			return false; 
		}
		if (count == 0) { // 业务合法数据，免费
			return true;
		}
		return getCount(configId) >= count;
	}

	public boolean isEnough(int configId) {
		return getCount(configId) >= 1;
	}

	public abstract E get(int configId);

	public abstract E get(long uid);

	/** 
	 * 这个模块处理的物品类型
	 * @return
	 */
	public abstract GoodsTypeEnum getGoodsTypeEnum();

	public abstract void initAddCache(E item);
	
	public abstract E removeFromCache(int id);

	public abstract E removeFromCache(long id);

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
