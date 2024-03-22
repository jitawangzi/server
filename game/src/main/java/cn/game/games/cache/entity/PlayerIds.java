package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;
import java.util.Date;

public class PlayerIds implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * @mbg.generated
	 */
	private Integer type;
	/**
	 * @mbg.generated
	 */
	private Integer configId;
	/**
	 * 更新时间，秒时间戳
	 * @mbg.generated
	 */
	private Long updateTime;
	/**
	 * @mbg.generated
	 */
	private Date createTime;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

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
	public Long getUpdateTime() {
		return updateTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
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

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.PlayerIdsMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, type, configId };
	}
}