package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 梦魇秘境
 * 2024年7月15日 下午2:07:37
 * @author SYQ
 */
public class MengYanMiJingBattle extends XiYouBattleHandler {

	/** 战役类型 */
//	private int type;
	/** 历史通关最高id,用来判断关卡的首通奖励 */
	private int maxBattleId;
	/** 今天最新通关的battleId */
	private int completeBattleId;
	/** 手动挑战的起始关卡 */
	private int startBattleId;
	/** 下一关可打的战役随机buff  增益或者减益buff， HeroBUFF表id*/
	private List<Integer> randomBuff = new ArrayList<>();
	/** 本玩法领取过奖励的battle id。  */
	private List<Integer> rewardBattleIds = new ArrayList<>();
	/**  可用的buff刷新次数 */
	private int buffRefreshTimes;
	/** 历史最高刷新次数 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Deprecated
	private int buffRefreshTimesMax;
	/** 客户端选择的buff */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Deprecated
	private List<Integer> buffIds = new ArrayList<Integer>();
	/** 客户端选择的buff */
	private Map<Integer, Integer> buffIdsMap = new HashMap<Integer, Integer>();
	/** 是否可以扫荡,功能开启第二天才可以扫 */
	private boolean canQuick = false;
	/** 今天有没有打过 */
	private boolean isTodayBattle = false;
	/** 今天打过关卡，则明天减两关打，否则不变 */
	private boolean isTomorrowSomeBattle = true;

	public MengYanMiJingBattle() {
	};

	public void init() {
		reset();
	}

	/** 
	 * 每天重置数据
	 */
	public void reset() {
		if (isTodayBattle) {
			isTomorrowSomeBattle = false;
		} else {
			isTomorrowSomeBattle = true;
		}
		isTodayBattle = false;
		startBattleId = nextBattleId();
		// 充值buff刷新次数
		this.buffRefreshTimes = 0;
		BattleConfig buffRefreshBattle = BattleManager.instance().getNullable(completeBattleId);
		while (buffRefreshBattle != null) {
			this.buffRefreshTimes++;
			buffRefreshBattle = BattleManager.instance().getNullable(buffRefreshBattle.preBattle);
		}

		this.randomBuff.clear();
		this.rewardBattleIds.clear();
		this.buffIdsMap.clear();
		this.buffIds.clear();
//		buffRefreshTimes = GlobalConst.NightmareRealmInitialcnt;
//		this.completeBattleId = 0;

		BattleConfig next = BattleManager.instance().getNullable(startBattleId);
		if (next != null) {
			randomBuff.addAll(BattleHelper.randomBuffs(startBattleId, 2));
		}
		setCanQuick(true);
	}

	public boolean battleCompleted(boolean win) {
		isTodayBattle = true;
		if (win) {
			boolean newReward = false;
			this.completeBattleId = startBattleId;
			this.randomBuff.clear();
			startBattleId = nextBattleId();
			if (completeBattleId > maxBattleId) {
				maxBattleId = completeBattleId;
				newReward = true;
			}
			BattleConfig next = BattleManager.instance().getNullable(startBattleId);
			if (next != null) {
				randomBuff.addAll(BattleHelper.randomBuffs(startBattleId, 2));
			}
			this.buffRefreshTimes++;
//		this.buffRefreshTimesMax = buffRefreshTimes;
			return newReward;
		}
		return false;
	}

	private int nextBattleId() {
		// 有打过这个玩法
		if (maxBattleId > 0) {
			// 昨天没打过，不变，昨天打过，减两关
			if (isTodayBattle || isTomorrowSomeBattle) {
				if (completeBattleId > 0) {
					BattleConfig nextBattleConfig = BattleHelper.nextBattleConfig(completeBattleId);
					if (nextBattleConfig != null) {
						return nextBattleConfig.ID;
					}
					return completeBattleId;
				}
			} else {
				BattleConfig completeBattle = BattleManager.instance().get(completeBattleId);
				BattleConfig preBattle = BattleManager.instance().getNullable(completeBattle.preBattle);
				if (preBattle == null) {
					return completeBattleId;
				}
				BattleConfig prepreBattle = BattleManager.instance().getNullable(preBattle.preBattle);
				if (prepreBattle == null) {
					completeBattleId = preBattle.ID;
				} else {
					completeBattleId = prepreBattle.ID;
				}
//				this.buffRefreshTimes = this.buffRefreshTimesMax - 2;
//				if (this.buffRefreshTimes < 0) {
//					this.buffRefreshTimes = 0;
//				}
				return preBattle.ID;
			}
//			BattleConfig prepreBattle = BattleManager.instance().getNullable(preBattle.preBattle);
//			if (prepreBattle == null) {
//				return preBattle.ID;
//			}

		}
		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(9);
		if (battleTypeList != null) {
			for (BattleConfig battleConfig : battleTypeList) {
				if (battleConfig.preBattle == completeBattleId) {
					return battleConfig.ID;
				}
			}
		}
		return 0;
	}

	public int maxSweepBattle() {
		if (maxBattleId == 0) {
			return 0;
		}
		BattleConfig maxBattle = BattleManager.instance().get(maxBattleId);
		BattleConfig preBattle = BattleManager.instance().getNullable(maxBattle.preBattle);
		if (preBattle == null) {
			return 0;
		}
		BattleConfig prepreBattle = BattleManager.instance().getNullable(preBattle.preBattle);
		if (prepreBattle == null) {
			return 0;
		}
		return prepreBattle.ID;
	}

	public List<Integer> getRandomBuff() {
		return randomBuff;
	}

	public void setRandomBuff(List<Integer> randomBuff) {
		this.randomBuff = randomBuff;
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

	public int getMaxBattleId() {
		return maxBattleId;
	}

	public int getStartBattleId() {
		return startBattleId;
	}

	public int getBuffRefreshTimes() {
		return buffRefreshTimes;
	}

	public void setBuffRefreshTimes(int buffRefreshTimes) {
		this.buffRefreshTimes = buffRefreshTimes;
	}

	public List<Integer> getBuffIds() {
		return buffIds;
	}

	public Map<Integer, Integer> getBuffIdsMap() {
		return buffIdsMap;
	}

	public void setBuffIdsMap(Map<Integer, Integer> buffIdsMap) {
		this.buffIdsMap = buffIdsMap;
	}

	public boolean isCanQuick() {
		return canQuick;
	}

	public void setCanQuick(boolean canQuick) {
		this.canQuick = canQuick;
	}

	@Override
	public int battleStart(int id) {
		if (!player.isFuncOpen(InitialUI.NightmareRealm)) {
			return ErrorMsgEnum.func_not_open.getId();
		}
		if (id != getStartBattleId()) {
			return ErrorMsgEnum.request_parameter_error.getId();
		}
		return 0;
	}

	@Override
	public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request) {

		if (!request.getWin()) {
			this.battleCompleted(false);
			return ResultObject.success();
		}
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		int attackingType = chapterModule.getAttackingType();
		BattleConfig battleConfig = BattleManager.instance().get(chapterModule.getAttackingDungeonId());
		boolean newRecord = this.battleCompleted(true);
		if (newRecord) {
			OpType opType = OpType.MengYanMiJingFirstFinish;
			List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.FirstPassReward, opType);
			return ResultObject.success(reward);
		}
		return ResultObject.success();
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.MengYanMiJing.getId();
	}

	@Override
	void newDay() {
		reset();
	}

}
