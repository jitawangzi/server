package cn.game.games.net.game.module.battle;
/** 
* 角色一场战斗中的行为
* @date 2022年11月7日 上午11:06:08 
* @author YYB 
*/
public interface IRoleBattleAction {
	/**
	 * 累计造成伤害
	 * @param roleId
	 * @return
	 */
	public int cumulativeDamage(int roleId);
	/**
	 * 累计击杀怪物（最后一击）
	 * @param roleId
	 * @return
	 */
	public int cumulativeKillMonsters(int roleId);
	/**
	 * 累计对实体/灵体类怪物造成伤害
	 * @param roleId
	 * @param isGhost 是否灵体类
	 * @return
	 */
	public int cumulativeToMonstersDamage(int roleId, boolean isGhost);
	/**
	 * 体型超过>width*height范围的怪物造成伤害
	 * @param roleId
	 * @param width 宽
	 * @param height 高
	 * @return
	 */
	public int cumulativeToShapeMonstersDamage(int roleId, int width, int height);
	/**
	 * 总攻击的次数
	 * @param roleId
	 * @return
	 */
	public int cumulativeAttackTimes(int roleId);
	/**
	 * 第一个进行攻击的角色
	 * @return roleId
	 */
	public int firstAttackRole();
	/**
	 * 被攻击次数
	 * @param roleId
	 * @return
	 */
	public int cumulativeInjuredTimes(int roleId);
	/**
	 * 第一个被攻击的角色
	 * @return
	 */
	public int firstAttackedRole();
	/**
	 * 是否第一次行动就击杀怪物
	 * @param roleId
	 * @return
	 */
	public boolean fristActionAndKillMonster(int roleId);
	/**
	 * 战斗开始时血量>startHp%,战斗结束时血量<=endHp%
	 * @param roleId
	 * @param startHp
	 * @param endHp
	 * @return
	 */
	public boolean isHpRange(int roleId, int startHp, int endHp);
	/**
	 * 战斗开始时血量<=startHp%
	 * @param roleId
	 * @param startHp
	 * @return
	 */
	public boolean isHpRange(int roleId, int startHp);
	/**
	 * 受到buff影响次数
	 * @param roleId
	 * @param buffType buff类型
	 * @return
	 */
	public int buffEffectCount(int roleId, int buffType);
	/**
	 * 死亡次数
	 * @param roleId
	 * @return
	 */
	public int dieTimes(int roleId);
	/**
	 * 受到单次伤害>自身最大生命值的?%
	 * @param roleId
	 * @param percent
	 * @return
	 */
	public int cumulativeInjuredLowerLimit(int roleId, int percent);
	/**
	 * 使用恢复类道具治疗次数
	 * @param roleId
	 * @return
	 */
	public int cumulativeUseRecoveryItemTimes(int roleId);
	/**
	 * SAN值到达低段次数
	 * @param roleId
	 * @param lowerThanValue 低于的值
	 * @return
	 */
	public int sanValueRangeTimes(int roleId, int lowerThanValue);
	/**
	 * 击杀怪物数量
	 * @param roleId
	 * @return
	 */
	public int killMonsterCount(int roleId);
	/**
	 * 战斗中对同一目标造成伤害次数
	 * @param roleId
	 * @param attackNum 攻击次数
	 * @return
	 */
	public boolean attackOneTargetThan(int roleId, int attackNum);
	/**
	 * 被击退次数
	 * @param roleId
	 * @return
	 */
	public int knockbackTimes(int roleId);
	/**
	 * 在攻击时有某种buff次数
	 * @param roleId
	 * @param buffType
	 * @return
	 */
	public int hasBuffWhenAttack(int roleId, int buffType);
	/**
	 * 攻击被护卫次数
	 * @param roleId
	 * @return
	 */
	public int attackBeEscorted(int roleId);
	/**
	 * 攻击被闪避次数
	 * @param roleId
	 * @return
	 */
	public int attackBeDodged(int roleId);
	/**
	 * 暴击次数
	 * @param roleId
	 * @return
	 */
	public int criticalStrike(int roleId);
	/**
	 * 中debuff次数
	 * @param roleId
	 * @param buffType
	 * @return
	 */
	public int hadDeBuff(int roleId, int buffType);


}
