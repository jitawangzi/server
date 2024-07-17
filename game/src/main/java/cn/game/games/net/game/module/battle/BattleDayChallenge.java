package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.net.game.helper.BattleHelper;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.util.Rnd;

/**    
 * 每日挑战
 * 2024年5月27日 上午10:50:49
 * @author SYQ
 */
public class BattleDayChallenge {

	private int battleTimes; // 每日挑战，挑战过的次数。
	private int battleId; // 每日挑战，每天不同的Battle 表id。
	private List<Integer> randomBuff = new ArrayList<>(); // 每天不同的随机 增益或者减益buff， HeroBUFF表id
	private List<Integer> rewardIndex = new ArrayList<>(); // 已经领取过奖励箱子的索引， 如果4个箱子就是0 - 3 。

	/** 
	 * 每天重置数据
	 */
	public void reset() {
		this.battleId = 0;
		this.battleTimes = 0;
		this.randomBuff.clear();
		this.rewardIndex.clear();

		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(12);
		if (battleTypeList != null) {
			BattleConfig battleConfig = Rnd.randomOne(battleTypeList);
			this.battleId = battleConfig.ID;
			randomBuff.addAll(BattleHelper.randomBuffs(battleConfig.ID, 1));
		}
	}
	public int getBattleTimes() {
		return battleTimes;
	}

	public void setBattleTimes(int battleTimes) {
		this.battleTimes = battleTimes;
	}

	public int getBattleId() {
		return battleId;
	}

	public void setBattleId(int battleId) {
		this.battleId = battleId;
	}

	public List<Integer> getRandomBuff() {
		return randomBuff;
	}

	public void setRandomBuff(List<Integer> randomBuff) {
		this.randomBuff = randomBuff;
	}

	public List<Integer> getRewardIndex() {
		return rewardIndex;
	}

	public void setRewardIndex(List<Integer> rewardIndex) {
		this.rewardIndex = rewardIndex;
	}

}
