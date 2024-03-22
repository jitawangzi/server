package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class BattleLevel implements Serializable, DbEntity {

	/**
	 * 角色ID
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 关卡id
	 * @mbg.generated
	 */
	private Integer levelId;
	/**
	 * 通关星数
	 * @mbg.generated
	 */
	private Integer star;
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
	public Integer getLevelId() {
		return levelId;
	}

	/**
	 * @mbg.generated
	 */
	public void setLevelId(Integer levelId) {
		this.levelId = levelId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getStar() {
		return star;
	}

	/**
	 * @mbg.generated
	 */
	public void setStar(Integer star) {
		this.star = star;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.BattleLevelMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, levelId };
	}
}