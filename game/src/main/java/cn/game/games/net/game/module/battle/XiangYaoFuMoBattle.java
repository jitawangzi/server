package cn.game.games.net.game.module.battle;

import java.util.List;

import cn.game.core.exception.LogicException;
import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 降妖伏魔
 * 2025年6月19日 11:44:20
 * @author SYQ
 */
public class XiangYaoFuMoBattle extends XiYouBattleHandler {
	   
	/** 最后通关的id（Battle表id） */
	private int lastCompleteBattleId;
	/** 每日扫荡次数 */
	private int sweepTimes;

	public XiangYaoFuMoBattle() {
	};


	@Override
	void newDay() {
		reset();
	}
	/** 
	 * 每天重置数据
	 */
	public void reset() {
		sweepTimes = 0;
	}

	@Override
	public int battleStart(int id) {
		if (BattleHelper.isComplete(lastCompleteBattleId, id)) {
			return 0;
		}
		BattleConfig battleConfig = BattleManager.instance().getNullable(id);

		boolean ok = battleConfig != null && battleConfig.preBattle == lastCompleteBattleId;
		return ok ? 0 : ErrorMsgEnum.pre_condition_check_error.getId();
	}

	@Override
	public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request) {
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		int attackingType = chapterModule.getAttackingType();
		BattleConfig battleConfig = BattleManager.instance().get(chapterModule.getAttackingDungeonId());
		if (request.getWin()) {
			if (battleConfig.preBattle == lastCompleteBattleId) { // 第一次通关
				List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.FirstPassReward, OpType.XiangYaoChuMo);
				lastCompleteBattleId = battleConfig.ID;
				return ResultObject.success(reward);
			}
		}
		return ResultObject.success();

	}

	@Override
	public ResultObject<List<RewardInfo>> quickEnd(int id, int subId, boolean isWin) {
		if (!isWin) {
			return ResultObject.success();
		}
		if (id != lastCompleteBattleId) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.getId());
		}
		if (sweepTimes >= GlobalConst.DemonsChallangeCost.length) {
			return ResultObject.fail(ErrorMsgEnum.times_limit.getId());
		}
		int cost = GlobalConst.DemonsChallangeCost[sweepTimes];
		PlayerHelper.delResources(player, Asset.diamond.ID, cost, OpType.XiangYaoChuMo);
		sweepTimes++;

		BattleConfig battleConfig = BattleManager.instance().get(id);
		return ResultObject.success(PlayerHelper.addReward(player, battleConfig.SweepReward, OpType.XiangYaoChuMo));
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.XiangYaoFuMo.getId();
	}

	public int getLastCompleteBattleId() {
		return lastCompleteBattleId;
	}

	public int getSweepTimes() {
		return sweepTimes;
	}

}
