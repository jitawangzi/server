package cn.game.games.net.game.module.battle;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.RankConfig;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.enume.WelfareTypeEnum;
import cn.game.protocol.generated.manager.RankManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.Rnd;

/**    
 * 世界boss
 * 2024年8月12日 上午11:17:45
 * @author SYQ
 */
public class WorldBossBattle extends XiYouBattleHandler {

	private long cumulativeDamage; // 今日累计伤害
	private long maxDamageToday; // 今日最高伤害
	private int battleTimes; // 今日已经挑战次数。每天第一次免费，以后需要付费购买。
	private int buyTimes; // 今日付费购买次数

	private int maxDamage; // 历史最高伤害,扫荡使用
	@JsonIgnore
	@Deprecated
	private int rewardIndex;
	private int rewardId;

	@Override
	void newDay() {
		reset();
	}

	private void reset() {
		cumulativeDamage = 0;
		maxDamageToday = 0;
		battleTimes = 0;
		buyTimes = 0;
	}

	@Override
	public int check(int id, int subId) {
//		ChapterModule chapterModule = player.getModule(ChapterModule.class);
//		BattleConfig battleConfig = BattleManager.instance().get(id);
		if (BattleHelper.isNowAfter2330()) {
			return ErrorMsgEnum.not_open.getId();
		}
		if (battleTimes >= GlobalConst.JDTMFreeCnt) {
			if (buyTimes <= battleTimes - GlobalConst.JDTMFreeCnt) {
				return ErrorMsgEnum.times_limit.getId();
			}
			if (battleTimes >= GlobalConst.JDTMFreeCnt + GlobalConst.JDTMPayCnt) {
				return ErrorMsgEnum.times_limit.getId();
			}
		}
		return super.check(id, subId);
	}

	@Override
	public int battleStart(int id) {

		return 0;
	}

	@Override
	public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request) {
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		end(request.getDamage());

		return ResultObject.success();

	}

	private void end(int damage) {
		cumulativeDamage += damage;
		if (damage > maxDamageToday) {
			maxDamageToday = damage;
		}
		if (damage > maxDamage) {
			maxDamage = damage;
		}
		battleTimes++;
//		player.getPointRewardModule().addReward(PointRewardType.WorldBoss, damage, damage, damage)
		RankConfig rankConfig = RankManager.instance().get(RankType.WorldBoss.ID);
		if (cumulativeDamage >= rankConfig.Request) {
			RankService.getInstance().updateScore(player.getServerId(), RankType.WorldBoss, player.getPlayerId(), cumulativeDamage);
		}

	}

	@Override
	public ResultObject<List<RewardInfo>> quickEnd(int id, int subId, boolean isWin) {
		if (maxDamage == 0) {
			return ResultObject.fail(ErrorMsgEnum.request_parameter_error.getId());
		}
		if (!player.hasWelfare(WelfareTypeEnum.JDTMSweep)) {
			return ResultObject.fail(ErrorMsgEnum.welfare_check_error.getId());
		}
		int damage = (int) (maxDamage * Rnd.nextDouble(0.9, 1.1));
		end(damage);
		return ResultObject.success();
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.WorldBoss.getId();
	}

	public int getBuyTimes() {
		return buyTimes;
	}

	public void setBuyTimes(int buyTimes) {
		this.buyTimes = buyTimes;
	}

	public long getCumulativeDamage() {
		return cumulativeDamage;
	}

	public void setCumulativeDamage(long cumulativeDamage) {
		this.cumulativeDamage = cumulativeDamage;
	}

	public long getMaxDamageToday() {
		return maxDamageToday;
	}

	public int getBattleTimes() {
		return battleTimes;
	}

	public int getMaxDamage() {
		return maxDamage;
	}

	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}

}
