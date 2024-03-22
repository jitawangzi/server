package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;

public class PlayerTest implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Long id;
	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * @mbg.generated
	 */
	private Integer intarg1;
	/**
	 * @mbg.generated
	 */
	private String stringarg2;
	/**
	 * @mbg.generated
	 */
	private String resource;
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
	public Integer getIntarg1() {
		return intarg1;
	}

	/**
	 * @mbg.generated
	 */
	public void setIntarg1(Integer intarg1) {
		this.intarg1 = intarg1;
	}

	/**
	 * @mbg.generated
	 */
	public String getStringarg2() {
		return stringarg2;
	}

	/**
	 * @mbg.generated
	 */
	public void setStringarg2(String stringarg2) {
		this.stringarg2 = stringarg2;
	}

	/**
	 * @mbg.generated
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * @mbg.generated
	 */
	public void setResource(String resource) {
		this.resource = resource;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.PlayerTestMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}
}