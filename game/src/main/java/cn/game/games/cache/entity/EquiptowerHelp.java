package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;

/**
 * 组队副本助战
 * @mbg.generated
 */
public class EquiptowerHelp implements Serializable, DbEntity {

	/**
	 * 唯一id
	 * @mbg.generated
	 */
	private long id;
	/**
	 * 助战的玩家id，也就是被邀请人
	 * @mbg.generated
	 */
	private long playerId;
	/**
	 * 邀请人id
	 * @mbg.generated
	 */
	private long helpPlayerId;
	/**
	 * 层数
	 * @mbg.generated
	 */
	private int floor;
	/**
	 * 过期时间，秒时间戳
	 * @mbg.generated
	 */
	private int expiredTime;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public long getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(long id) {
		this.id = id;
	}

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
	public long getHelpPlayerId() {
		return helpPlayerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setHelpPlayerId(long helpPlayerId) {
		this.helpPlayerId = helpPlayerId;
	}

	/**
	 * @mbg.generated
	 */
	public int getFloor() {
		return floor;
	}

	/**
	 * @mbg.generated
	 */
	public void setFloor(int floor) {
		this.floor = floor;
	}

	/**
	 * @mbg.generated
	 */
	public int getExpiredTime() {
		return expiredTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setExpiredTime(int expiredTime) {
		this.expiredTime = expiredTime;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.EquiptowerHelpMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}
}