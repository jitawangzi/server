package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.core.SimplePlayer;

public class ForbidAccount implements Serializable, DbEntity {

	/**
	 * 封禁的玩家id
	 * @mbg.generated
	 */
	private long playerId;
	/**
	 * 封禁的玩家
	 * @mbg.generated
	 */
	private String name;
	/**
	 * 等级
	 * @mbg.generated
	 */
	private int level;
	/**
	 * 解封时间
	 * @mbg.generated
	 */
	private Date unblockTime;
	/**
	 * 封禁原因
	 * @mbg.generated
	 */
	private String reason;
	/**
	 * 封禁类型 0 所有， 1 封禁账号，2 禁言
	 * @mbg.generated
	 */
	private int type;
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
	public String getName() {
		return name;
	}

	/**
	 * @mbg.generated
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @mbg.generated
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @mbg.generated
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @mbg.generated
	 */
	public Date getUnblockTime() {
		return unblockTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setUnblockTime(Date unblockTime) {
		this.unblockTime = unblockTime;
	}

	/**
	 * @mbg.generated
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @mbg.generated
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * @mbg.generated
	 */
	public int getType() {
		return type;
	}

	/**
	 * @mbg.generated
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.ForbidAccountMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return playerId;
	}

	public static ForbidAccount valueOf(Player p, String reason, Date unblockTime) {
		ForbidAccount f = new ForbidAccount();
		f.setPlayerId(p.getPlayerId());
		f.setName(p.getData().getName());
		f.setLevel(p.getData().getLevel());
		f.setReason(reason);
		f.setUnblockTime(unblockTime);
		return f;
	}

	public static ForbidAccount valueOf(SimplePlayer p, String reason, Date unblockTime) {
		ForbidAccount f = new ForbidAccount();
		f.setPlayerId(p.getId());
		f.setName(p.getName());
		f.setLevel(p.getLevel());
		f.setReason(reason);
		f.setUnblockTime(unblockTime);
		return f;
	}

	public static boolean isForbidLogin(int type){
//		 * 封禁类型 0 所有， 1 封禁账号，2 禁言
		if (type == 0){
			return true;
		}
		if (type == 1){
			return true;
		}
		return false;
	}

	public static boolean isForbidChat(int type){
		return type == 0 || type == 2;
	}

	public void updateType(int type){
		if (this.type != 0 && this.type == type) {
			return;
		}
		if (this.type != 0 && this.type == 1 && type == 2) {
			this.type = 0;
			return;
		}
		if (this.type != 0 && this.type == 2 && type == 1) {
			this.type = 0;
			return;
		}
		this.type = type;
	}



}