package cn.game.games.cache.op.face;

import java.util.Collection;
import java.util.List;

import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.games.net.game.module.award.RewardItem;
import cn.game.games.cache.entity.Store;
import cn.game.games.cache.entity.StoreData;
import cn.game.protocol.protobuf.StoreMsg.StoreGoodsInfo;
import cn.game.protocol.protobuf.StoreMsg.StoreType;
import cn.game.util.Pair;

public interface IStoreOp {
	
	/**
	 * 生成探索局间商店
	 * @param exploreLevelId 当前探索区域
	 * @return
	 */
	public boolean genExploreRoomStores(int exploreLevelId);
	
	/**
	 * 删除探索局间商店
	 * @return
	 */
	public boolean delExploreRoomStores();
	
	/**
	 * 查看商店商品
	 * @param type
	 * @return
	 */
	public Collection<StoreGoodsInfo> seeStore(StoreType type);
	
	/**
	 * 购物
	 * @param uid
	 * @param count
	 * @return 
	 */
	public Pair<ErrorMsgEnum, RewardItem> buy(long uid, int count);

	/**
	 * 持久化商店数据
	 * @param types {@link StoreType}
	 */
	public void saveStore(StoreType[] types);

	void insertStoreData();

	void initLoadData(List<Store> stores, StoreData data);

	

}
