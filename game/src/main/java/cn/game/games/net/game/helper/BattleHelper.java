package cn.game.games.net.game.helper;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.game.games.cache.entity.Hero;
import cn.game.protocol.generated.config.AttrEffectConfigConfig;
import cn.game.protocol.generated.config.AttributeVlalueConfig;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GamePlayRandomBuffConfig;
import cn.game.protocol.generated.config.HCBattleConfig;
import cn.game.protocol.generated.config.HeroBreakConfig;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.manager.AttrEffectConfigManager;
import cn.game.protocol.generated.manager.AttributeVlalueManager;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.GamePlayRandomBuffManager;
import cn.game.protocol.generated.manager.HCBattleManager;
import cn.game.protocol.generated.manager.HeroBreakManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.util.IntMapWrapper;
import cn.game.util.Rnd;

public class BattleHelper {

	public static int getBattleLevelType(int id) {
		return id / 10000;
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
		// 卡牌经验，(roundup(lv/10,0)*lv+10)*体力消耗
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
		BattleConfig battleConfig = BattleManager.instance().getNullable(battleId);
		if (battleConfig == null) {
			return ret;
		}
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

	/** 
	 * 根据已经完成的关卡id，找到下一个可以打的关卡id
	 * @param battleType
	 * @param completeBattleId
	 * @return
	 */
	public static int nextStartBattleId(DungeonTypeEnum battleType, int completeBattleId) {
		int startBattleId = 0;
		List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(battleType.getId());
		for (BattleConfig battleConfig : battleTypeList) {
			if (battleConfig.preBattle == completeBattleId) {
				startBattleId = battleConfig.ID;
				break;
			}
		}
		return startBattleId;
	}

	/** 
	 * 判断当前时间是否在23:30之后
	 * @return
	 */
	public static boolean isNowAfter2330() {

		LocalTime now = LocalTime.now();
		LocalTime start = LocalTime.of(23, 30);

		return now.isAfter(start); 
	}

	/** 
	 * 计算属性的战斗力
	 * @param attrMap
	 * @return
	 */
	public static float calcCombat(IntMapWrapper attrMap) {

		float combat = 0;
		Iterator<Entry<Integer, Integer>> iterator = attrMap.getMap().entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<java.lang.Integer, java.lang.Integer> entry = (Map.Entry<java.lang.Integer, java.lang.Integer>) iterator.next();

			AttrEffectConfigConfig attrEffectConfigConfig = AttrEffectConfigManager.instance().get(entry.getKey());
			float combatEffectiveness = attrEffectConfigConfig.CombatEffectiveness / 10000f;
			combat += combatEffectiveness * entry.getValue();
		}
		return combat;
	}

	public static IntMapWrapper makeHeroAttr(Hero hero) {
		IntMapWrapper heroAttrMap = new IntMapWrapper();

		HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
		// 初始属性
		AttributeVlalueConfig attributeVlalueConfig = AttributeVlalueManager.instance().get(heroConfig.InitialAttributeId);
		heroAttrMap.addAll(attributeVlalueConfig.AttributeVlalue);
		// 等级成长属性
		if (hero.getLevel() > 1) {
			attributeVlalueConfig = AttributeVlalueManager.instance().get(heroConfig.GrowthAttributeId);
			attributeVlalueConfig.AttributeVlalue.forEach((k, v) -> {
				heroAttrMap.add(k, v * (hero.getLevel() - 1));
			});
		}
		// 突破属性
		HeroBreakConfig uiInitialQualityStar = HeroBreakManager.instance().getUIInitialQualityStar(hero.getQuality(), hero.getStar());
		attributeVlalueConfig = AttributeVlalueManager.instance().get(uiInitialQualityStar.BreakOneTime);
		heroAttrMap.addAll(attributeVlalueConfig.AttributeVlalue);
		return heroAttrMap;
	}

	public static int calcHeroCombat(Hero hero) {
		return (int) calcCombat(makeHeroAttr(hero));
	}

}
