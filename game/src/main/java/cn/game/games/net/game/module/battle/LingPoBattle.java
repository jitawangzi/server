package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.player.pointreward.PointRewardType;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.WelfareTypeEnum;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 灵魄之战
 * 2024年7月31日 下午12:02:38
 * @author SYQ
 */
public class LingPoBattle extends XiYouBattleHandler {
	   
	/** 当前难度，（Battle表id） */
	private int battleId;
	/** 战役随机buff  增益或者减益buff， HeroBUFF表id*/
	private List<Integer> randomBuff = new ArrayList<>();
	// 当天累计挑战过的次数。
	private int battleTimes;
	// 变更难度次数
	private int changeBattleTimes;
	/** 是否需要看广告获得挑战次数，打完第一次，第二次需要先看广告。 */
	private boolean adsGetBattleTimes;

	public LingPoBattle() {
	};


	@Override
	void newDay() {
		reset();
	}
	/** 
	 * 每天重置数据
	 */
	public void reset() {

		player.getCurrencyModule().setCount(Asset.SpiritBattlePoint.ID, 0);
		battleTimes = 0;
		adsGetBattleTimes = false;
		changeBattleTimes = 0;
		if (battleId == 0) {
			initBattleId();
		}
		this.randomBuff.clear();
		randomBuff.addAll(BattleHelper.randomBuffs(battleId, 3));
	}

	public void updateBattleId() {
		if (battleId == 0) {
			initBattleId();
		}
		if (randomBuff.isEmpty()) {
			randomBuff.addAll(BattleHelper.randomBuffs(battleId, 3));
		}
	}

	/** 
	 * 获取可以打的，最新的战役id
	 * @param type  {@link BattleConfig#BattleType} 
	 * @return
	 */
	private void initBattleId() {

		int mainBattleId = player.getBattleModule().getMainBattleHighest();
		if (mainBattleId == 0) {
			return;
		}
		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(DungeonTypeEnum.LingPo.getId());
		for (BattleConfig battleConfig : battleTypeList) {
			if (BattleHelper.isComplete(mainBattleId, battleConfig.preBattle)) {
				battleId = battleConfig.ID;
			} else {
				break;
			}
		}
	}

	public void battleEnd() {
		this.battleTimes++;
		if (this.battleTimes < 2) {
			adsGetBattleTimes = true;
		}
	}

	public BattleSpiritualInfo buildBattleInfo(Player player) {

		BattleSpiritualInfo.Builder builder = BattleSpiritualInfo.newBuilder();
		builder.setBattleTimes(battleTimes);
		builder.setBattleId(battleId);
		builder.setAdsGetBattleTimes(adsGetBattleTimes);
		builder.setChangeBattleTimes(changeBattleTimes);
		builder.addAllRandomBuff(randomBuff);

		List<Integer> activeRewardList = player.getPointRewardModule().getActiveRewardList(PointRewardType.LingPo);
		builder.addAllRewardIndex(activeRewardList);

		return builder.build();
	}


	public List<Integer> getRandomBuff() {
		return randomBuff;
	}

	public void setRandomBuff(List<Integer> randomBuff) {
		this.randomBuff = randomBuff;
	}

	public int getBattleId() {
		return battleId;
	}

	public void setBattleId(int battleId) {
		this.battleId = battleId;
	}

	public int getBattleTimes() {
		return battleTimes;
	}

	public void setBattleTimes(int battleTimes) {
		this.battleTimes = battleTimes;
	}

	public int getChangeBattleTimes() {
		return changeBattleTimes;
	}

	public void setChangeBattleTimes(int changeBattleTimes) {
		this.changeBattleTimes = changeBattleTimes;
	}

	public boolean isAdsGetBattleTimes() {
		return adsGetBattleTimes;
	}

	public void setAdsGetBattleTimes(boolean adsGetBattleTimes) {
		this.adsGetBattleTimes = adsGetBattleTimes;
	}


	@Override
	public int battleStart(int id) {
		if (id != this.getBattleId()) {
			return ErrorMsgEnum.request_parameter_error.getId();
		}
		if (BattleHelper.isNowAfter2330()) {
			return ErrorMsgEnum.not_open.getId();
		}
		if (this.isAdsGetBattleTimes()) {
			return ErrorMsgEnum.not_watch_ads.getId();
		}
		if (this.getBattleTimes() >= 2) {
			return ErrorMsgEnum.times_limit.getId();
		}
		return 0;
	}

	@Override
	public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request, BattleMsg.BattleFieldEndResponse_13000004.Builder response) {
		BattleModule battleModule = player.getModule(BattleModule.class);
		int attackingType = battleModule.getAttackingType();
		BattleConfig battleConfig = BattleManager.instance().get(battleModule.getAttackingId());
		int pointAdd = 0;
		if (request.getWin()) {
			pointAdd = GlobalConst.SpiritBattleVicpoint;
		} else {
			int[][] spiritBattleFailpoint = GlobalConst.SpiritBattleFailpoint;
			for (int i = spiritBattleFailpoint.length - 1; i >= 0; i--) {
				if (request.getBattleTime() >= spiritBattleFailpoint[i][0]) {
					pointAdd = spiritBattleFailpoint[i][1];
					break;
				}
			}
		}

		int welfareValue = player.getWelfareValue(WelfareTypeEnum.LingPoBattleIntegral);
		if (welfareValue > 0) {
			pointAdd = (int) (pointAdd * (1 + welfareValue / 10000.0));
		}

		List<RewardInfo> reward = PlayerHelper.addResources(player, Asset.SpiritBattlePoint.ID, pointAdd, OpType.LingPoBattle);
		battleEnd();
		return ResultObject.success(reward);
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.LingPo.getId();
	}


}
