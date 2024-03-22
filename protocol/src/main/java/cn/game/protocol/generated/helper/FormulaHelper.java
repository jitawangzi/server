package cn.game.protocol.generated.helper;

import cn.game.util.MathUtils;

public class FormulaHelper {

	/** 
	 * 公式id:101,污染值影响血量恢复量下降百分比
	 *
	 * @param pollution 污染值
	 * @param pollutionMax 最大污染值
	 * @return
	 */
	public static double getHpRecoverSubPercentByPollution(float pollution,float pollutionMax) {
		return pollution / pollutionMax * 0.8;
	}

	/** 
	 * 公式id:102,污染值影响血量恢复量公式
	 *
	 * @param recover 恢复值
	 * @param pollution 污染值
	 * @param pollutionMax 最大污染值
	 * @return
	 */
	public static int getChangeHpRecoverByPollution(float recover,float pollution,float pollutionMax) {
		return MathUtils.round(recover * (1 - FormulaHelper.getHpRecoverSubPercentByPollution(pollution, pollutionMax)));
	}

	/** 
	 * 公式id:103,污染值影响被暴击增加百分比
	 *
	 * @param pollution 污染值
	 * @param pollutionMax 最大污染值
	 * @return
	 */
	public static double getCriticalAddPercentByPollution(float pollution,float pollutionMax) {
		return 0.2 * pollution / pollutionMax;
	}

	/** 
	 * 公式id:104,污染值影响伤害值公式
	 *
	 * @param critical 暴击率
	 * @param pollution 污染值
	 * @param pollutionMax 最大污染值
	 * @return
	 */
	public static int getChangeCriticalByPollution(float critical,float pollution,float pollutionMax) {
		return MathUtils.round(critical + FormulaHelper.getCriticalAddPercentByPollution(pollution, pollutionMax) * 100);
	}

	/** 
	 * 公式id:105,污染值影响San值损失增加百分比
	 *
	 * @param pollution 污染值
	 * @param pollutionMax 最大污染值
	 * @return
	 */
	public static double getSanSubPercentByPollution(float pollution,float pollutionMax) {
		return pollution / pollutionMax * 0.5;
	}

	/** 
	 * 公式id:106,污染值影响San值损失公式
	 *
	 * @param san san值
	 * @param pollution 污染值
	 * @param pollutionMax 最大污染值
	 * @return
	 */
	public static int getChangeSanByPollution(float san,float pollution,float pollutionMax) {
		return MathUtils.round(san * (1 + FormulaHelper.getSanSubPercentByPollution(pollution, pollutionMax)));
	}

	/** 
	 * 公式id:1001,计算角色身体属性
	 *
	 * @param base 配表里的基础值
	 * @param level 角色等级
	 * @param growUp 配表里的成长值
	 * @return
	 */
	public static int getRoleBodyAttribute(float base,float level,float growUp) {
		return MathUtils.round(base + (level - 1) * growUp);
	}

	/** 
	 * 公式id:2001,返回是否命中
	 *
	 * @param hitRate 攻方命中率
	 * @param dodgeRate 守方闪避率
	 * @return
	 */
	public static boolean getIsHit(float hitRate,float dodgeRate) {
		return MathUtils.randomInt(0, 100) < FormulaHelper.getHitRate(hitRate, dodgeRate);
	}

	/** 
	 * 公式id:2002,返回是否暴击
	 *
	 * @param criticalRate 攻方的暴击率
	 * @param critResisRate 守方的抗暴率
	 * @return
	 */
	public static boolean getIsCritical(float criticalRate,float critResisRate) {
		return MathUtils.randomInt(0, 100) < (criticalRate - critResisRate);
	}

	/** 
	 * 公式id:2003,返回命中率
	 *
	 * @param hitRate 攻击方命中率
	 * @param dodgeRate 防御方闪避率
	 * @return
	 */
	public static double getHitRate(float hitRate,float dodgeRate) {
		return hitRate * (1 - dodgeRate / 100) * 1.2;
	}

	/** 
	 * 公式id:2004,返回防御百分比
	 *
	 * @param defense 防御力
	 * @return
	 */
	public static double getDefensePercent(float defense) {
		return (defense * 0.02) / (1 + (defense * 0.02));
	}

	/** 
	 * 公式id:2005,返回技能伤害量/治疗量
	 *
	 * @param attack 攻击力
	 * @param damagePercent 技能伤害百分比
	 * @param defense 防御力（治疗传0）
	 * @param criticalDamage 暴击伤害（未暴击传100）
	 * @param addValue 伤害/治疗加值
	 * @return
	 */
	public static int getDamage(float attack,float damagePercent,float defense,float criticalDamage,float addValue) {
		return MathUtils.round((attack * (damagePercent / 100)) * (1 - FormulaHelper.getDefensePercent(defense)) * (criticalDamage / 100) + addValue);
	}

	/** 
	 * 公式id:2006,返回buff伤害量/恢复量
	 *
	 * @param value buff数值
	 * @param level buff层数
	 * @return
	 */
	public static int getBuffDamage(float value,float level) {
		return MathUtils.round(value * level);
	}

	/** 
	 * 公式id:2007,返回当前最大值伤害
	 *
	 * @param value 当前值伤害
	 * @return
	 */
	public static int getDamageCT(float value) {
		return MathUtils.round(MathUtils.min(value * 0.1, -1));
	}

	/** 
	 * 公式id:2008,返回移动消耗ap值
	 *
	 * @param dis 移动距离
	 * @param movement 移动力
	 * @return
	 */
	public static int getMoveApCost(float dis,float movement) {
		return MathUtils.ceil(dis / movement);
	}

	/** 
	 * 公式id:2009,返回Buff是否命中
	 *
	 * @param hitRate buff命中率
	 * @param resisRate 守方抗性
	 * @return
	 */
	public static boolean getBuffIsHit(float hitRate,float resisRate) {
		return MathUtils.randomInt(0, 100) < FormulaHelper.getBuffHitRate(hitRate, resisRate);
	}

	/** 
	 * 公式id:2010,返回Buff命中率
	 *
	 * @param hitRate buff命中率
	 * @param resisRate 守方抗性
	 * @return
	 */
	public static double getBuffHitRate(float hitRate,float resisRate) {
		return hitRate * MathUtils.max((1 - resisRate / 100), 0);
	}

	/** 
	 * 公式id:2011,返回技能San伤害量
	 *
	 * @param attack 攻击力
	 * @param damagePercent 技能伤害百分比
	 * @param sanTotal 守方san总值
	 * @param sanGrow 守方san成长值
	 * @param level 守方等级
	 * @return
	 */
	public static int getSanDamage(float attack,float damagePercent,float sanTotal,float sanGrow,float level) {
		return MathUtils.round((attack * (damagePercent / 100)) / (((sanGrow + level * 2) / (sanTotal * 0.5)) * 10));
	}

}

