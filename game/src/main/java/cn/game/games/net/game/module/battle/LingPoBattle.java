package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.module.player.pointreward.PointRewardType;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.protobuf.BattleMsg.BattleSpiritualInfo;

/**    
 * 灵魄之战
 * 2024年7月31日 下午12:02:38
 * @author SYQ
 */
public class LingPoBattle {
	   
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

	public void init(Player player) {
		reset(player);
	}

	/** 
	 * 每天重置数据
	 */
	public void reset(Player player) {

		battleTimes = 0;
		adsGetBattleTimes = false;
		changeBattleTimes = 0;
		if (battleId == 0) {
			initBattleId(player);
		}
		this.randomBuff.clear();
		randomBuff.addAll(BattleHelper.randomBuffs(battleId, 3));
	}

	/** 
	 * 获取可以打的，最新的战役id
	 * @param type  {@link BattleConfig#BattleType} 
	 * @return
	 */
	private void initBattleId(Player player) {

		int mainBattleId = player.getChapterModule().getFightMainBattleId();

		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(DungeonTypeEnum.LingPo.getId());
		for (BattleConfig battleConfig : battleTypeList) {
			if (battleConfig.preBattle == mainBattleId) {
				battleId = battleConfig.ID;
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

}
