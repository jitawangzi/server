package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;

public class GuildJoin implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private long playerId;
	/**
	 * 加入的公会id
	 * @mbg.generated
	 */
	private long guildId;
	/**
	 * 加入时间
	 * @mbg.generated
	 */
	private long createTime;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public long getPlayerId() {
		return playerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	/**
	 * @mbg.generated
	 */
	public long getGuildId() {
		return guildId;
	}

	/**
	 * @mbg.generated
	 */
	public void setGuildId(long guildId) {
		this.guildId = guildId;
	}

	/**
	 * @mbg.generated
	 */
	public long getCreateTime() {
		return createTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.GuildJoinMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return playerId;
	}
}