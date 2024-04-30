package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.game.games.cache.base.DbEntity;
import cn.game.protocol.protobuf.BattleMsg.BattleInfo;
import cn.game.protocol.protobuf.BattleMsg.BattleInfo.Builder;

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
	 */
	private List<Integer> rewards = new ArrayList<>();;
	/**
	 * @mbg.generated
	 */
	private Integer killMonsterCount;
	/**
	 * 剩余血量百分比
	 * @mbg.generated
	 */
	private Integer hpPercent;

	/** 战斗时长（秒） */
	private int battleTime;
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

	public int getBattleTime() {
		return battleTime;
	}

	public void setBattleTime(int battleTime) {
		this.battleTime = battleTime;
	}

	public List<Integer> getRewards() {
		return rewards;
	}

//	@Override
//	public Class<?> getMapperClass() {
//		return cn.game.games.net.data.mapper.ChapterMapper.class;
//	}

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
//		chapter.setRewards(-1);
		chapter.setKillMonsterCount(0);
		chapter.setHpPercent(0);
		return chapter;
	}

	public BattleInfo toBattleInfo() {
		Builder builder = BattleInfo.newBuilder();
		builder.setId(battleId);
		builder.setHpPercent(hpPercent);
		builder.setFinish(pass) ; 
		builder.addAllRewardIndex(rewards);
//		builder.setRewardIndex(rewards) ; 
		builder.setBattleTime(battleTime);

		return builder.build();

	}

}