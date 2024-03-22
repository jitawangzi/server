package cn.game.games.cache.entity;

import java.io.Serializable;

public class BattleChapter implements Serializable {

	/**
	 * 角色id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 意识空间id
	 * @mbg.generated
	 */
	private Integer chapterId;
	/**
	 * 意识空间是否通关，貌似没什么用了
	 * @mbg.generated
	 */
	private Boolean pass;
	/**
	 * 领取过的意识空间奖励索引
	 * @mbg.generated
	 */
	private Integer rewards;
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
	public Integer getChapterId() {
		return chapterId;
	}

	/**
	 * @mbg.generated
	 */
	public void setChapterId(Integer chapterId) {
		this.chapterId = chapterId;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getPass() {
		return pass;
	}

	/**
	 * @mbg.generated
	 */
	public void setPass(Boolean pass) {
		this.pass = pass;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getRewards() {
		return rewards;
	}

	/**
	 * @mbg.generated
	 */
	public void setRewards(Integer rewards) {
		this.rewards = rewards;
	}
}