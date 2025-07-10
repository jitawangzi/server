package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class FriendApplication implements Serializable, DbEntity {

	/**
	 * 被申请人id
	 * @mbg.generated
	 */
	private long playerId;
	/**
	 * 申请人id
	 * @mbg.generated
	 */
	private long applyPlayerId;
	/**
	 * 申请时间
	 * @mbg.generated
	 */
	private long applyTime;
	/**
	 * 申请人所在服务器id
	 * @mbg.generated
	 */
	private String applyPlayerServer;
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
	public long getApplyPlayerId() {
		return applyPlayerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setApplyPlayerId(long applyPlayerId) {
		this.applyPlayerId = applyPlayerId;
	}

	/**
	 * @mbg.generated
	 */
	public long getApplyTime() {
		return applyTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setApplyTime(long applyTime) {
		this.applyTime = applyTime;
	}

	/**
	 * @mbg.generated
	 */
	public String getApplyPlayerServer() {
		return applyPlayerServer;
	}

	/**
	 * @mbg.generated
	 */
	public void setApplyPlayerServer(String applyPlayerServer) {
		this.applyPlayerServer = applyPlayerServer;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.FriendApplicationMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, applyPlayerId };
	}

	public static FriendApplication valueOf(long playerId, long applyPlayerId, String applyPlayerServer) {
		FriendApplication friendApplication = new FriendApplication();
		friendApplication.setApplyPlayerId(applyPlayerId);
		friendApplication.setPlayerId(playerId);
		friendApplication.setApplyPlayerServer(applyPlayerServer);
		friendApplication.setApplyTime(System.currentTimeMillis());

		return friendApplication;
	}
}