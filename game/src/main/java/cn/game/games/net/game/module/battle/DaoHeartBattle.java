package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.net.game.helper.BattleHelper;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.manager.BattleManager;

/**    
 * 道心磨砺和心魔试炼
 * 2024年5月31日 下午4:56:08
 * @author SYQ
 */
public class DaoHeartBattle {

	/** 战役类型 */
	private int type;
	/** 最新通关的battleId */
	private int completeBattleId;
	private int nextBattleId;
	/** 下一关可打的战役随机buff  增益或者减益buff， HeroBUFF表id*/
	private List<Integer> randomBuff = new ArrayList<>();
	/** 本玩法领取过奖励的battle id。  */
	private List<Integer> rewardBattleIds = new ArrayList<>();
	/** 免费扫荡次数。 */
	private int freeSweep = 2; //
	/** 收费扫荡次数。 */
	private int paySweep = 3; //

	public DaoHeartBattle() {
	};

	public DaoHeartBattle(int type) {
		this.type = type;
		nextBattleId = nextBattleId();
		reset();
	};

	/** 
	 * 每天重置数据
	 */
	public void reset() {
		this.randomBuff.clear();
		this.freeSweep = 0;
		this.paySweep = 0;

		BattleConfig next = BattleManager.instance().getNullable(nextBattleId);
		if (next != null) {
			randomBuff.addAll(BattleHelper.randomBuffs(nextBattleId, 1));
		}
	}

	public void battleCompleted() {
		if (nextBattleId > 0) {
			this.completeBattleId = nextBattleId;
			this.randomBuff.clear();
			nextBattleId = nextBattleId();
			BattleConfig next = BattleManager.instance().getNullable(nextBattleId);
			if (next != null) {
				randomBuff.addAll(BattleHelper.randomBuffs(nextBattleId, 1));
			}
		}
	}

	private int nextBattleId() {
		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(type);
		if (battleTypeList != null) {
			for (BattleConfig battleConfig : battleTypeList) {
				if (battleConfig.preBattle == completeBattleId) {
					return battleConfig.ID;
				}
			}
		}
		return 0;
	}

	public int getMaxFreeSweepCount() {
		if (type == 2) {
			return GlobalConst.DaoHeartFreeCnt;
		} else if (type == 3) {
			return GlobalConst.InnerDemonsFreeCnt;
		}
		else if (type == 4) {
			return GlobalConst.DemonKingFreeCnt;
		}
		throw new IllegalArgumentException("没有实现的战役类型： " + type);
	}

	public int getMaxPaySweepCount() {
		if(type == 2) {
			return GlobalConst.DaoHeartPayCnt;
		}else if (type == 3) {
			return GlobalConst.InnerDemonsPayCnt;
		} else if (type == 4) {
			return GlobalConst.DemonKingCnt;
		}
		throw new IllegalArgumentException("没有实现的战役类型： " + type);
	}

	public int[] getPaySweepCost() {
		return getPaySweepCostAll()[paySweep];
	}

	public int[][] getPaySweepCostAll() {
		if (type == 2) {
			return GlobalConst.DaoHeartConsume;
		} else if (type == 3) {
			return GlobalConst.InnerDemonsConsume;
		} else if (type == 4) {
			return GlobalConst.DemonKingConsume;
		}
		throw new IllegalArgumentException("没有实现的战役类型： " + type);
	}


	public List<Integer> getRandomBuff() {
		return randomBuff;
	}

	public void setRandomBuff(List<Integer> randomBuff) {
		this.randomBuff = randomBuff;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCompleteBattleId() {
		return completeBattleId;
	}

	public List<Integer> getRewardBattleIds() {
		return rewardBattleIds;
	}

	public void setRewardBattleIds(List<Integer> rewardBattleIds) {
		this.rewardBattleIds = rewardBattleIds;
	}

	public int getFreeSweep() {
		return freeSweep;
	}

	public void setFreeSweep(int freeSweep) {
		this.freeSweep = freeSweep;
	}

	public int getPaySweep() {
		return paySweep;
	}

	public void setPaySweep(int paySweep) {
		this.paySweep = paySweep;
	}

	public int getNextBattleId() {
		return nextBattleId;
	}

	public void setNextBattleId(int nextBattleId) {
		this.nextBattleId = nextBattleId;
	}

}
