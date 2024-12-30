package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;

public class Invite implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Long id;
	/**
	 * 邀请者的角色id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 被邀请者玩家id
	 * @mbg.generated
	 */
	private Long dstPid;
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
	public Long getDstPid() {
		return dstPid;
	}

	/**
	 * @mbg.generated
	 */
	public void setDstPid(Long dstPid) {
		this.dstPid = dstPid;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.InviteMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}
}