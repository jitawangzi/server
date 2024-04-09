package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;

/**
 * 一般是累计条件的计数
 * @mbg.generated
 */
public class ConditionCount implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 条件类型
	 * @mbg.generated
	 */
	private Integer conditionType;
	/**
	 * 计数
	 * @mbg.generated
	 */
	private Integer count;
	/**
	 * 额外参数1
	 * @mbg.generated
	 */
	private Integer arg1;
	/**
	 * 额外参数2
	 * @mbg.generated
	 */
	private Integer arg2;
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
	public Integer getConditionType() {
		return conditionType;
	}

	/**
	 * @mbg.generated
	 */
	public void setConditionType(Integer conditionType) {
		this.conditionType = conditionType;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getCount() {
		return count;
	}

	/**
	 * @mbg.generated
	 */
	public void setCount(Integer count) {
		this.count = count;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getArg1() {
		return arg1;
	}

	/**
	 * @mbg.generated
	 */
	public void setArg1(Integer arg1) {
		this.arg1 = arg1;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getArg2() {
		return arg2;
	}

	/**
	 * @mbg.generated
	 */
	public void setArg2(Integer arg2) {
		this.arg2 = arg2;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.ConditionCountMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, conditionType };
	}
}