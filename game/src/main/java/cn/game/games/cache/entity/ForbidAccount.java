package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;

import cn.game.games.core.SimplePlayer;
import cn.game.games.cache.base.DbEntity;

public class ForbidAccount implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * @mbg.generated
	 */
	private String name;
	/**
	 * 等级
	 * @mbg.generated
	 */
	private Integer level;
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
	public Integer getLevel() {
		return level;
	}

	/**
	 * @mbg.generated
	 */
	public void setLevel(Integer level) {
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
}