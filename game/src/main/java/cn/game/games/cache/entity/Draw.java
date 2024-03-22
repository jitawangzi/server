package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class Draw implements Serializable, DbEntity {

	/**
	 * 玩家id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 新手卡池累计次数
	 * @mbg.generated
	 */
	private Byte noviceTimes;
	/**
	 * 普通卡池累计次数
	 * @mbg.generated
	 */
	private Byte commonTimes;
	/**
	 * UP卡池累计次数
	 * @mbg.generated
	 */
	private Byte upTimes;
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
	public Byte getNoviceTimes() {
		return noviceTimes;
	}

	/**
	 * @mbg.generated
	 */
	public void setNoviceTimes(Byte noviceTimes) {
		this.noviceTimes = noviceTimes;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getCommonTimes() {
		return commonTimes;
	}

	/**
	 * @mbg.generated
	 */
	public void setCommonTimes(Byte commonTimes) {
		this.commonTimes = commonTimes;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getUpTimes() {
		return upTimes;
	}

	/**
	 * @mbg.generated
	 */
	public void setUpTimes(Byte upTimes) {
		this.upTimes = upTimes;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.DrawMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return playerId;
	}

	public static Draw valueOf(long playerId, int novice, int common, int up) {
		Draw d = new Draw();
		d.setPlayerId(playerId);
		d.setNoviceTimes((byte) novice);
		d.setCommonTimes((byte) common);
		d.setUpTimes((byte) up);
		
		return d;
	}
}