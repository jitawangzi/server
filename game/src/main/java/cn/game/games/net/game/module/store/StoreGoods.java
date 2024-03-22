package cn.game.games.net.game.module.store;

import java.io.Serializable;

import cn.game.games.cache.entity.Equip;
import cn.game.protocol.generated.config.CityEquipmentStoreConfig;
import cn.game.protocol.generated.config.CityItemStoreConfig;

/**
 * 商店出售的商品
 *
 */
public class StoreGoods implements Serializable {

	private static final long serialVersionUID = 1L;

	private long uid; // 商品uid
	private int id; // 商店配置表id,可以是ExploreItem表/ExploreEquipment表/store
	private int count; // 商品库存
	private int costId; // 消耗资源id
	private int costCount; // 消耗资源数量
	private Equip equip; // 装备信息(装备商店商品才有这个字段)
	private int lastRefresh; // 上次刷新时间


	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCostId() {
		return costId;
	}

	public void setCostId(int costId) {
		this.costId = costId;
	}

	public int getCostCount() {
		return costCount;
	}

	public void setCostCount(int costCount) {
		this.costCount = costCount;
	}

	public Equip getEquip() {
		return equip;
	}

	public void setEquip(Equip equip) {
		this.equip = equip;
	}
	
	public int getLastRefresh() {
		return lastRefresh;
	}

	public void setLastRefresh(int lastRefresh) {
		this.lastRefresh = lastRefresh;
	}
	
	private static StoreGoods valueOf(long uid, int id, int count, int costId, int costCount) {
		return valueOf(uid, id, count, costId, costCount, 0);
	}

	public static StoreGoods valueOf(long uid, int id, int count, int costId, int costCount, int refreshTime) {
		StoreGoods s = new StoreGoods();
		s.uid = uid;
		s.id = id;
		s.count = count;
		s.costId = costId;
		s.costCount = costCount;
		s.lastRefresh = refreshTime;
		
		return s;
	}


	public static StoreGoods valueOf(CityEquipmentStoreConfig conf, Equip equip) {
		long uid = equip.getId();
		int id = conf.getId();
		int count = 1;
		int costId = conf.getCostGoodsId();
		int costCount = conf.getCostGoodsCount();
		StoreGoods s = valueOf(uid, id, count, costId, costCount);
		s.setEquip(equip);
		
		return s;
	}

	/**
	 * 是否是装备商品
	 * @return
	 */
	public boolean isEquip() {
		return this.equip != null;
	}

	public static StoreGoods valueOf(CityItemStoreConfig e, int num) {
		// 主城道具商店,商品uid和id不会重复
		long uid = e.getId();
		int id = e.getId();
		int count = num;
		int costId = e.getCostGoodsId();
		int costCount = e.getCostGoodsCount();
		
		return valueOf(uid, id, count, costId, costCount, 0);
	}





}
