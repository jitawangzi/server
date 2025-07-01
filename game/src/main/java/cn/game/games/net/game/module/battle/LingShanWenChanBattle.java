package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
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
 * 灵山问禅
 * 2025年7月1日 11:35:04
 * @author SYQ
 */
public class LingShanWenChanBattle extends XiYouBattleHandler {
	   
	/** 最后通关的id（Battle表id） */
	private int lastCompleteBattleId;
	/** 每日挑战+扫荡次数 */
	private int battleTimes;
	/** 付费购买次数 */
	private int payTimes;
	/** 领取过宝箱奖励的索引  */
	private long rewardBattleIds;

	/** 本次战斗的起始id/层数,应该是lastCompleteBattleId 的下一关 */
	private int startBattleId;

	/** 本次战斗累计的奖励，完全结束时，下发给客户端 */
	private transient List<RewardInfo> rewards = new ArrayList<>();

	public LingShanWenChanBattle() {
	};


	@Override
	void newDay() {
		reset();
	}
	/** 
	 * 每天重置数据
	 */
	public void reset() {
		battleTimes = 0;
		payTimes = 0;
	}

	@Override
	public int battleStart(int id) {
		BattleConfig battleConfig = BattleManager.instance().getNullable(id);
		int power = player.getAttrModule().getPower();

		boolean ok = battleConfig != null && (battleConfig.preBattle == lastCompleteBattleId
//				|| (battleConfig.preBattle == 0 && battleConfig.powerCondition <= power)
		);
		if (!ok) {
			return ErrorMsgEnum.pre_condition_check_error.getId();
		}
		startBattleId = BattleHelper.nextBattleConfig(lastCompleteBattleId).ID;
		return 0;
	}

	@Override
	public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request) {
		BattleModule chapterModule = player.getModule(BattleModule.class);
		int attackingType = chapterModule.getAttackingType();
		BattleConfig battleConfig = BattleManager.instance().get(chapterModule.getAttackingDungeonId());
		if (request.getWin()) {
			// 应该都是第一次通关，可能跳关了
			BattleConfig preConfig = battleConfig;
			while ((preConfig = BattleManager.instance().getNullable(preConfig.preBattle)) != null) {
				if (preConfig.ID == startBattleId) {
					break;
				}
				List<RewardInfo> reward1 = PlayerHelper.addReward(player, battleConfig.FirstPassReward, OpType.LingShanWenChan);
				List<RewardInfo> reward2 = PlayerHelper.addReward(player, battleConfig.SweepReward, OpType.LingShanWenChan);
				rewards.addAll(reward1);
				rewards.addAll(reward2);
			}

			lastCompleteBattleId = battleConfig.ID;
			return ResultObject.success();
		} else { // 失败了，最终结算
			// 如果没有成功通过一关，则获得上一关的扫荡奖励
			if (lastCompleteBattleId > 0 && BattleHelper.nextBattleConfig(lastCompleteBattleId).ID == startBattleId) {
				List<RewardInfo> reward = PlayerHelper.addReward(player, BattleManager.instance().get(lastCompleteBattleId).SweepReward,
						OpType.LingShanWenChan);
				rewards.addAll(reward);
				return ResultObject.success(rewards);
			}
			// 否则 什么也不获得(通关时已经获得那一关的扫荡奖励了)
			return ResultObject.success(rewards);
		}
	}

	@Override
	public ResultObject<List<RewardInfo>> quickEnd(int id, int subId, boolean isWin) {
		if (!isWin) {
			return ResultObject.success();
		}
		if (id != lastCompleteBattleId) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.getId());
		}
		if (battleTimes >= GlobalConst.LingshanChallangeCost.length) {
			return ResultObject.fail(ErrorMsgEnum.times_limit.getId());
		}
		int cost = GlobalConst.LingshanChallangeCost[battleTimes];
		PlayerHelper.delResources(player, Asset.diamond.ID, cost, OpType.LingShanWenChan);
		battleTimes++;

		BattleConfig battleConfig = BattleManager.instance().get(id);
		return ResultObject.success(PlayerHelper.addReward(player, battleConfig.SweepReward, OpType.LingShanWenChan));
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.LingShanWenChan.getId();
	}

	public int getLastCompleteBattleId() {
		return lastCompleteBattleId;
	}

}
