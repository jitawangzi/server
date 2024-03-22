package cn.game.games.cache.entity;

import java.io.Serializable;

public class PlayerGroup implements Serializable {

	/**
	 * 玩家id
	 * @mbggenerated
	 */
	private Long playerId;
	/**
	 * 群组id
	 * @mbggenerated
	 */
	private Long groupId;
	/**
	 * 群组所在服务器id
	 * @mbggenerated
	 */
	private String groupServerId;
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
	public Long getGroupId() {
		return groupId;
	}

	/**
	 * @mbggenerated
	 */
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	/**
	 * @mbggenerated
	 */
	public String getGroupServerId() {
		return groupServerId;
	}

	/**
	 * @mbggenerated
	 */
	public void setGroupServerId(String groupServerId) {
		this.groupServerId = groupServerId;
	}
}