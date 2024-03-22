package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 陷阱表
 * 
 * 工具生成的，不要手动修改
 */
 public class TrapConfig {

	/** id -- 根据地貌重新设计id */
	private final int id;		
	/** 名字 */
	private final String name;		
	/** 美术资源 */
	private final String resource;		
	/** 动作 */
	private final String spine;		
	/** 形态 -- 1-固态 2-液态 3-气态 */
	private final int form;		
	/** 是否碰撞 -- 1-碰撞 0-不碰撞 */
	private final boolean isCollide;		
	/** 是否独立 -- 拥有独立的回合 1-独立 0-不独立 */
	private final boolean isIndependent;		
	/** 是否显示 -- 在AT条上显示 1-显示 0-不显示 */
	private final boolean isDisplay;		
	/** 可行走 -- 1-可行走 0-不可行走 */
	private final boolean walkable;		
	/** 可选中 -- 1-可选中 0-不可选中 */
	private final boolean selectable;		
	/** 受范围影响 -- 1-受范围影响 0-不受范围影响 */
	private final boolean rangeEffect;		
	/** 互动类型 -- 条件1:结果1|条件2:结果2 条件类型 1-触碰 2-攻击|物理 3-攻击|火焰 结果类型 1-位移 2-摧毁 3-爆炸 */
	private final List<Entry<Integer,Integer>> interact;		
	/** 占用空间 */
	private final int area;		
	/** 持续回合 -- 999为一直存在 */
	private final int round;		
	/** 稀有度 -- 1-蓝 2-紫 3-橙 */
	private final int quality;		
	/** 职业 -- 1-守护 2-先锋 3-异能 4-突袭 5-祈愿 6-黯灭 */
	private final int occupation;		
	/** 伤害类型 -- 1-物理 2-法术 */
	private final int damageType;		
	/** 主动技能列表 */
	private final List<Integer> activeSkillList;		
	/** 被动技能列表 */
	private final List<Integer> passiveSkillList;		
	/** 生命 */
	private final int hp;		
	/** 生命成长值 */
	private final int hpGrow;		
	/** 物理攻击 */
	private final int attack;		
	/** 物理攻击成长值 */
	private final int attackGrow;		
	/** 法术攻击 */
	private final int spellPower;		
	/** 法术攻击成长值 */
	private final int spellPowerGrow;		
	/** 防御 */
	private final int defense;		
	/** 防御成长值 */
	private final int defenseGrow;		
	/** 抗性 */
	private final int spellResistance;		
	/** 抗性成长值 */
	private final int spellResistanceGrow;		
	/** 速度 */
	private final int speed;		
	/** 速度成长值 */
	private final int speedGrow;		
	/** 暴击 */
	private final int critical;		
	/** 暴击成长值 */
	private final int criticalGrow;		
	/** 格挡 */
	private final int block;		
	/** 格挡成长值 */
	private final int blockGrow;		
	/** 命中 */
	private final int hit;		
	/** 命中成长值 */
	private final int hitGrow;		
	/** 闪避 */
	private final int dodge;		
	/** 闪避成长值 */
	private final int dodgeGrow;		
	/** 暴击伤害 -- 固定值 */
	private final int criticalDamage;		
	/** EP -- 固定值 */
	private final int ep;		
	/** 抗暴率 */
	private final int antiCriticalRate;		
	/** 爆伤减免 */
	private final int criticalDamageReduction;		
	/** 伤害加成 */
	private final int damageBonus;		
	/** 伤害减免 */
	private final int damageReduction;		
	/** 格挡率 */
	private final int blockRate;		
	/** 格挡反击率 */
	private final int blockCounterattackRate;		
	/** 格挡破除率 */
	private final int blockBreakingRate;		
	/** 护卫概率 */
	private final int guardProbability;		
	/** 护卫减伤率 */
	private final int guardReduction;		
	/** 护卫破除率 */
	private final int guardBreaking;		
	/** 支援反击率 */
	private final int supportCounterattackRate;		

	public TrapConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 名字
		this.resource = element.getAttribute("resource"); // 美术资源
		this.spine = element.getAttribute("spine"); // 动作
		this.form = Integer.parseInt(element.getAttribute("form") == null || element.getAttribute("form").length() == 0 ? "0"
			: element.getAttribute("form")); // 形态
		this.isCollide = Boolean.parseBoolean(element.getAttribute("isCollide") == null || element.getAttribute("isCollide").length() == 0 ? "false"
			: element.getAttribute("isCollide")); // 是否碰撞
		this.isIndependent = Boolean.parseBoolean(element.getAttribute("isIndependent") == null || element.getAttribute("isIndependent").length() == 0 ? "false"
			: element.getAttribute("isIndependent")); // 是否独立
		this.isDisplay = Boolean.parseBoolean(element.getAttribute("isDisplay") == null || element.getAttribute("isDisplay").length() == 0 ? "false"
			: element.getAttribute("isDisplay")); // 是否显示
		this.walkable = Boolean.parseBoolean(element.getAttribute("walkable") == null || element.getAttribute("walkable").length() == 0 ? "false"
			: element.getAttribute("walkable")); // 可行走
		this.selectable = Boolean.parseBoolean(element.getAttribute("selectable") == null || element.getAttribute("selectable").length() == 0 ? "false"
			: element.getAttribute("selectable")); // 可选中
		this.rangeEffect = Boolean.parseBoolean(element.getAttribute("rangeEffect") == null || element.getAttribute("rangeEffect").length() == 0 ? "false"
			: element.getAttribute("rangeEffect")); // 受范围影响
		String interactString = element.getAttribute("interact"); // 互动类型
		if (interactString != null && interactString.length() > 0) {
			String[] interactStrings = interactString.split("\\|"); 
			List<Entry<Integer,Integer>> interact = new ArrayList<Entry<Integer,Integer>>(interactStrings.length) ; 
			for (int i = 0; i < interactStrings.length; i++) {
			    String[] split = interactStrings[i].split(":", 2);
				interact.add(new Entry<Integer,Integer>()	{
					@Override
					public Integer setValue(Integer value) {
						return null;
					}
					@Override
					public Integer getValue() {
						try {
							return Integer.parseInt(split[1]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						throw new NullPointerException();	
					}
					@Override
					public Integer getKey() {
						try {
							return Integer.parseInt(split[0]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						throw new NullPointerException();
					}
				}) ; 
			}

			this.interact = com.google.common.collect.ImmutableList.copyOf(interact);						
		} else {
			this.interact = java.util.Collections.emptyList();
		}
		this.area = Integer.parseInt(element.getAttribute("area") == null || element.getAttribute("area").length() == 0 ? "0"
			: element.getAttribute("area")); // 占用空间
		this.round = Integer.parseInt(element.getAttribute("round") == null || element.getAttribute("round").length() == 0 ? "0"
			: element.getAttribute("round")); // 持续回合
		this.quality = Integer.parseInt(element.getAttribute("quality") == null || element.getAttribute("quality").length() == 0 ? "0"
			: element.getAttribute("quality")); // 稀有度
		this.occupation = Integer.parseInt(element.getAttribute("occupation") == null || element.getAttribute("occupation").length() == 0 ? "0"
			: element.getAttribute("occupation")); // 职业
		this.damageType = Integer.parseInt(element.getAttribute("damageType") == null || element.getAttribute("damageType").length() == 0 ? "0"
			: element.getAttribute("damageType")); // 伤害类型
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
		this.hp = Integer.parseInt(element.getAttribute("hp") == null || element.getAttribute("hp").length() == 0 ? "0"
			: element.getAttribute("hp")); // 生命
		this.hpGrow = Integer.parseInt(element.getAttribute("hpGrow") == null || element.getAttribute("hpGrow").length() == 0 ? "0"
			: element.getAttribute("hpGrow")); // 生命成长值
		this.attack = Integer.parseInt(element.getAttribute("attack") == null || element.getAttribute("attack").length() == 0 ? "0"
			: element.getAttribute("attack")); // 物理攻击
		this.attackGrow = Integer.parseInt(element.getAttribute("attackGrow") == null || element.getAttribute("attackGrow").length() == 0 ? "0"
			: element.getAttribute("attackGrow")); // 物理攻击成长值
		this.spellPower = Integer.parseInt(element.getAttribute("spellPower") == null || element.getAttribute("spellPower").length() == 0 ? "0"
			: element.getAttribute("spellPower")); // 法术攻击
		this.spellPowerGrow = Integer.parseInt(element.getAttribute("spellPowerGrow") == null || element.getAttribute("spellPowerGrow").length() == 0 ? "0"
			: element.getAttribute("spellPowerGrow")); // 法术攻击成长值
		this.defense = Integer.parseInt(element.getAttribute("defense") == null || element.getAttribute("defense").length() == 0 ? "0"
			: element.getAttribute("defense")); // 防御
		this.defenseGrow = Integer.parseInt(element.getAttribute("defenseGrow") == null || element.getAttribute("defenseGrow").length() == 0 ? "0"
			: element.getAttribute("defenseGrow")); // 防御成长值
		this.spellResistance = Integer.parseInt(element.getAttribute("spellResistance") == null || element.getAttribute("spellResistance").length() == 0 ? "0"
			: element.getAttribute("spellResistance")); // 抗性
		this.spellResistanceGrow = Integer.parseInt(element.getAttribute("spellResistanceGrow") == null || element.getAttribute("spellResistanceGrow").length() == 0 ? "0"
			: element.getAttribute("spellResistanceGrow")); // 抗性成长值
		this.speed = Integer.parseInt(element.getAttribute("speed") == null || element.getAttribute("speed").length() == 0 ? "0"
			: element.getAttribute("speed")); // 速度
		this.speedGrow = Integer.parseInt(element.getAttribute("speedGrow") == null || element.getAttribute("speedGrow").length() == 0 ? "0"
			: element.getAttribute("speedGrow")); // 速度成长值
		this.critical = Integer.parseInt(element.getAttribute("critical") == null || element.getAttribute("critical").length() == 0 ? "0"
			: element.getAttribute("critical")); // 暴击
		this.criticalGrow = Integer.parseInt(element.getAttribute("criticalGrow") == null || element.getAttribute("criticalGrow").length() == 0 ? "0"
			: element.getAttribute("criticalGrow")); // 暴击成长值
		this.block = Integer.parseInt(element.getAttribute("block") == null || element.getAttribute("block").length() == 0 ? "0"
			: element.getAttribute("block")); // 格挡
		this.blockGrow = Integer.parseInt(element.getAttribute("blockGrow") == null || element.getAttribute("blockGrow").length() == 0 ? "0"
			: element.getAttribute("blockGrow")); // 格挡成长值
		this.hit = Integer.parseInt(element.getAttribute("hit") == null || element.getAttribute("hit").length() == 0 ? "0"
			: element.getAttribute("hit")); // 命中
		this.hitGrow = Integer.parseInt(element.getAttribute("hitGrow") == null || element.getAttribute("hitGrow").length() == 0 ? "0"
			: element.getAttribute("hitGrow")); // 命中成长值
		this.dodge = Integer.parseInt(element.getAttribute("dodge") == null || element.getAttribute("dodge").length() == 0 ? "0"
			: element.getAttribute("dodge")); // 闪避
		this.dodgeGrow = Integer.parseInt(element.getAttribute("dodgeGrow") == null || element.getAttribute("dodgeGrow").length() == 0 ? "0"
			: element.getAttribute("dodgeGrow")); // 闪避成长值
		this.criticalDamage = Integer.parseInt(element.getAttribute("criticalDamage") == null || element.getAttribute("criticalDamage").length() == 0 ? "0"
			: element.getAttribute("criticalDamage")); // 暴击伤害
		this.ep = Integer.parseInt(element.getAttribute("ep") == null || element.getAttribute("ep").length() == 0 ? "0"
			: element.getAttribute("ep")); // EP
		this.antiCriticalRate = Integer.parseInt(element.getAttribute("antiCriticalRate") == null || element.getAttribute("antiCriticalRate").length() == 0 ? "0"
			: element.getAttribute("antiCriticalRate")); // 抗暴率
		this.criticalDamageReduction = Integer.parseInt(element.getAttribute("criticalDamageReduction") == null || element.getAttribute("criticalDamageReduction").length() == 0 ? "0"
			: element.getAttribute("criticalDamageReduction")); // 爆伤减免
		this.damageBonus = Integer.parseInt(element.getAttribute("damageBonus") == null || element.getAttribute("damageBonus").length() == 0 ? "0"
			: element.getAttribute("damageBonus")); // 伤害加成
		this.damageReduction = Integer.parseInt(element.getAttribute("damageReduction") == null || element.getAttribute("damageReduction").length() == 0 ? "0"
			: element.getAttribute("damageReduction")); // 伤害减免
		this.blockRate = Integer.parseInt(element.getAttribute("blockRate") == null || element.getAttribute("blockRate").length() == 0 ? "0"
			: element.getAttribute("blockRate")); // 格挡率
		this.blockCounterattackRate = Integer.parseInt(element.getAttribute("blockCounterattackRate") == null || element.getAttribute("blockCounterattackRate").length() == 0 ? "0"
			: element.getAttribute("blockCounterattackRate")); // 格挡反击率
		this.blockBreakingRate = Integer.parseInt(element.getAttribute("blockBreakingRate") == null || element.getAttribute("blockBreakingRate").length() == 0 ? "0"
			: element.getAttribute("blockBreakingRate")); // 格挡破除率
		this.guardProbability = Integer.parseInt(element.getAttribute("guardProbability") == null || element.getAttribute("guardProbability").length() == 0 ? "0"
			: element.getAttribute("guardProbability")); // 护卫概率
		this.guardReduction = Integer.parseInt(element.getAttribute("guardReduction") == null || element.getAttribute("guardReduction").length() == 0 ? "0"
			: element.getAttribute("guardReduction")); // 护卫减伤率
		this.guardBreaking = Integer.parseInt(element.getAttribute("guardBreaking") == null || element.getAttribute("guardBreaking").length() == 0 ? "0"
			: element.getAttribute("guardBreaking")); // 护卫破除率
		this.supportCounterattackRate = Integer.parseInt(element.getAttribute("supportCounterattackRate") == null || element.getAttribute("supportCounterattackRate").length() == 0 ? "0"
			: element.getAttribute("supportCounterattackRate")); // 支援反击率
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getResource() {
		return resource;
	}
	
	public String getSpine() {
		return spine;
	}
	
	public int getForm() {
		return form;
	}
	
	public boolean getIsCollide() {
		return isCollide;
	}
	
	public boolean getIsIndependent() {
		return isIndependent;
	}
	
	public boolean getIsDisplay() {
		return isDisplay;
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
	
	public List<Entry<Integer,Integer>> getInteract() {
		return interact;
	}
	
	public int getArea() {
		return area;
	}
	
	public int getRound() {
		return round;
	}
	
	public int getQuality() {
		return quality;
	}
	
	public int getOccupation() {
		return occupation;
	}
	
	public int getDamageType() {
		return damageType;
	}
	
	public List<Integer> getActiveSkillList() {
		return activeSkillList;
	}
	
	public List<Integer> getPassiveSkillList() {
		return passiveSkillList;
	}
	
	public int getHp() {
		return hp;
	}
	
	public int getHpGrow() {
		return hpGrow;
	}
	
	public int getAttack() {
		return attack;
	}
	
	public int getAttackGrow() {
		return attackGrow;
	}
	
	public int getSpellPower() {
		return spellPower;
	}
	
	public int getSpellPowerGrow() {
		return spellPowerGrow;
	}
	
	public int getDefense() {
		return defense;
	}
	
	public int getDefenseGrow() {
		return defenseGrow;
	}
	
	public int getSpellResistance() {
		return spellResistance;
	}
	
	public int getSpellResistanceGrow() {
		return spellResistanceGrow;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public int getSpeedGrow() {
		return speedGrow;
	}
	
	public int getCritical() {
		return critical;
	}
	
	public int getCriticalGrow() {
		return criticalGrow;
	}
	
	public int getBlock() {
		return block;
	}
	
	public int getBlockGrow() {
		return blockGrow;
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
	
	public int getCriticalDamage() {
		return criticalDamage;
	}
	
	public int getEp() {
		return ep;
	}
	
	public int getAntiCriticalRate() {
		return antiCriticalRate;
	}
	
	public int getCriticalDamageReduction() {
		return criticalDamageReduction;
	}
	
	public int getDamageBonus() {
		return damageBonus;
	}
	
	public int getDamageReduction() {
		return damageReduction;
	}
	
	public int getBlockRate() {
		return blockRate;
	}
	
	public int getBlockCounterattackRate() {
		return blockCounterattackRate;
	}
	
	public int getBlockBreakingRate() {
		return blockBreakingRate;
	}
	
	public int getGuardProbability() {
		return guardProbability;
	}
	
	public int getGuardReduction() {
		return guardReduction;
	}
	
	public int getGuardBreaking() {
		return guardBreaking;
	}
	
	public int getSupportCounterattackRate() {
		return supportCounterattackRate;
	}
	
}
