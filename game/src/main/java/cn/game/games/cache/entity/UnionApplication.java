package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class UnionApplication implements Serializable, DbEntity {

	/**
	 * 被申请人id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 申请的工会id
	 * @mbg.generated
	 */
	private Long unionId;
	/**
	 * @mbg.generated
	 */
	private Long createTime;
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
	public Long getUnionId() {
		return unionId;
	}

	/**
	 * @mbg.generated
	 */
	public void setUnionId(Long unionId) {
		this.unionId = unionId;
	}

	/**
	 * @mbg.generated
	 */
	public Long getCreateTime() {
		return createTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.UnionApplicationMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, unionId };
	}
}