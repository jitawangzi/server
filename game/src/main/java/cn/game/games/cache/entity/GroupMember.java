package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class GroupMember implements Serializable, DbEntity {

	/**
	 * 群组id
	 * @mbg.generated
	 */
	private Long groupId;
	/**
	 * 玩家id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * @mbg.generated
	 */
	private String playerServerId;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

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
	public String getPlayerServerId() {
		return playerServerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerServerId(String playerServerId) {
		this.playerServerId = playerServerId;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.GroupMemberMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { groupId, playerId };
	}
}