package cn.game.games.cache.entity;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;
import cn.game.protocol.protobuf.ShopMsg.MonthCardProto;

public class MonthCard implements Serializable, DbEntity {

	/**
	 * 用户id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 月卡id
	 * @mbg.generated
	 */
	private Integer monthCardId;
	/**
	 * 是否领取了购买奖励，只能领取一次
	 * @mbg.generated
	 */
	private Boolean isBuyRewards;
	/**
	 * 是否领取了每日奖励，可以每天领取
	 * @mbg.generated
	 */
	private Boolean isDayRewards;
	/**
	 * 获得月卡时间，按当天的0点算
	 * @mbg.generated
	 */
	private Long getTime;
	/**
	 * 月卡失效时间
	 * @mbg.generated
	 */
	private Long expireTime;
	private long lastRewardTime;

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
	public Integer getMonthCardId() {
		return monthCardId;
	}

	/**
	 * @mbg.generated
	 */
	public void setMonthCardId(Integer monthCardId) {
		this.monthCardId = monthCardId;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getIsBuyRewards() {
		return isBuyRewards;
	}

	/**
	 * @mbg.generated
	 */
	public void setIsBuyRewards(Boolean isBuyRewards) {
		this.isBuyRewards = isBuyRewards;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getIsDayRewards() {
		return isDayRewards;
	}

	/**
	 * @mbg.generated
	 */
	public void setIsDayRewards(Boolean isDayRewards) {
		this.isDayRewards = isDayRewards;
	}

	/**
	 * @mbg.generated
	 */
	public Long getGetTime() {
		return getTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setGetTime(Long getTime) {
		this.getTime = getTime;
	}

	/**
	 * @mbg.generated
	 */
	public Long getExpireTime() {
		return expireTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setExpireTime(Long expireTime) {
		this.expireTime = expireTime;
	}

	public long getLastRewardTime() {
		return lastRewardTime;
	}

	public void setLastRewardTime(long lastRewardTime) {
		this.lastRewardTime = lastRewardTime;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.MonthCardMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, monthCardId };
	}

	public static MonthCard valueOf(long playerId, int monthCardId, long getTime, long expireTime) {
		MonthCard card=new MonthCard();
		card.setPlayerId(playerId);
		card.setMonthCardId(monthCardId);
		card.setGetTime(getTime);
		card.setExpireTime(expireTime);
		card.setIsBuyRewards(false);
		card.setIsDayRewards(false);
		card.setLastRewardTime(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
		return card;
	}

	public MonthCardProto toProto() {
		return MonthCardProto.newBuilder().setId(monthCardId).setExpireTime((int) (expireTime/1000)).setIsBuyRewards(isBuyRewards)
				.setIsDayRewards(isDayRewards).build();
	}
}