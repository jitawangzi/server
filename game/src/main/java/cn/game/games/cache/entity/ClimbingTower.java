package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Set;

import cn.game.protocol.manual.OldErrorMsgEnum;
import cn.game.util.StrUtil;

public class ClimbingTower implements Serializable {

	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 爬塔积分
	 * @mbg.generated
	 */
	private Integer score;
	/**
	 * 当前段位
	 * @mbg.generated
	 */
	private Integer level;
	/**
	 * 当前层
	 * @mbg.generated
	 */
	private Integer layer;
	/**
	 * 本层是否完成
	 * @mbg.generated
	 */
	private Boolean layerFinish;
	/**
	 * 本层完成时间
	 * @mbg.generated
	 */
	private Long scoreTime;
	/**
	 * 当前层的关卡id
	 * @mbg.generated
	 */
	private String battleLevel;
	/**
	 * 20人小组id
	 * @mbg.generated
	 */
	private Integer groupId;
	/**
	 * 爬塔代币
	 * @mbg.generated
	 */
	private Integer money;
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
	public Integer getLayer() {
		return layer;
	}

	/**
	 * @mbg.generated
	 */
	public void setLayer(Integer layer) {
		this.layer = layer;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getLayerFinish() {
		return layerFinish;
	}

	/**
	 * @mbg.generated
	 */
	public void setLayerFinish(Boolean layerFinish) {
		this.layerFinish = layerFinish;
	}

	/**
	 * @mbg.generated
	 */
	public Long getScoreTime() {
		return scoreTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setScoreTime(Long scoreTime) {
		this.scoreTime = scoreTime;
	}

	/**
	 * @mbg.generated
	 */
	public String getBattleLevel() {
		return battleLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setBattleLevel(String battleLevel) {
		this.battleLevel = battleLevel;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getGroupId() {
		return groupId;
	}

	/**
	 * @mbg.generated
	 */
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getMoney() {
		return money;
	}

	/**
	 * @mbg.generated
	 */
	public void setMoney(Integer money) {
		this.money = money;
	}

	private int[] battles;
	
	public int[] getBattles() {
		if (battles == null) {
			battles = StrUtil.parseArray(battleLevel);
		}
		return battles;
	}
	
	public int checkBattle(int battleLevelId) {
		for (int id : getBattles()) {
			if (id == battleLevelId) {
				return 0;
			}
		}
		return OldErrorMsgEnum.illegal_request.getId();
	}

	public void setBattles(Set<Integer> set) {
		int index = 0;
		StringBuilder sb = new StringBuilder();

		if (getBattles().length != set.size()) {
			battles = new int[set.size()];
		}
		for (Integer id : set) {
			battles[index++] = id;
			sb.append(id).append("|");
		}
		setBattleLevel(sb.toString());
	}
}