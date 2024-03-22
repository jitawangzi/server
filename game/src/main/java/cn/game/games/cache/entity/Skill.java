package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class Skill implements Serializable, DbEntity {

	/**
	 * 玩家id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 技能id
	 * @mbg.generated
	 */
	private Integer id;
	/**
	 * 技能等级
	 * @mbg.generated
	 */
	private Integer level;
	/**
	 * 装备此技能的主角id
	 * @mbg.generated
	 */
	private Integer heroId;
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
	public Integer getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getLevel() {
		return level;
	}

	/**
	 * @mbg.generated
	 */
	public void setLevel(Integer level) {
		this.level = level;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getHeroId() {
		return heroId;
	}

	/**
	 * @mbg.generated
	 */
	public void setHeroId(Integer heroId) {
		this.heroId = heroId;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.SkillMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, id };
	}


}