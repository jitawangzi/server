package cn.game.games.cache.entity;

import java.io.Serializable;

public class EventGame implements Serializable {

	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * GameEvent表id
	 * @mbg.generated
	 */
	private Integer eventId;
	/**
	 * 事件阶段
	 * @mbg.generated
	 */
	private Byte stage;
	/**
	 * 这个事件当前是否完成了
	 * @mbg.generated
	 */
	private Boolean isFinished;
	/**
	 * 是否放弃了事件（脱离），放弃得分支事件，不会再刷新出来
	 * @mbg.generated
	 */
	private Boolean isGiveup;
	/**
	 * 这个事件是否完成过
	 * @mbg.generated
	 */
	private Boolean hasCompleted;
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
	public Integer getEventId() {
		return eventId;
	}

	/**
	 * @mbg.generated
	 */
	public void setEventId(Integer eventId) {
		this.eventId = eventId;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getStage() {
		return stage;
	}

	/**
	 * @mbg.generated
	 */
	public void setStage(Byte stage) {
		this.stage = stage;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getIsFinished() {
		return isFinished;
	}

	/**
	 * @mbg.generated
	 */
	public void setIsFinished(Boolean isFinished) {
		this.isFinished = isFinished;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getIsGiveup() {
		return isGiveup;
	}

	/**
	 * @mbg.generated
	 */
	public void setIsGiveup(Boolean isGiveup) {
		this.isGiveup = isGiveup;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getHasCompleted() {
		return hasCompleted;
	}

	/**
	 * @mbg.generated
	 */
	public void setHasCompleted(Boolean hasCompleted) {
		this.hasCompleted = hasCompleted;
	}
}