package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 角色
 * 
 * 工具生成的，不要手动修改
 */
 public class RoleConfig {

	/** id -- id */
	private final int id;		
	/** 名称 */
	private final String name;		
	/** 角色类型 -- 1-预设角色 2-佣兵 */
	private final int type;		
	/** 职业 -- 1-守护 2-先锋 3-异能 4-突袭 5-祈愿 */
	private final int occupation;		
	/** 阵营 -- 1-马戏团 2-深水重工 3-昨日联盟 4-群星乐园 5-风铃群落 6-伊娜教会 7-七海神社 */
	private final int camp;		
	/** SAN阶段临界值% -- SAN值 临界点1/临界点2/最大值 */
	private final List<Integer> sanValue;		
	/** SAN值标签 -- 每名角色专属的San值标签,读取标签库 */
	private final int sanValueTag;		
	/** 礼物卡 -- 每个角色需6张礼物卡 礼物卡:角色id:好感度 没有解锁条件的卡默认填写0 */
	private final List<List<Integer>> giftCards;		
	/** 装备槽位类型 -- 1-固有装备 2-饰品 */
	private final List<Integer> equipmentSlot;		
	/** 固定装备 -- 专属该角色的装备 读取equipment表 */
	private final List<Integer> equipmentId;		
	/** 技能1 -- 技能1 */
	private final List<Integer> normalSkill;		
	/** 技能2 -- 技能2 */
	private final List<Integer> epSkill;		
	/** 技能3 -- 技能3 */
	private final List<Integer> apSkill;		
	/** 天赋技能 -- 天赋技能 */
	private final int talentSkill;		
	/** 被动技能列表 -- 被动技能列表 */
	private final List<Integer> passiveSkillList;		
	/** 生命 */
	private final int hp;		
	/** 生命成长值 */
	private final int hpGrow;		
	/** san */
	private final int san;		
	/** san值成长值 */
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
	/** 闪避 -- 改为概率值 */
	private final int dodge;		
	/** 暴击 -- 改为概率值 */
	private final int critical;		
	/** 暴击伤害 -- 固定值 */
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
	/** 伤害类型 -- 1-物理 2-法术 */
	private final int damageType;		
	/** 抗性 */
	private final int spellResistance;		
	/** 抗性成长值 */
	private final int[] spellResistanceGrow;		
	/** 命中成长值 */
	private final int[] hitGrow;		
	/** 暴击成长值 */
	private final int[] criticalGrow;		
	/** 闪避成长值 */
	private final int[] dodgeGrow;		
	/** 法术攻击 */
	private final int spellPower;		
	/** 法术攻击成长值 */
	private final int[] spellPowerGrow;		
	/** 格挡 */
	private final int block;		
	/** 格挡成长值 */
	private final int[] blockGrow;		
	/** EP -- 固定值 */
	private final int ep;		
	/** 等级上限 -- 每个角色等级的上限值 */
	private final int levelMax;		
	/** 等级系数 -- 等级exp经验值*(等级系数/属性点数) */
	private final float levelCoef;		
	/** 属性点数 -- 每个角色升级时获得的属性点数 */
	private final int attributePoint;		
	/** 属性权重 -- 属性类型:权重|属性类型:权重 属性类型读取属性表 */
	private final List<Entry<Integer,Integer>> attributeWeight;		
	/** 初始核心套装 -- 初始核心套装 */
	private final List<Integer> initialCore;		
	/** 稀有度 -- 1-蓝 2-紫 3-橙 */
	private final int quality;		
	/** 亲密度喜好 -- 1-蛋糕 2-鲜花 3- 4- 5- 亲密度喜好列表 */
	private final List<Entry<Integer,Float>> friendlyLike;		
	/** 倾向标记 -- 1-前排 2-中排 3-后排 4-群体 5-爆发 6-续航 7-辅助 8-护卫 9-反击 10-生存 11-控制 12-治疗 13-指挥 */
	private final List<Integer> tag;		
	/** 初始星级 -- 0-0星 1-1星 2-2星 3-3星 4-4星 5-5星 */
	private final int star;		
	/** 星级碎片id -- 对应物品表的星级碎片 */
	private final int starFragment;		
	/** 被动技能 -- 进入战场生效，有不同的触发条件 */
	private final List<Integer> passiveSkill;		
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

	public RoleConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 名称
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 角色类型
		this.occupation = Integer.parseInt(element.getAttribute("occupation") == null || element.getAttribute("occupation").length() == 0 ? "0"
			: element.getAttribute("occupation")); // 职业
		this.camp = Integer.parseInt(element.getAttribute("camp") == null || element.getAttribute("camp").length() == 0 ? "0"
			: element.getAttribute("camp")); // 阵营
		String sanValueString = element.getAttribute("sanValue"); // SAN阶段临界值%
		if (sanValueString != null && sanValueString.length() > 0) {
			String[] sanValueStrings = sanValueString.split("\\|"); 
			List<Integer> sanValue = new ArrayList<Integer>(sanValueStrings.length) ; 
			for (int i = 0; i < sanValueStrings.length; i++) {
				Integer temp = Integer.parseInt(sanValueStrings[i]);
				sanValue.add(temp);
			}
			this.sanValue = com.google.common.collect.ImmutableList.copyOf(sanValue);						
		} else {
			this.sanValue = java.util.Collections.emptyList();
		}
		this.sanValueTag = Integer.parseInt(element.getAttribute("sanValueTag") == null || element.getAttribute("sanValueTag").length() == 0 ? "0"
			: element.getAttribute("sanValueTag")); // SAN值标签
		String giftCardsString = element.getAttribute("giftCards"); // 礼物卡
		if (giftCardsString != null && giftCardsString.length() > 0) {
			String[] giftCardsStrings = giftCardsString.split("\\|"); 
			List<List<Integer>> giftCards = new ArrayList<List<Integer>>(giftCardsStrings.length) ; 
			for (int i = 0; i < giftCardsStrings.length; i++) {
				String[] giftCardsStrings2 = giftCardsStrings[i].split(":"); 
				List<Integer> list = new ArrayList<Integer>(giftCardsStrings2.length) ; 
				for (int j = 0; j < giftCardsStrings2.length; j++) {
					Integer temp = Integer.parseInt(giftCardsStrings2[j]);
					list.add(temp) ; 
				}
				giftCards.add(list);
			}
			this.giftCards = com.google.common.collect.ImmutableList.copyOf(giftCards);						
		} else {
			this.giftCards = java.util.Collections.emptyList();
		}
		String equipmentSlotString = element.getAttribute("equipmentSlot"); // 装备槽位类型
		if (equipmentSlotString != null && equipmentSlotString.length() > 0) {
			String[] equipmentSlotStrings = equipmentSlotString.split("\\|"); 
			List<Integer> equipmentSlot = new ArrayList<Integer>(equipmentSlotStrings.length) ; 
			for (int i = 0; i < equipmentSlotStrings.length; i++) {
				Integer temp = Integer.parseInt(equipmentSlotStrings[i]);
				equipmentSlot.add(temp);
			}
			this.equipmentSlot = com.google.common.collect.ImmutableList.copyOf(equipmentSlot);						
		} else {
			this.equipmentSlot = java.util.Collections.emptyList();
		}
		String equipmentIdString = element.getAttribute("equipmentId"); // 固定装备
		if (equipmentIdString != null && equipmentIdString.length() > 0) {
			String[] equipmentIdStrings = equipmentIdString.split("\\|"); 
			List<Integer> equipmentId = new ArrayList<Integer>(equipmentIdStrings.length) ; 
			for (int i = 0; i < equipmentIdStrings.length; i++) {
				Integer temp = Integer.parseInt(equipmentIdStrings[i]);
				equipmentId.add(temp);
			}
			this.equipmentId = com.google.common.collect.ImmutableList.copyOf(equipmentId);						
		} else {
			this.equipmentId = java.util.Collections.emptyList();
		}
		String normalSkillString = element.getAttribute("normalSkill"); // 技能1
		if (normalSkillString != null && normalSkillString.length() > 0) {
			String[] normalSkillStrings = normalSkillString.split("\\|"); 
			List<Integer> normalSkill = new ArrayList<Integer>(normalSkillStrings.length) ; 
			for (int i = 0; i < normalSkillStrings.length; i++) {
				Integer temp = Integer.parseInt(normalSkillStrings[i]);
				normalSkill.add(temp);
			}
			this.normalSkill = com.google.common.collect.ImmutableList.copyOf(normalSkill);						
		} else {
			this.normalSkill = java.util.Collections.emptyList();
		}
		String epSkillString = element.getAttribute("epSkill"); // 技能2
		if (epSkillString != null && epSkillString.length() > 0) {
			String[] epSkillStrings = epSkillString.split("\\|"); 
			List<Integer> epSkill = new ArrayList<Integer>(epSkillStrings.length) ; 
			for (int i = 0; i < epSkillStrings.length; i++) {
				Integer temp = Integer.parseInt(epSkillStrings[i]);
				epSkill.add(temp);
			}
			this.epSkill = com.google.common.collect.ImmutableList.copyOf(epSkill);						
		} else {
			this.epSkill = java.util.Collections.emptyList();
		}
		String apSkillString = element.getAttribute("apSkill"); // 技能3
		if (apSkillString != null && apSkillString.length() > 0) {
			String[] apSkillStrings = apSkillString.split("\\|"); 
			List<Integer> apSkill = new ArrayList<Integer>(apSkillStrings.length) ; 
			for (int i = 0; i < apSkillStrings.length; i++) {
				Integer temp = Integer.parseInt(apSkillStrings[i]);
				apSkill.add(temp);
			}
			this.apSkill = com.google.common.collect.ImmutableList.copyOf(apSkill);						
		} else {
			this.apSkill = java.util.Collections.emptyList();
		}
		this.talentSkill = Integer.parseInt(element.getAttribute("talentSkill") == null || element.getAttribute("talentSkill").length() == 0 ? "0"
			: element.getAttribute("talentSkill")); // 天赋技能
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
		this.san = Integer.parseInt(element.getAttribute("san") == null || element.getAttribute("san").length() == 0 ? "0"
			: element.getAttribute("san")); // san
		this.sanGrow = Integer.parseInt(element.getAttribute("sanGrow") == null || element.getAttribute("sanGrow").length() == 0 ? "0"
			: element.getAttribute("sanGrow")); // san值成长值
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
		this.dodge = Integer.parseInt(element.getAttribute("dodge") == null || element.getAttribute("dodge").length() == 0 ? "0"
			: element.getAttribute("dodge")); // 闪避
		this.critical = Integer.parseInt(element.getAttribute("critical") == null || element.getAttribute("critical").length() == 0 ? "0"
			: element.getAttribute("critical")); // 暴击
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
		this.damageType = Integer.parseInt(element.getAttribute("damageType") == null || element.getAttribute("damageType").length() == 0 ? "0"
			: element.getAttribute("damageType")); // 伤害类型
		this.spellResistance = Integer.parseInt(element.getAttribute("spellResistance") == null || element.getAttribute("spellResistance").length() == 0 ? "0"
			: element.getAttribute("spellResistance")); // 抗性
		String spellResistanceGrowString = element.getAttribute("spellResistanceGrow"); // 抗性成长值
		if (spellResistanceGrowString != null && spellResistanceGrowString.length() > 0) {
			String[] spellResistanceGrowStrings = spellResistanceGrowString.split("\\|"); 
			int[] spellResistanceGrow = new int[spellResistanceGrowStrings.length] ; 
			for (int i = 0; i < spellResistanceGrowStrings.length; i++) {
				int temp = Integer.parseInt(spellResistanceGrowStrings[i]);
				spellResistanceGrow[i] = temp;
			}
			this.spellResistanceGrow = spellResistanceGrow ;			
		} else {
			this.spellResistanceGrow = new int[] {};
		}
		String hitGrowString = element.getAttribute("hitGrow"); // 命中成长值
		if (hitGrowString != null && hitGrowString.length() > 0) {
			String[] hitGrowStrings = hitGrowString.split("\\|"); 
			int[] hitGrow = new int[hitGrowStrings.length] ; 
			for (int i = 0; i < hitGrowStrings.length; i++) {
				int temp = Integer.parseInt(hitGrowStrings[i]);
				hitGrow[i] = temp;
			}
			this.hitGrow = hitGrow ;			
		} else {
			this.hitGrow = new int[] {};
		}
		String criticalGrowString = element.getAttribute("criticalGrow"); // 暴击成长值
		if (criticalGrowString != null && criticalGrowString.length() > 0) {
			String[] criticalGrowStrings = criticalGrowString.split("\\|"); 
			int[] criticalGrow = new int[criticalGrowStrings.length] ; 
			for (int i = 0; i < criticalGrowStrings.length; i++) {
				int temp = Integer.parseInt(criticalGrowStrings[i]);
				criticalGrow[i] = temp;
			}
			this.criticalGrow = criticalGrow ;			
		} else {
			this.criticalGrow = new int[] {};
		}
		String dodgeGrowString = element.getAttribute("dodgeGrow"); // 闪避成长值
		if (dodgeGrowString != null && dodgeGrowString.length() > 0) {
			String[] dodgeGrowStrings = dodgeGrowString.split("\\|"); 
			int[] dodgeGrow = new int[dodgeGrowStrings.length] ; 
			for (int i = 0; i < dodgeGrowStrings.length; i++) {
				int temp = Integer.parseInt(dodgeGrowStrings[i]);
				dodgeGrow[i] = temp;
			}
			this.dodgeGrow = dodgeGrow ;			
		} else {
			this.dodgeGrow = new int[] {};
		}
		this.spellPower = Integer.parseInt(element.getAttribute("spellPower") == null || element.getAttribute("spellPower").length() == 0 ? "0"
			: element.getAttribute("spellPower")); // 法术攻击
		String spellPowerGrowString = element.getAttribute("spellPowerGrow"); // 法术攻击成长值
		if (spellPowerGrowString != null && spellPowerGrowString.length() > 0) {
			String[] spellPowerGrowStrings = spellPowerGrowString.split("\\|"); 
			int[] spellPowerGrow = new int[spellPowerGrowStrings.length] ; 
			for (int i = 0; i < spellPowerGrowStrings.length; i++) {
				int temp = Integer.parseInt(spellPowerGrowStrings[i]);
				spellPowerGrow[i] = temp;
			}
			this.spellPowerGrow = spellPowerGrow ;			
		} else {
			this.spellPowerGrow = new int[] {};
		}
		this.block = Integer.parseInt(element.getAttribute("block") == null || element.getAttribute("block").length() == 0 ? "0"
			: element.getAttribute("block")); // 格挡
		String blockGrowString = element.getAttribute("blockGrow"); // 格挡成长值
		if (blockGrowString != null && blockGrowString.length() > 0) {
			String[] blockGrowStrings = blockGrowString.split("\\|"); 
			int[] blockGrow = new int[blockGrowStrings.length] ; 
			for (int i = 0; i < blockGrowStrings.length; i++) {
				int temp = Integer.parseInt(blockGrowStrings[i]);
				blockGrow[i] = temp;
			}
			this.blockGrow = blockGrow ;			
		} else {
			this.blockGrow = new int[] {};
		}
		this.ep = Integer.parseInt(element.getAttribute("ep") == null || element.getAttribute("ep").length() == 0 ? "0"
			: element.getAttribute("ep")); // EP
		this.levelMax = Integer.parseInt(element.getAttribute("levelMax") == null || element.getAttribute("levelMax").length() == 0 ? "0"
			: element.getAttribute("levelMax")); // 等级上限
		this.levelCoef = Float.parseFloat(element.getAttribute("levelCoef") == null || element.getAttribute("levelCoef").length() == 0 ? "0"
			: element.getAttribute("levelCoef")); // 等级系数
		this.attributePoint = Integer.parseInt(element.getAttribute("attributePoint") == null || element.getAttribute("attributePoint").length() == 0 ? "0"
			: element.getAttribute("attributePoint")); // 属性点数
		String attributeWeightString = element.getAttribute("attributeWeight"); // 属性权重
		if (attributeWeightString != null && attributeWeightString.length() > 0) {
			String[] attributeWeightStrings = attributeWeightString.split("\\|"); 
			List<Entry<Integer,Integer>> attributeWeight = new ArrayList<Entry<Integer,Integer>>(attributeWeightStrings.length) ; 
			for (int i = 0; i < attributeWeightStrings.length; i++) {
			    String[] split = attributeWeightStrings[i].split(":", 2);
				attributeWeight.add(new Entry<Integer,Integer>()	{
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

			this.attributeWeight = com.google.common.collect.ImmutableList.copyOf(attributeWeight);						
		} else {
			this.attributeWeight = java.util.Collections.emptyList();
		}
		String initialCoreString = element.getAttribute("initialCore"); // 初始核心套装
		if (initialCoreString != null && initialCoreString.length() > 0) {
			String[] initialCoreStrings = initialCoreString.split("\\|"); 
			List<Integer> initialCore = new ArrayList<Integer>(initialCoreStrings.length) ; 
			for (int i = 0; i < initialCoreStrings.length; i++) {
				Integer temp = Integer.parseInt(initialCoreStrings[i]);
				initialCore.add(temp);
			}
			this.initialCore = com.google.common.collect.ImmutableList.copyOf(initialCore);						
		} else {
			this.initialCore = java.util.Collections.emptyList();
		}
		this.quality = Integer.parseInt(element.getAttribute("quality") == null || element.getAttribute("quality").length() == 0 ? "0"
			: element.getAttribute("quality")); // 稀有度
		String friendlyLikeString = element.getAttribute("friendlyLike"); // 亲密度喜好
		if (friendlyLikeString != null && friendlyLikeString.length() > 0) {
			String[] friendlyLikeStrings = friendlyLikeString.split("\\|"); 
			List<Entry<Integer,Float>> friendlyLike = new ArrayList<Entry<Integer,Float>>(friendlyLikeStrings.length) ; 
			for (int i = 0; i < friendlyLikeStrings.length; i++) {
			    String[] split = friendlyLikeStrings[i].split(":", 2);
				friendlyLike.add(new Entry<Integer,Float>()	{
					@Override
					public Float setValue(Float value) {
						return null;
					}
					@Override
					public Float getValue() {
						try {
							return Float.parseFloat(split[1]);
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

			this.friendlyLike = com.google.common.collect.ImmutableList.copyOf(friendlyLike);						
		} else {
			this.friendlyLike = java.util.Collections.emptyList();
		}
		String tagString = element.getAttribute("tag"); // 倾向标记
		if (tagString != null && tagString.length() > 0) {
			String[] tagStrings = tagString.split("\\|"); 
			List<Integer> tag = new ArrayList<Integer>(tagStrings.length) ; 
			for (int i = 0; i < tagStrings.length; i++) {
				Integer temp = Integer.parseInt(tagStrings[i]);
				tag.add(temp);
			}
			this.tag = com.google.common.collect.ImmutableList.copyOf(tag);						
		} else {
			this.tag = java.util.Collections.emptyList();
		}
		this.star = Integer.parseInt(element.getAttribute("star") == null || element.getAttribute("star").length() == 0 ? "0"
			: element.getAttribute("star")); // 初始星级
		this.starFragment = Integer.parseInt(element.getAttribute("starFragment") == null || element.getAttribute("starFragment").length() == 0 ? "0"
			: element.getAttribute("starFragment")); // 星级碎片id
		String passiveSkillString = element.getAttribute("passiveSkill"); // 被动技能
		if (passiveSkillString != null && passiveSkillString.length() > 0) {
			String[] passiveSkillStrings = passiveSkillString.split("\\|"); 
			List<Integer> passiveSkill = new ArrayList<Integer>(passiveSkillStrings.length) ; 
			for (int i = 0; i < passiveSkillStrings.length; i++) {
				Integer temp = Integer.parseInt(passiveSkillStrings[i]);
				passiveSkill.add(temp);
			}
			this.passiveSkill = com.google.common.collect.ImmutableList.copyOf(passiveSkill);						
		} else {
			this.passiveSkill = java.util.Collections.emptyList();
		}
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
	
	public int getType() {
		return type;
	}
	
	public int getOccupation() {
		return occupation;
	}
	
	public int getCamp() {
		return camp;
	}
	
	public List<Integer> getSanValue() {
		return sanValue;
	}
	
	public int getSanValueTag() {
		return sanValueTag;
	}
	
	public List<List<Integer>> getGiftCards() {
		return giftCards;
	}
	
	public List<Integer> getEquipmentSlot() {
		return equipmentSlot;
	}
	
	public List<Integer> getEquipmentId() {
		return equipmentId;
	}
	
	public List<Integer> getNormalSkill() {
		return normalSkill;
	}
	
	public List<Integer> getEpSkill() {
		return epSkill;
	}
	
	public List<Integer> getApSkill() {
		return apSkill;
	}
	
	public int getTalentSkill() {
		return talentSkill;
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
	
	public int getDodge() {
		return dodge;
	}
	
	public int getCritical() {
		return critical;
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
	
	public int getDamageType() {
		return damageType;
	}
	
	public int getSpellResistance() {
		return spellResistance;
	}
	
	public int[] getSpellResistanceGrow() {
		return spellResistanceGrow;
	}
	
	public int[] getHitGrow() {
		return hitGrow;
	}
	
	public int[] getCriticalGrow() {
		return criticalGrow;
	}
	
	public int[] getDodgeGrow() {
		return dodgeGrow;
	}
	
	public int getSpellPower() {
		return spellPower;
	}
	
	public int[] getSpellPowerGrow() {
		return spellPowerGrow;
	}
	
	public int getBlock() {
		return block;
	}
	
	public int[] getBlockGrow() {
		return blockGrow;
	}
	
	public int getEp() {
		return ep;
	}
	
	public int getLevelMax() {
		return levelMax;
	}
	
	public float getLevelCoef() {
		return levelCoef;
	}
	
	public int getAttributePoint() {
		return attributePoint;
	}
	
	public List<Entry<Integer,Integer>> getAttributeWeight() {
		return attributeWeight;
	}
	
	public List<Integer> getInitialCore() {
		return initialCore;
	}
	
	public int getQuality() {
		return quality;
	}
	
	public List<Entry<Integer,Float>> getFriendlyLike() {
		return friendlyLike;
	}
	
	public List<Integer> getTag() {
		return tag;
	}
	
	public int getStar() {
		return star;
	}
	
	public int getStarFragment() {
		return starFragment;
	}
	
	public List<Integer> getPassiveSkill() {
		return passiveSkill;
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
