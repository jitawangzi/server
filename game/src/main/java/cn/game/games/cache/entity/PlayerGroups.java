package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class PlayerGroups implements Serializable, DbEntity {

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
	 * 群组所在服务器id
	 * @mbg.generated
	 */
	private String groupServerId;
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
	public String getGroupServerId() {
		return groupServerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setGroupServerId(String groupServerId) {
		this.groupServerId = groupServerId;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.PlayerGroupsMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, groupId };
	}
}