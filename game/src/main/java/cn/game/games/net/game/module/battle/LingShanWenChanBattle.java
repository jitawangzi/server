package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.List;

import cn.game.core.exception.LogicException;
import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.LingShanConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.LingShanManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.GameUtil;

/**    
 * 灵山问禅
 * 2025年7月1日 11:35:04
 * @author SYQ
 */
public class LingShanWenChanBattle extends XiYouBattleHandler {
	   
	/** 最后通关的层数 */
	private  int lastCompleteFloor;
	/** 每日挑战+扫荡次数 */
	private int battleTimes;
	/** 付费购买次数 */
	private int payTimes;
	/** 领取过宝箱奖励的索引  */
	private long rewardBattleIds;
	/** 本次战斗的起始id/层数,应该是lastCompleteFloor 的下一层 */
	private int startFloor;

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
	public int checkCustom(int id, int subId) {
		LingShanConfig floorConfig = getFloorConfig(subId, id); 
		int power = player.getAttrModule().getPower();
		int floorInConfig = getFloorInConfig(subId, floorConfig);
		
		int Capacity  = floorConfig.Capacity + floorInConfig * floorConfig.CapacityAdd;
		boolean ok = floorConfig != null && (subId == lastCompleteFloor + 1
				|| Capacity <= power 
				);
		if (!ok) {
			return ErrorMsgEnum.pre_condition_check_error.getId();
		}
		if (subId <= lastCompleteFloor) {
			return ErrorMsgEnum.request_parameter_error.getId();
		}
		return 0 ; 
	}
	
	@Override
	public int battleStart(int id,int subId) {
		if (this.startFloor == 0) {
			this.startFloor = subId;
		}
		return 0;
	}

	@Override
	public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request) {
		BattleModule battleModule = player.getModule(BattleModule.class);
		int attackingType = battleModule.getAttackingType();
		int attackingSubId = battleModule.getAttackingSubId(); 
		if (request.getWin()) {
			lastCompleteFloor = attackingSubId; // 记录最后通关的层数
			// 应该都是第一次通关，可能跳关了
			RankService.getInstance()
					.setScoreAsync(player.getServerId(), RankType.LingShanWenChan, player.getPlayerId(), attackingSubId);
			return ResultObject.success();
		} else { // 失败了，最终结算
			// 每一关的通过奖励
			List<int[][]> allRewardList = new ArrayList<>();
 			for (int floor = startFloor; floor < attackingSubId; floor++) {
				allRewardList.add(getReward(floor,false)); 
			}
			// 上一关的扫荡奖励
			if (attackingSubId > 1) {
				allRewardList.add(getReward(attackingSubId - 1,true)); 
			}
		
			List<RewardInfo> rewards = new ArrayList<>();
			
			for (int[][] array : allRewardList) {
				List<RewardInfo> resources = PlayerHelper.addResources(player, array, OpType.LingShanWenChan); 
				rewards.addAll(resources); 
			}
			
			return ResultObject.success(rewards);
		}
	}
	
	private int[][] getReward(int floor,boolean sweep) {
		LingShanConfig floorConfig = getFloorConfig(floor);
		int floorInConfig = getFloorInConfig(floor, floorConfig);
		return GameUtil.arrayAddition(sweep?floorConfig.SDReward: floorConfig.Reward,sweep?floorConfig.SDRewardAdd: floorConfig.RewardAdd, floorInConfig);
	}

	@Override
	public ResultObject<List<RewardInfo>> quickEnd(int id, int subId, boolean isWin) {
		if (!isWin) {
			return ResultObject.success();
		}
		if (subId != lastCompleteFloor) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.getId());
		}
		if (battleTimes >= GlobalConst.LingshanChallangeCost.length) {
			return ResultObject.fail(ErrorMsgEnum.times_limit.getId());
		}
		int cost = GlobalConst.LingshanChallangeCost[battleTimes];
		PlayerHelper.delResources(player, Asset.diamond.ID, cost, OpType.LingShanWenChan);
		battleTimes++;
		
		List<RewardInfo> resources = PlayerHelper.addResources(player, getReward(lastCompleteFloor,true), OpType.LingShanWenChan); 
		return ResultObject.success(resources);
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.LingShanWenChan.getId();
	}
	
	/** 
	 * 获取当前层所在的配置
	 * @return
	 */
	public LingShanConfig getCurFloorConfig() {
		return getFloorConfig(lastCompleteFloor); 
	}
	/** 
	 * 根据层数获取到对应的配置。 
	 * @param floor
	 * @return
	 */
	private LingShanConfig getFloorConfig(int floor) {
		if (floor == 0) {
			return null ; 
		}
		List<LingShanConfig> list = LingShanManager.instance().list(); 
		for (LingShanConfig config : list) {
			if (floor >= config.StartFloor && floor <= config.EndFloor) {
				return config;
			}
		}
		return list.getLast(); 
	}
	/** 
	 * 根据层数获取到对应的配置。 
	 * @param floor
	 * @param battleId  这里其实用不到，只是校验数据
	 * @return
	 */
	private LingShanConfig getFloorConfig(int floor,int battleId) {
		List<LingShanConfig> list = LingShanManager.instance().list(); 
		for (LingShanConfig config : list) {
			if (floor >= config.StartFloor && floor <= config.EndFloor) {
				return config;
			}
		}
		throw new LogicException(ErrorMsgEnum.request_parameter_error.getId(), "floor: " + floor + ", battleId: " + battleId);
	}
	/** 
	 * 根据当前层数所在的配置，获取到该层数在层数区间中的层数，用来计算奖励等
	 * @param floor
	 * @return
	 */
	private int getFloorInConfig(int floor,LingShanConfig config) {
		if (config == null) {
			return 0;
		}
		if (floor < config.StartFloor || floor > config.EndFloor) {
			return 0;
		}
		return floor - config.StartFloor;
	}

	public int getBattleTimes() {
		return battleTimes;
	}

	public int getPayTimes() {
		return payTimes;
	}

	public long getRewardBattleIds() {
		return rewardBattleIds;
	}

	public void setPayTimes(int payTimes) {
		this.payTimes = payTimes;
	}

	public void setBattleTimes(int battleTimes) {
		this.battleTimes = battleTimes;
	}

	public void setRewardBattleIds(long rewardBattleIds) {
		this.rewardBattleIds = rewardBattleIds;
	}


	public int getLastCompleteFloor() {
		return lastCompleteFloor;
	}


	public void setLastCompleteFloor(int lastCompleteFloor) {
		this.lastCompleteFloor = lastCompleteFloor;
	}


	public int getStartFloor() {
		return startFloor;
	}


	public void setStartFloor(int startFloor) {
		this.startFloor = startFloor;
	}
	
	

}
