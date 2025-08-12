package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.List;

import cn.game.protocol.protobuf.BattleMsg;
import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.RankConfig;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.enume.WelfareTypeEnum;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.RankManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class ShiLuoZhenJingBattle extends XiYouBattleHandler {

	private int historyMaxStage = -1;
	private int historyMaxBattleId;
	/** 历史最高奖励是否已经领取了。 */
	private boolean historyMaxReward;

	/** 当前可打的失落真经非战斗关卡，从1开始，如果为10表示打战斗关卡 */
	private int battleStage = -1;
	@Deprecated
	private boolean dayReward;
	/** 最新通关的battleId */
	private int completeBattleId;
	/** 手动挑战的起始关卡，当前可打的战役id（还没通关的）战斗关卡。 */
	private int startBattleId;
	/** 下一关可打的战役随机buff  增益或者减益buff， HeroBUFF表id*/
	private List<Integer> randomBuff = new ArrayList<>();
	/** 是否可以领奖， 只有注册后第二天才可以领奖 */
	private boolean canReward = false;
	/** gm命令直接手动关 */
	@JsonIgnore
	private boolean zhijieshoudong = false;

	@Override
	void newDay() {
		canReward = true;
		reset();
	}

	@Override
	public void onLogin() {
		if (startBattleId == 0) {
			nextBattleId();
		}
	}

	public void reset() {
		// 跨天重置之前，先结算一下奖励
//		if (startBattleId > 0) {
//			BattleConfig battleConfig = BattleManager.instance().get(startBattleId);
//			List<Goods> goods = new ArrayList<>();
//			for (int randomId : battleConfig.BattleBoxRandomId) {
//				goods.addAll(PlayerHelper.randomReward(player, randomId));
//			}
//			MailHelper.sendMail(player.getPlayerId(), 7, goods);
//		}
		if (historyMaxReward) {
			historyMaxReward = false;
		}
		historyMaxBattleId = completeBattleId;
		historyMaxStage = battleStage;
//		dayReward = false;
		if (startBattleId == 0) {
			nextBattleId();
		}
		List<Integer> randomBuffs = BattleHelper.randomBuffs(startBattleId, DungeonTypeEnum.ShiLuoZhenJing.getId());
		this.randomBuff.clear();
		this.randomBuff.addAll(randomBuffs);
		if (battleStage == -1) {
			battleStage = 1;
		}
	}

	private void nextBattleId() {
		startBattleId = BattleHelper.nextStartBattleId(DungeonTypeEnum.ShiLuoZhenJing, completeBattleId);
	}

	public void battleCompleted() {
		this.completeBattleId = startBattleId;
		if (this.battleStage == 10) {
			nextBattleId();
			this.randomBuff.clear();
			List<Integer> randomBuffs = BattleHelper.randomBuffs(startBattleId, DungeonTypeEnum.ShiLuoZhenJing.getId());
			this.randomBuff.addAll(randomBuffs);
			
			RankConfig rankConfig = RankManager.instance().get(RankType.ShiLuoZhenJing.ID);
			if (BattleHelper.isComplete(completeBattleId, rankConfig.Request)) {
				RankService.getInstance().setScoreAsync(player.getServerId(), RankType.ShiLuoZhenJing, player.getPlayerId(), completeBattleId);
			}
		}
		if (!zhijieshoudong) {
			this.battleStage++;
			// 1打到9，然后10本关
			if (this.battleStage == 11) {
				this.battleStage = 1;
			}
		}

	}

	@Override
	public int check(int id, int subId,long ... args) {
		if (id != startBattleId || subId != battleStage) {
			return ErrorMsgEnum.request_parameter_error.ID;
		}
		return super.check(id, subId);
	}

	@Override
	public int battleStart(int id) {

		return 0;
	}

	@Override
	public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request, BattleMsg.BattleFieldEndResponse_13000004.Builder response) {

		if (!request.getWin()) {
			return ResultObject.success();
		}
		BattleModule battleModule = player.getBattleModule();
		int attackingId = battleModule.getAttackingId();
		BattleConfig battleConfig = BattleManager.instance().get(attackingId);
		int oldBattleStage  = battleStage;
		battleCompleted();
		// 通关奖励，用公式计算
		return ResultObject.success(calcRewardInfos(battleConfig.Level, oldBattleStage));
	}

	@Override
	public ResultObject<List<RewardInfo>> quickEnd(int id, int subId, boolean isWin) {
		if (!isWin) {
			return ResultObject.success();
		}
		battleCompleted();

		BattleConfig battleConfig = BattleManager.instance().get(id);
		// 通关奖励，用公式计算
		return ResultObject.success(calcRewardInfos(battleConfig.Level, subId));
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.ShiLuoZhenJing.getId();
	}

	public int getBattleStage() {
		return battleStage;
	}

	public int getCompleteBattleId() {
		return completeBattleId;
	}

	public int getStartBattleId() {
		return startBattleId;
	}

	public List<Integer> getRandomBuff() {
		return randomBuff;
	}


	public int getHistoryMaxStage() {
		return historyMaxStage;
	}

	public void setHistoryMaxStage(int historyMaxStage) {
		this.historyMaxStage = historyMaxStage;
	}

	public int getHistoryMaxBattleId() {
		return historyMaxBattleId;
	}

	public boolean isHistoryMaxReward() {
		return historyMaxReward;
	}

	public void setHistoryMaxReward(boolean historyMaxReward) {
		this.historyMaxReward = historyMaxReward;
	}

	public boolean isCanReward() {
		return canReward;
	}

	public void setHistoryMaxBattleId(int historyMaxBattleId) {
		this.historyMaxBattleId = historyMaxBattleId;
	}

	public void setStartBattleId(int startBattleId) {
		this.startBattleId = startBattleId;
	}

	public void setCompleteBattleId(int completeBattleId) {
		this.completeBattleId = completeBattleId;
	}

	public void setBattleStage(int battleStage) {
		this.battleStage = battleStage;
	}

	public void setZhijieshoudong(boolean zhijieshoudong) {
		this.zhijieshoudong = zhijieshoudong;
	}

	private List<RewardInfo> calcRewardInfos(int level, int stage) {
		List<RewardInfo> rewardInfos = new ArrayList<>();

		int welfareValue = player.getWelfareValue(WelfareTypeEnum.LostScripturesFriendAddition);
		for (int i = 0; i < GlobalConst.LostScripturesRewards.length; i++) {
			int rewardId = GlobalConst.LostScripturesRewards[i];
			int rewardCount = calcRewardCount(i, level, stage);
			if (welfareValue > 0) {
				rewardCount += rewardCount * welfareValue / 10000f;
			}
			if (rewardCount <= 0){
				continue;
			}
			List<RewardInfo> resources = PlayerHelper.addResources(player, rewardId, rewardCount, OpType.ShiLuoZhenJing);
			rewardInfos.addAll(resources);
		}
		return rewardInfos;
	}

	private int calcRewardCount(int index, int level, int stage) {
//	private int calcRewardCount(float[] rewardsParam, int level, int stage) {//旧的 公式
//		LostScripturesRewardsFormula	【失落真经】奖励公式	1号位奖励公式=IF(小关卡数/5取余数=0,5，IF(小关卡数/10取余数=0,10,空))|2号位奖励=30|3号位奖励=大关卡数*100+小关卡数10
		float[][] params = GlobalConst.LostScripturesRewardsFormula;
		if (index == 0){//1号位奖励公式=IF(小关卡数/5取余数=0,5，IF(小关卡数/10取余数=0,10,空))
			return stage == 5 ? (int)params[0][0] : ((stage == 10) ? (int)params[0][1] : 0);
		} else if (index == 1){//2号位奖励=30
			return (int)params[1][0];
		} else if (index == 2) {//3号位奖励=大关卡数*100+小关卡数10
			return level * (int)params[2][0] + stage * (int)params[2][1];
		}
		return 0;

		/*// 计算关卡的值
		int battle = (level - 1) * 10 + stage;
		// 计算整体公式的值
		double result = (rewardsParam[0] * Math.pow((battle + rewardsParam[1]), rewardsParam[2]) + rewardsParam[3]) / rewardsParam[4] + 1;
		// 向下取整并乘以 n
		int finalResult = (int) (Math.floor(result) * rewardsParam[5]);
		return finalResult;*/
	}

}
