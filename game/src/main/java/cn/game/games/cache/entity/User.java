package cn.game.games.cache.entity;

import java.io.Serializable;

public class User implements Serializable {

	/**
	 * 用户id，全存档通用
	 * @mbggenerated
	 */
	private Long id;
	/**
	 * @mbggenerated
	 */
	private Integer rmb;
	/**
	 * @mbggenerated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbggenerated
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @mbggenerated
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @mbggenerated
	 */
	public Integer getRmb() {
		return rmb;
	}

	/**
	 * @mbggenerated
	 */
	public void setRmb(Integer rmb) {
		this.rmb = rmb;
	}
}