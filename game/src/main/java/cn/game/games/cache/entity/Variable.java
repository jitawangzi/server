package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;

public class Variable implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 变量id，类型
	 * @mbg.generated
	 */
	private Integer type;
	/**
	 * 变量值
	 * @mbg.generated
	 */
	private Integer value;
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
	public Integer getValue() {
		return value;
	}

	/**
	 * @mbg.generated
	 */
	public void setValue(Integer value) {
		this.value = value;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.VariableMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, type };
	}
}