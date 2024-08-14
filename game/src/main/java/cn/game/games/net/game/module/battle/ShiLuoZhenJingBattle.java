package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class ShiLuoZhenJingBattle extends XiYouBattleHandler {

	/** 当前可打的失落真经非战斗关卡，从1开始，如果为10表示打战斗关卡 */
	private int battleStage = -1;
	/** 每日奖励是否已经领取了。 */
	@JsonIgnore
	private boolean dayReward;
	/** 最新通关的battleId */
	private int completeBattleId;
	/** 手动挑战的起始关卡，当前可打的战役id（还没通关的）战斗关卡。 */
	private int startBattleId;
	/** 下一关可打的战役随机buff  增益或者减益buff， HeroBUFF表id*/
	private List<Integer> randomBuff = new ArrayList<>();

	@Override
	void newDay() {
		reset();
	}

	public void reset() {
		// 跨天重置之前，先结算一下奖励
		if (startBattleId > 0) {
			BattleConfig battleConfig = BattleManager.instance().get(startBattleId);
			List<Goods> goods = new ArrayList<>();
			for (int randomId : battleConfig.BattleBoxRandomId) {
				goods.addAll(PlayerHelper.randomReward(player, randomId));
			}
			MailHelper.sendMail(player.getPlayerId(), 7, goods);
		}

		dayReward = false;
		if (startBattleId == 0) {
			nextBattleId();
		}
		List<Integer> randomBuffs = BattleHelper.randomBuffs(startBattleId, DungeonTypeEnum.ShiLuoZhenJing.getId());
		this.randomBuff.addAll(randomBuffs);
		if (battleStage == -1) {
			battleStage = 1;
		}
	}

	private void nextBattleId() {
		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(DungeonTypeEnum.ShiLuoZhenJing.getId());
		for (BattleConfig battleConfig : battleTypeList) {
			if (battleConfig.preBattle == completeBattleId) {
				startBattleId = battleConfig.ID;
				break;
			}
		}
	}

	public void battleCompleted() {
		this.completeBattleId = startBattleId;
		if (this.battleStage == 10) {
			nextBattleId();
			this.randomBuff.clear();
			List<Integer> randomBuffs = BattleHelper.randomBuffs(startBattleId, DungeonTypeEnum.ShiLuoZhenJing.getId());
			this.randomBuff.addAll(randomBuffs);
		}
		this.battleStage++;
		// 1打到9，然后10本关
		if (this.battleStage == 11) {
			this.battleStage = 1;
		}
	}

	@Override
	public int check(int id, int subId) {
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
	public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request) {

		if (!request.getWin()) {
			return ResultObject.success();
		}
		ChapterModule chapterModule = player.getChapterModule();
		int attackingDungeonId = chapterModule.getAttackingDungeonId();
		BattleConfig battleConfig = BattleManager.instance().get(attackingDungeonId);
		battleCompleted();
		// 通关奖励，用公式计算
		return ResultObject.success(calcRewardInfos(battleConfig.Level, battleStage));
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

	public boolean isDayReward() {
		return dayReward;
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

	private List<RewardInfo> calcRewardInfos(int level, int stage) {
		List<RewardInfo> rewardInfos = new ArrayList<>();

		for (int i = 0; i < GlobalConst.LostScripturesRewards.length; i++) {
			int rewardId = GlobalConst.LostScripturesRewards[i];
			int rewardCount = calcRewardCount(GlobalConst.LostScripturesRewardsNum[i], level, stage);
			List<RewardInfo> resources = PlayerHelper.addResources(player, rewardId, rewardCount, OpType.ShiLuoZhenJing);
			rewardInfos.addAll(resources);
		}
		return rewardInfos;
	}

	private int calcRewardCount(float[] rewardsParam, int level, int stage) {

		// 计算关卡的值
		int battle = (level - 1) * 10 + stage;
		// 计算整体公式的值
		double result = (rewardsParam[0] * Math.pow((battle + rewardsParam[1]), rewardsParam[2]) + rewardsParam[3]) / rewardsParam[4] + 1;
		// 向下取整并乘以 n
		int finalResult = (int) (Math.floor(result) * rewardsParam[5]);
		return finalResult;
	}

}
