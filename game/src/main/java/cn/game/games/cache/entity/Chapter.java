package cn.game.games.cache.entity;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;

public class Chapter implements Serializable, DbEntity {

	/**
	 * 角色id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 战役id
	 * @mbg.generated
	 */
	private Integer battleId;
	/**
	 * 战役是否通关
	 * @mbg.generated
	 */
	private Boolean pass;
	/**
	 * 领取到的战役奖励索引
	 * @mbg.generated
	 */
	private Integer rewards;
	/**
	 * @mbg.generated
	 */
	private Integer killMonsterCount;
	/**
	 * 剩余血量百分比
	 * @mbg.generated
	 */
	private Integer hpPercent;
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
	public Integer getBattleId() {
		return battleId;
	}

	/**
	 * @mbg.generated
	 */
	public void setBattleId(Integer battleId) {
		this.battleId = battleId;
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

	/**
	 * @mbg.generated
	 */
	public Integer getKillMonsterCount() {
		return killMonsterCount;
	}

	/**
	 * @mbg.generated
	 */
	public void setKillMonsterCount(Integer killMonsterCount) {
		this.killMonsterCount = killMonsterCount;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getHpPercent() {
		return hpPercent;
	}

	/**
	 * @mbg.generated
	 */
	public void setHpPercent(Integer hpPercent) {
		this.hpPercent = hpPercent;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.ChapterMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, battleId };
	}

	public static Chapter valueOf(long playerId, int chapterId) {
		Chapter chapter = new Chapter();
		chapter.setPlayerId(playerId);
		chapter.setBattleId(chapterId);
		chapter.setPass(false);
		chapter.setRewards(-1);
		chapter.setKillMonsterCount(0);
		chapter.setHpPercent(0);
		return chapter;
	}

}