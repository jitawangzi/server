package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;

import cn.game.games.cache.base.DbEntity;
import cn.game.protocol.protobuf.BaseMsg.ItemInfo;

public class Item implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	protected Long id;
	/**
	 * @mbg.generated
	 */
	protected Long playerId;
	/**
	 * @mbg.generated
	 */
	protected Integer configId;
	/**
	 * 道具类型,
	 * @mbg.generated
	 */
	protected Integer type;
	/**
	 * @mbg.generated
	 */
	protected Long count;
	/**
	 * 创建时间
	 * @mbg.generated
	 */
	protected Date createTime;

	/** 过期时间（秒时间戳）  */
	protected int expiredTime;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @mbg.generated
	 */
	public Long getPlayerId() {
		return playerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getConfigId() {
		return configId;
	}

	/**
	 * @mbg.generated
	 */
	public void setConfigId(Integer configId) {
		this.configId = configId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * @mbg.generated
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * @mbg.generated
	 */
	public Long getCount() {
		return count;
	}

	/**
	 * @mbg.generated
	 */
	public void setCount(Long count) {
		this.count = count;
	}

	/**
	 * @mbg.generated
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
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

	public ItemInfo toProto() {
		return ItemInfo.newBuilder().setId(this.configId).setCount(this.count.intValue()).build();
	}
}