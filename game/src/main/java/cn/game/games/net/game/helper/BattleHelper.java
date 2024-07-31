package cn.game.games.net.game.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.EventTriggerConfig;
import cn.game.protocol.generated.config.GamePlayRandomBuffConfig;
import cn.game.protocol.generated.config.HCBattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.EventTriggerManager;
import cn.game.protocol.generated.manager.GamePlayRandomBuffManager;
import cn.game.protocol.generated.manager.HCBattleManager;
import cn.game.util.Rnd;

public class BattleHelper {

	public static int getBattleLevelType(int id) {
		return id / 10000;
	}
	/**
	 * 随机出来一个事件类型，去除指定的类型
	 * @param excludeIds
	 * @return
	 */
	public static int randomBattleEventType(List<Integer> excludeIds) {
		Collection<EventTriggerConfig> list = EventTriggerManager.getInstance().list();
		int total = 0;
		for (EventTriggerConfig config : list) {
			if (excludeIds != null && excludeIds.contains(config.getId())) {
				continue;
			}
			total += config.getProportion();
		}

		int rand = Rnd.nextInt(total);
		int current = 0;

		for (EventTriggerConfig config : list) {

			if (excludeIds != null && excludeIds.contains(config.getId())) {
				continue;
			}

			current += config.getProportion();
			if (rand < current) { 
				return config.getType();
			}
		}
		return -1;
	}

	public static int roundUpLevel(int level) {
		int ret = level / 10;
		return level % 10 == 0 ? ret : ret + 1;

	}
	/**
	 * 战斗结束时，计算获得的玩家经验
	 * @param level
	 *            当前等级
	 * @param ap
	 *            体力消耗
	 * @return
	 */
	public static int calcPlayerExp(int level, int ap) {
		//账号经验，(roundup(lv/10,0)*lv+50)*体力消耗

		return (roundUpLevel(level) * level + 50) * ap;
		

	}
	/**
	 * 战斗结束时，计算获得的角色经验
	 * @param level
	 *            当前等级
	 * @param ap
	 *            体力消耗
	 * @return
	 */
	public static int calcRoleExp(int level, int ap) {
		//		卡牌经验，(roundup(lv/10,0)*lv+10)*体力消耗
		return (roundUpLevel(level) * level + 10) * ap;

	}
	/**
	 * 战斗结束时，计算获得的金币
	 * @param level
	 *            当前等级
	 * @param ap
	 *            体力消耗
	 * @return
	 */
	public static int calcCoin(int level, int ap) {
		// 		金币，roundup(lv/10,0)*lv*500*体力消耗
		return roundUpLevel(level) * level * 500 * ap;
	}

	/** 
	 * 随机一个战役的buff
	 * @param battleId
	 * @param playType 1 道心、心魔  2梦魇秘境 {@link GamePlayRandomBuffConfig#GamePlayMark}
	 * @return
	 */
	public static List<Integer> randomBuffs(int battleId, int playType) {
		List<Integer> ret = new ArrayList<>();
		BattleConfig battleConfig = BattleManager.instance().get(battleId);
		for (int[] buffs : battleConfig.Cnt) {
			for (int i = 0; i < buffs[1]; i++) {
				List<GamePlayRandomBuffConfig> gamePlayMarkBuffCategoryList = GamePlayRandomBuffManager.instance().getGamePlayMarkBuffCategoryList(playType,
						buffs[0]);
				GamePlayRandomBuffConfig randomWeighableElement = Rnd.randomWeighableElement(gamePlayMarkBuffCategoryList);
				ret.add(randomWeighableElement.GamePlayBuffId);
			}
		}
		return ret;
	}

	public static int hcFailRewardId(int hcBattleId, int battleTime) {

		HCBattleConfig battleConfig = HCBattleManager.instance().get(hcBattleId);

//		3;5;8
//		≤3——第1个
//		3＜x≤5——第2个
//		5＜x≤8——第3个,> 8 第三个。 

//		int battleTime = request.getBattleTime();
		int[] failRandomTrigger = battleConfig.FailRandomTrigger;
		int index = 0;
		for (int i = 0; i < failRandomTrigger.length; i++) {
			if (battleTime <= failRandomTrigger[i]) {
				index = i;
			}
		}
		if (battleTime >= failRandomTrigger[failRandomTrigger.length - 1]) {
			index = failRandomTrigger.length - 1;
		}
		return battleConfig.FailRandom[index];
	}

	/** 
	 * 根据已经完成的关卡id，判断某一关是否完成了
	 * 
	 * @param completeBattleId
	 * @param checkId
	 * @return
	 */
	public static boolean isComplete(int completeBattleId, int id) {
		return isPreBattle(completeBattleId, id);
	}

	/** 
	 * 判断某关，是否是某关的前置关卡
	 * @param id 当前关卡id
	 * @param preId  需要判断的前置关卡id
	 * @return
	 */
	public static boolean isPreBattle(int id, int preId) {
		if (id == 0) {
			return false;
		}
		if (preId == id) {
			return true;
		}
		BattleConfig battleConfig = BattleManager.instance().get(id);
		BattleConfig preConfig = battleConfig;
		while ((preConfig = BattleManager.instance().getNullable(preConfig.preBattle)) != null) {
			if (preConfig.ID == preId) {
				return true;
			}
		}
		return false;
	}

	public static BattleConfig nextBattleConfig(int id) {
		BattleConfig battleConfig = BattleManager.instance().getNullable(id);
		if (battleConfig == null) {
			return null;
		}
		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(battleConfig.BattleType);
		if (battleTypeList == null) {
			return null;
		}
		for (BattleConfig config : battleTypeList) {
			if (config.preBattle == id) {
				return config;
			}
		}
		return null;
	}

}
