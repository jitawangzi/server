package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 召唤物表
 * 
 * 工具生成的，不要手动修改
 */
 public class SummonedConfig {

	/** id -- id */
	private final int id;		
	/** 类型 -- 1-普通 2-精英 3-BOSS 4-特殊 */
	private final int type;		
	/** 范围 -- x方向|y方向 */
	private final List<Integer> range;		
	/** 稀有度 -- 1-蓝 2-紫 3-橙 */
	private final int quality;		
	/** 职业 -- 1-守护 2-先锋 3-异能 4-突袭 5-祈愿 6-黯灭 */
	private final int occupation;		
	/** 主动技能列表 */
	private final List<Integer> activeSkillList;		
	/** 被动技能列表 */
	private final List<Integer> passiveSkillList;		
	/** 是否碰撞 -- 1-碰撞 0-不碰撞 */
	private final boolean isCollide;		
	/** 可行走 -- 1-可行走 0-不可行走 */
	private final boolean walkable;		
	/** 可选中 -- 1-可选中 0-不可选中 */
	private final boolean selectable;		
	/** 受范围影响 -- 1-受范围影响 0-不受范围影响 */
	private final boolean rangeEffect;		
	/** 回合类型 -- 1-独立回合 2-主人回合 3-任意单位回合 4-目标回合 */
	private final int roundType;		
	/** 回合数 -- 999-无限回合 */
	private final int round;		
	/** 属性继承主人 */
	private final boolean isAttrExtendMaster;		
	/** 生命 */
	private final int hp;		
	/** 生命成长值 */
	private final int hpGrow;		
	/** san值 */
	private final int san;		
	/** san值成长 */
	private final int sanGrow;		
	/** 攻击 */
	private final int attack;		
	/** 攻击力成长值 */
	private final int attackGrow;		
	/** 速度 */
	private final int speed;		
	/** 速度成长值 */
	private final int speedGrow;		
	/** 防御 */
	private final int defense;		
	/** 防御成长值 */
	private final int defenseGrow;		
	/** 命中 -- 改为概率值 */
	private final int hit;		
	/** 命中成长值 */
	private final int hitGrow;		
	/** 闪避 -- 改为概率值 */
	private final int dodge;		
	/** 闪避成长值 */
	private final int dodgeGrow;		
	/** 暴击 -- 改为概率值 */
	private final int critical;		
	/** 暴击成长值 */
	private final int criticalGrow;		
	/** 暴击伤害 -- 固定值 改为概率值 */
	private final int criticalDamage;		
	/** 击退抗性 */
	private final int repelResistance;		
	/** 眩晕抗性 */
	private final int vertigoResistance;		
	/** 异常抗性 */
	private final int abnormalResistance;		
	/** 弱化抗性 */
	private final int weakenResistance;		
	/** 流血抗性 */
	private final int bleedingResistance;		
	/** 腐蚀抗性 */
	private final int corrosionResistance;		
	/** 暴击抗性 */
	private final int critResistance;		

	public SummonedConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 类型
		String rangeString = element.getAttribute("range"); // 范围
		if (rangeString != null && rangeString.length() > 0) {
			String[] rangeStrings = rangeString.split("\\|"); 
			List<Integer> range = new ArrayList<Integer>(rangeStrings.length) ; 
			for (int i = 0; i < rangeStrings.length; i++) {
				Integer temp = Integer.parseInt(rangeStrings[i]);
				range.add(temp);
			}
			this.range = com.google.common.collect.ImmutableList.copyOf(range);						
		} else {
			this.range = java.util.Collections.emptyList();
		}
		this.quality = Integer.parseInt(element.getAttribute("quality") == null || element.getAttribute("quality").length() == 0 ? "0"
			: element.getAttribute("quality")); // 稀有度
		this.occupation = Integer.parseInt(element.getAttribute("occupation") == null || element.getAttribute("occupation").length() == 0 ? "0"
			: element.getAttribute("occupation")); // 职业
		String activeSkillListString = element.getAttribute("activeSkillList"); // 主动技能列表
		if (activeSkillListString != null && activeSkillListString.length() > 0) {
			String[] activeSkillListStrings = activeSkillListString.split("\\|"); 
			List<Integer> activeSkillList = new ArrayList<Integer>(activeSkillListStrings.length) ; 
			for (int i = 0; i < activeSkillListStrings.length; i++) {
				Integer temp = Integer.parseInt(activeSkillListStrings[i]);
				activeSkillList.add(temp);
			}
			this.activeSkillList = com.google.common.collect.ImmutableList.copyOf(activeSkillList);						
		} else {
			this.activeSkillList = java.util.Collections.emptyList();
		}
		String passiveSkillListString = element.getAttribute("passiveSkillList"); // 被动技能列表
		if (passiveSkillListString != null && passiveSkillListString.length() > 0) {
			String[] passiveSkillListStrings = passiveSkillListString.split("\\|"); 
			List<Integer> passiveSkillList = new ArrayList<Integer>(passiveSkillListStrings.length) ; 
			for (int i = 0; i < passiveSkillListStrings.length; i++) {
				Integer temp = Integer.parseInt(passiveSkillListStrings[i]);
				passiveSkillList.add(temp);
			}
			this.passiveSkillList = com.google.common.collect.ImmutableList.copyOf(passiveSkillList);						
		} else {
			this.passiveSkillList = java.util.Collections.emptyList();
		}
		this.isCollide = Boolean.parseBoolean(element.getAttribute("isCollide") == null || element.getAttribute("isCollide").length() == 0 ? "false"
			: element.getAttribute("isCollide")); // 是否碰撞
		this.walkable = Boolean.parseBoolean(element.getAttribute("walkable") == null || element.getAttribute("walkable").length() == 0 ? "false"
			: element.getAttribute("walkable")); // 可行走
		this.selectable = Boolean.parseBoolean(element.getAttribute("selectable") == null || element.getAttribute("selectable").length() == 0 ? "false"
			: element.getAttribute("selectable")); // 可选中
		this.rangeEffect = Boolean.parseBoolean(element.getAttribute("rangeEffect") == null || element.getAttribute("rangeEffect").length() == 0 ? "false"
			: element.getAttribute("rangeEffect")); // 受范围影响
		this.roundType = Integer.parseInt(element.getAttribute("roundType") == null || element.getAttribute("roundType").length() == 0 ? "0"
			: element.getAttribute("roundType")); // 回合类型
		this.round = Integer.parseInt(element.getAttribute("round") == null || element.getAttribute("round").length() == 0 ? "0"
			: element.getAttribute("round")); // 回合数
		this.isAttrExtendMaster = Boolean.parseBoolean(element.getAttribute("isAttrExtendMaster") == null || element.getAttribute("isAttrExtendMaster").length() == 0 ? "false"
			: element.getAttribute("isAttrExtendMaster")); // 属性继承主人
		this.hp = Integer.parseInt(element.getAttribute("hp") == null || element.getAttribute("hp").length() == 0 ? "0"
			: element.getAttribute("hp")); // 生命
		this.hpGrow = Integer.parseInt(element.getAttribute("hpGrow") == null || element.getAttribute("hpGrow").length() == 0 ? "0"
			: element.getAttribute("hpGrow")); // 生命成长值
		this.san = Integer.parseInt(element.getAttribute("san") == null || element.getAttribute("san").length() == 0 ? "0"
			: element.getAttribute("san")); // san值
		this.sanGrow = Integer.parseInt(element.getAttribute("sanGrow") == null || element.getAttribute("sanGrow").length() == 0 ? "0"
			: element.getAttribute("sanGrow")); // san值成长
		this.attack = Integer.parseInt(element.getAttribute("attack") == null || element.getAttribute("attack").length() == 0 ? "0"
			: element.getAttribute("attack")); // 攻击
		this.attackGrow = Integer.parseInt(element.getAttribute("attackGrow") == null || element.getAttribute("attackGrow").length() == 0 ? "0"
			: element.getAttribute("attackGrow")); // 攻击力成长值
		this.speed = Integer.parseInt(element.getAttribute("speed") == null || element.getAttribute("speed").length() == 0 ? "0"
			: element.getAttribute("speed")); // 速度
		this.speedGrow = Integer.parseInt(element.getAttribute("speedGrow") == null || element.getAttribute("speedGrow").length() == 0 ? "0"
			: element.getAttribute("speedGrow")); // 速度成长值
		this.defense = Integer.parseInt(element.getAttribute("defense") == null || element.getAttribute("defense").length() == 0 ? "0"
			: element.getAttribute("defense")); // 防御
		this.defenseGrow = Integer.parseInt(element.getAttribute("defenseGrow") == null || element.getAttribute("defenseGrow").length() == 0 ? "0"
			: element.getAttribute("defenseGrow")); // 防御成长值
		this.hit = Integer.parseInt(element.getAttribute("hit") == null || element.getAttribute("hit").length() == 0 ? "0"
			: element.getAttribute("hit")); // 命中
		this.hitGrow = Integer.parseInt(element.getAttribute("hitGrow") == null || element.getAttribute("hitGrow").length() == 0 ? "0"
			: element.getAttribute("hitGrow")); // 命中成长值
		this.dodge = Integer.parseInt(element.getAttribute("dodge") == null || element.getAttribute("dodge").length() == 0 ? "0"
			: element.getAttribute("dodge")); // 闪避
		this.dodgeGrow = Integer.parseInt(element.getAttribute("dodgeGrow") == null || element.getAttribute("dodgeGrow").length() == 0 ? "0"
			: element.getAttribute("dodgeGrow")); // 闪避成长值
		this.critical = Integer.parseInt(element.getAttribute("critical") == null || element.getAttribute("critical").length() == 0 ? "0"
			: element.getAttribute("critical")); // 暴击
		this.criticalGrow = Integer.parseInt(element.getAttribute("criticalGrow") == null || element.getAttribute("criticalGrow").length() == 0 ? "0"
			: element.getAttribute("criticalGrow")); // 暴击成长值
		this.criticalDamage = Integer.parseInt(element.getAttribute("criticalDamage") == null || element.getAttribute("criticalDamage").length() == 0 ? "0"
			: element.getAttribute("criticalDamage")); // 暴击伤害
		this.repelResistance = Integer.parseInt(element.getAttribute("repelResistance") == null || element.getAttribute("repelResistance").length() == 0 ? "0"
			: element.getAttribute("repelResistance")); // 击退抗性
		this.vertigoResistance = Integer.parseInt(element.getAttribute("vertigoResistance") == null || element.getAttribute("vertigoResistance").length() == 0 ? "0"
			: element.getAttribute("vertigoResistance")); // 眩晕抗性
		this.abnormalResistance = Integer.parseInt(element.getAttribute("abnormalResistance") == null || element.getAttribute("abnormalResistance").length() == 0 ? "0"
			: element.getAttribute("abnormalResistance")); // 异常抗性
		this.weakenResistance = Integer.parseInt(element.getAttribute("weakenResistance") == null || element.getAttribute("weakenResistance").length() == 0 ? "0"
			: element.getAttribute("weakenResistance")); // 弱化抗性
		this.bleedingResistance = Integer.parseInt(element.getAttribute("bleedingResistance") == null || element.getAttribute("bleedingResistance").length() == 0 ? "0"
			: element.getAttribute("bleedingResistance")); // 流血抗性
		this.corrosionResistance = Integer.parseInt(element.getAttribute("corrosionResistance") == null || element.getAttribute("corrosionResistance").length() == 0 ? "0"
			: element.getAttribute("corrosionResistance")); // 腐蚀抗性
		this.critResistance = Integer.parseInt(element.getAttribute("critResistance") == null || element.getAttribute("critResistance").length() == 0 ? "0"
			: element.getAttribute("critResistance")); // 暴击抗性
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
	
	public List<Integer> getRange() {
		return range;
	}
	
	public int getQuality() {
		return quality;
	}
	
	public int getOccupation() {
		return occupation;
	}
	
	public List<Integer> getActiveSkillList() {
		return activeSkillList;
	}
	
	public List<Integer> getPassiveSkillList() {
		return passiveSkillList;
	}
	
	public boolean getIsCollide() {
		return isCollide;
	}
	
	public boolean getWalkable() {
		return walkable;
	}
	
	public boolean getSelectable() {
		return selectable;
	}
	
	public boolean getRangeEffect() {
		return rangeEffect;
	}
	
	public int getRoundType() {
		return roundType;
	}
	
	public int getRound() {
		return round;
	}
	
	public boolean getIsAttrExtendMaster() {
		return isAttrExtendMaster;
	}
	
	public int getHp() {
		return hp;
	}
	
	public int getHpGrow() {
		return hpGrow;
	}
	
	public int getSan() {
		return san;
	}
	
	public int getSanGrow() {
		return sanGrow;
	}
	
	public int getAttack() {
		return attack;
	}
	
	public int getAttackGrow() {
		return attackGrow;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public int getSpeedGrow() {
		return speedGrow;
	}
	
	public int getDefense() {
		return defense;
	}
	
	public int getDefenseGrow() {
		return defenseGrow;
	}
	
	public int getHit() {
		return hit;
	}
	
	public int getHitGrow() {
		return hitGrow;
	}
	
	public int getDodge() {
		return dodge;
	}
	
	public int getDodgeGrow() {
		return dodgeGrow;
	}
	
	public int getCritical() {
		return critical;
	}
	
	public int getCriticalGrow() {
		return criticalGrow;
	}
	
	public int getCriticalDamage() {
		return criticalDamage;
	}
	
	public int getRepelResistance() {
		return repelResistance;
	}
	
	public int getVertigoResistance() {
		return vertigoResistance;
	}
	
	public int getAbnormalResistance() {
		return abnormalResistance;
	}
	
	public int getWeakenResistance() {
		return weakenResistance;
	}
	
	public int getBleedingResistance() {
		return bleedingResistance;
	}
	
	public int getCorrosionResistance() {
		return corrosionResistance;
	}
	
	public int getCritResistance() {
		return critResistance;
	}
	
}
