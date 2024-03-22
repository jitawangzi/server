package cn.game.games.cache.entity;

import java.io.Serializable;

public class Account implements Serializable {

	/**
	 * 用户id，全存档通用
	 * @mbg.generated
	 */
	private Long id;
	/**
	 * @mbg.generated
	 */
	private Integer rmb;
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
	public Integer getRmb() {
		return rmb;
	}

	/**
	 * @mbg.generated
	 */
	public void setRmb(Integer rmb) {
		this.rmb = rmb;
	}
}