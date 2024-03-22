package cn.game.games.cache.entity;

import java.io.Serializable;

public class Msg implements Serializable {

	/**
	 * 用户id
	 * @mbggenerated
	 */
	private Long playerId;
	/**
	 * 金币
	 * @mbggenerated
	 */
	private Long coin;
	/**
	 * @mbggenerated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbggenerated
	 */
	public Long getPlayerId() {
		return playerId;
	}

	/**
	 * @mbggenerated
	 */
	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	/**
	 * @mbggenerated
	 */
	public Long getCoin() {
		return coin;
	}

	/**
	 * @mbggenerated
	 */
	public void setCoin(Long coin) {
		this.coin = coin;
	}
}