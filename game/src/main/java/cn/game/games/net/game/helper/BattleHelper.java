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

	/** 普通关卡 */
	public static final int LEVEL_TYPE_BATTLELEVEL = 1;
	/** 剧情关卡 */
	public static final int LEVEL_TYPE_PLOT = 2;
	/** 挑战关卡 */
	public static final int LEVEL_TYPE_EXPLORE = 3;

	/** 普通关卡里的意识空间 */
	public static final int BATTLE_LEVEL_TYPE_AWARENESSSPACE = 1;

	public static int getBattleLevelType(int id) {
		return id / 10000;
	}
	/**
	 * @Description 随机出来一个事件类型，去除指定的类型
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
	 * @Description 战斗结束时，计算获得的玩家经验
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
	 * @Description 战斗结束时，计算获得的角色经验
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
	 * @Description 战斗结束时，计算获得的金币
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
	 * @return
	 */
	public static List<Integer> randomBuffs(int battleId) {
		List<Integer> ret = new ArrayList<>();
		BattleConfig battleConfig = BattleManager.instance().get(battleId);
		for (int[] buffs : battleConfig.Cnt) {
			for (int i = 0; i < buffs[1]; i++) {
				List<GamePlayRandomBuffConfig> gamePlayMarkBuffCategoryList = GamePlayRandomBuffManager.instance().getGamePlayMarkBuffCategoryList(1, buffs[0]);
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


}
