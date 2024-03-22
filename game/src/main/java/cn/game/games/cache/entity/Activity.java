package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class Activity implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * @mbg.generated
	 */
	private Integer id;
	/**
	 * @mbg.generated
	 */
	private Byte stat;
	/**
	 * @mbg.generated
	 */
	private String params;
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
	public Byte getStat() {
		return stat;
	}

	/**
	 * @mbg.generated
	 */
	public void setStat(Byte stat) {
		this.stat = stat;
	}

	/**
	 * @mbg.generated
	 */
	public String getParams() {
		return params;
	}

	/**
	 * @mbg.generated
	 */
	public void setParams(String params) {
		this.params = params;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.ActivityMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, id };
	}


}