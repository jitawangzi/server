package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class PlayerGroupApplication implements Serializable, DbEntity {

	/**
	 * 玩家id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 群组id
	 * @mbg.generated
	 */
	private Long groupId;
	/**
	 * 区服id
	 * @mbg.generated
	 */
	private String serverId;
	/**
	 * 发出邀请的人
	 * @mbg.generated
	 */
	private Long friendPlayerId;
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
	public Long getGroupId() {
		return groupId;
	}

	/**
	 * @mbg.generated
	 */
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	/**
	 * @mbg.generated
	 */
	public String getServerId() {
		return serverId;
	}

	/**
	 * @mbg.generated
	 */
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	/**
	 * @mbg.generated
	 */
	public Long getFriendPlayerId() {
		return friendPlayerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setFriendPlayerId(Long friendPlayerId) {
		this.friendPlayerId = friendPlayerId;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.PlayerGroupApplicationMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, groupId };
	}
}