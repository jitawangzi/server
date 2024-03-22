package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class Skin implements Serializable, DbEntity {

	/**
	 * 角色id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 购买的皮肤id
	 * @mbg.generated
	 */
	private Integer skin;
	/**
	 * @mbg.generated
	 */
	private Integer getTime;
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
	public Integer getSkin() {
		return skin;
	}

	/**
	 * @mbg.generated
	 */
	public void setSkin(Integer skin) {
		this.skin = skin;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getGetTime() {
		return getTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setGetTime(Integer getTime) {
		this.getTime = getTime;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.SkinMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, skin };
	}
}