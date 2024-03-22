package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class Sign implements Serializable, DbEntity {

	/**
	 * 用户id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 当月签到情况 0-未签到；1已签到，逗号隔开
	 * @mbg.generated
	 */
	private String signInfo;
	/**
	 * 当前签到周期，如：202012，表示2020年12月
	 * @mbg.generated
	 */
	private Integer time;
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
	public String getSignInfo() {
		return signInfo;
	}

	/**
	 * @mbg.generated
	 */
	public void setSignInfo(String signInfo) {
		this.signInfo = signInfo;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getTime() {
		return time;
	}

	/**
	 * @mbg.generated
	 */
	public void setTime(Integer time) {
		this.time = time;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.SignMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return playerId;
	}

	public static Sign valueOf(long playerId2, int time2, String signInfo2) {
		Sign sign=new Sign();
		sign.setPlayerId(playerId2);
		sign.setSignInfo(signInfo2);
		sign.setTime(time2);
		return sign;
	}
}