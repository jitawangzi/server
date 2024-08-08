package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class ShiLuoZhenJingBattle extends XiYouBattleHandler {

	/** 当前可打的失落真经非战斗关卡，从1开始，如果为0表示打战斗关卡 */
	private int battleStage = -1;
	/** 每日奖励是否已经领取了。 */
	private boolean dayReward;
	/** 最新通关的battleId */
	private int completeBattleId;
	/** 手动挑战的起始关卡，当前可打的战役id（还没通关的）战斗关卡。 */
	private int startBattleId;
	/** 下一关可打的战役随机buff  增益或者减益buff， HeroBUFF表id*/
	private List<Integer> randomBuff = new ArrayList<>();

	@Override
	void newDay() {
		dayReward = false;
		reset();
	}

	public void reset() {
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
		if (this.battleStage == 0) {
			nextBattleId();
			this.randomBuff.clear();
			List<Integer> randomBuffs = BattleHelper.randomBuffs(startBattleId, DungeonTypeEnum.ShiLuoZhenJing.getId());
			this.randomBuff.addAll(randomBuffs);
		}
		this.battleStage++;
		// 1打到9，然后0本关
		if (this.battleStage == 10) {
			this.battleStage = 0;
		}
	}

	public int check(int id) {
		if (id != this.startBattleId || this.battleStage != 0) {
			return ErrorMsgEnum.request_parameter_error.ID;
		}
		return super.check(id);
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
		// TODO 通关奖励，用公式计算
		battleCompleted();
		return ResultObject.success();

	}

	@Override
	public ResultObject<List<RewardInfo>> quickEnd(int id, int subId, boolean isWin) {
		if (id != startBattleId || subId != battleStage) {
			return ResultObject.fail(ErrorMsgEnum.request_parameter_error.ID);
		}
		if (!isWin) {
			return ResultObject.success();
		}
		battleCompleted();
		return ResultObject.success();
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

	public void setDayReward(boolean dayReward) {
		this.dayReward = dayReward;
	}

}
