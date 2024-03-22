package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class QuestChallenge implements Serializable, DbEntity {

	/**
	 * MissionChallengeGroup.xlsm 的id
	 * @mbg.generated
	 */
	private Integer id;
	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 组任务积分
	 * @mbg.generated
	 */
	private Integer score;
	/**
	 * 是否完成了（领取过奖励）
	 * @mbg.generated
	 */
	private Boolean finish;
	/**
	 * 开启时间（秒）
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
	public Integer getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(Integer id) {
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
	public Integer getScore() {
		return score;
	}

	/**
	 * @mbg.generated
	 */
	public void setScore(Integer score) {
		this.score = score;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getFinish() {
		return finish;
	}

	/**
	 * @mbg.generated
	 */
	public void setFinish(Boolean finish) {
		this.finish = finish;
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
		return cn.game.games.net.data.mapper.QuestChallengeMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { id, playerId };
	}
}