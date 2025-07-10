package cn.game.games.cache.entity;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;
import cn.game.protocol.protobuf.BaseMsg.ItemInfo;

public class Item implements Serializable, DbEntity {

	protected long id;
	protected long playerId;
	protected int configId;
	/**
	 * 道具类型,
	 */
	protected Integer type;
	protected Long count;
	/**
	 * 创建时间
	 */
	protected long createTimeMillis;

	/** 过期时间（秒时间戳）  */
	protected int expiredTime;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getConfigId() {
		return configId;
	}

	public void setConfigId(int configId) {
		this.configId = configId;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public long getCreateTimeMillis() {
		return createTimeMillis;
	}

	public void setCreateTimeMillis(long createTimeMillis) {
		this.createTimeMillis = createTimeMillis;
	}

	public int getExpiredTime() {
		return expiredTime;
	}

	public void setExpiredTime(int expiredTime) {
		this.expiredTime = expiredTime;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.ItemMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

	public ItemInfo toItemInfo() {
		return ItemInfo.newBuilder().setId(this.configId).setCount(this.count.intValue()).build();
	}
}