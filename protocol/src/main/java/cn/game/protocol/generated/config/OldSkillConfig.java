package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 技能表
 * 
 * 工具生成的，不要手动修改
 */
 public class OldSkillConfig {

	/** 技能id -- 角色技能ID,roleId前4位+技能序列01 普通怪,51+01(种类)+01(技能) boss怪,52+01(种类)+01(技能) 剧情怪,53+01(种类)+01(技能) 恶灵怪,54+01(种类)+01(技能) 天赋,70+1(职业)+1(条目)+01(节点) */
	private final int id;		
	/** 子技能列表 -- 子技能列表 */
	private final List<Integer> subIds;		
	/** 技能类型 -- 1-技能1 2-技能2 3-技能3 4-被动 5-天赋 */
	private final int type;		
	/** 阶段 -- 属于第几个阶段的技能 */
	private final int stage;		
	/** 强化消耗 */
	private final List<Entry<Integer,Integer>> upgradeCost;		
	/** 职业 -- 1-守护 2-先锋 3-异能 4-突袭 5-祈愿 6-黯灭 */
	private final int occupation;		
	/** 攻击范围类型 -- 1 十 2 田 3 -| 4 ]- 5 |a| 201 攻击者身前横向2格 202 攻击者身前纵向3格 203 攻击者身前九宫格 204 攻击者身前两列 205 标记的位置 206 所有敌方单位 207 攻击者身前随机两行 208 非两端随机一列 209 攻击者身前一列 210 身前范围内随机 */
	private final int attackRangeType;		
	/** 攻击范围参数 -- 1 最小距离|最大距离|最大距离X?|最大距离Y? 2 最小距离|最大距离 210 随机数量 */
	private final int[] attackRangeParams;		
	/** 生效范围类型 -- 同攻击范围 不填则使用攻击范围 */
	private final int effectRangeType;		
	/** 生效范围参数 -- 同攻击范围参数 */
	private final int[] effectRangeParams;		
	/** 目标类型 -- 0 无目标 1 全部单位 2 己方单位 3 敌方单位 */
	private final int targetType;		
	/** 目标条件 -- SkillTargetCondition表ID （&&)关系 */
	private final int[] targetTypeParams;		
	/** 打点类型 -- 0 无目标 1 全部地块 2 有单位地块 3 空闲地块 */
	private final int attackPosType;		
	/** 打点条件参数 -- 1 有自己的召唤物|召唤物ID */
	private final int[] attackPosParams;		
	/** 技能CD -- 角色技能CD */
	private final int cd;		
	/** 技能消耗 -- 消耗AP的数值 */
	private final int cost;		
	/** 专属点数消耗 */
	private final int costEx;		
	/** 使用条件 -- SkillUseCondition表ID （&&)关系 */
	private final int[] useConParas;		
	/** 角色位移 -- 技能释放后角色的位移,角色面向方向位移 */
	private final int movePos;		
	/** 类型 -- 1-伤害 2-治疗 3-Buff(无伤害) */
	private final int attackType;		
	/** 伤害段数 -- 不填表示蓄力 */
	private final int part;		
	/** 伤害段数目标类型 -- 0全部目标 1单个目标 */
	private final int partType;		
	/** hp伤害数值 -- 技能段伤害百分比 ,支持数组每段伤害不同 */
	private final int[] values;		
	/** san伤害数值 -- 技能段伤害百分比 ,支持数组每段伤害不同 */
	private final int[] valuesSan;		
	/** 命中率 -- 命中率 */
	private final int hitRate;		
	/** 技能主Buff -- 读取Buff表 */
	private final List<Integer> buff;		
	/** 技能附加Buff -- 读取Buff表 */
	private final List<Integer> strengthenBuff;		
	/** 召唤物 -- 读取SkillSummoned表 */
	private final List<Integer> summoneds;		

	public OldSkillConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 技能id
		String subIdsString = element.getAttribute("subIds"); // 子技能列表
		if (subIdsString != null && subIdsString.length() > 0) {
			String[] subIdsStrings = subIdsString.split("\\|"); 
			List<Integer> subIds = new ArrayList<Integer>(subIdsStrings.length) ; 
			for (int i = 0; i < subIdsStrings.length; i++) {
				Integer temp = Integer.parseInt(subIdsStrings[i]);
				subIds.add(temp);
			}
			this.subIds = com.google.common.collect.ImmutableList.copyOf(subIds);						
		} else {
			this.subIds = java.util.Collections.emptyList();
		}
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 技能类型
		this.stage = Integer.parseInt(element.getAttribute("stage") == null || element.getAttribute("stage").length() == 0 ? "0"
			: element.getAttribute("stage")); // 阶段
		String upgradeCostString = element.getAttribute("upgradeCost"); // 强化消耗
		if (upgradeCostString != null && upgradeCostString.length() > 0) {
			String[] upgradeCostStrings = upgradeCostString.split("\\|"); 
			List<Entry<Integer,Integer>> upgradeCost = new ArrayList<Entry<Integer,Integer>>(upgradeCostStrings.length) ; 
			for (int i = 0; i < upgradeCostStrings.length; i++) {
			    String[] split = upgradeCostStrings[i].split(":", 2);
				upgradeCost.add(new Entry<Integer,Integer>()	{
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

			this.upgradeCost = com.google.common.collect.ImmutableList.copyOf(upgradeCost);						
		} else {
			this.upgradeCost = java.util.Collections.emptyList();
		}
		this.occupation = Integer.parseInt(element.getAttribute("occupation") == null || element.getAttribute("occupation").length() == 0 ? "0"
			: element.getAttribute("occupation")); // 职业
		this.attackRangeType = Integer.parseInt(element.getAttribute("attackRangeType") == null || element.getAttribute("attackRangeType").length() == 0 ? "0"
			: element.getAttribute("attackRangeType")); // 攻击范围类型
		String attackRangeParamsString = element.getAttribute("attackRangeParams"); // 攻击范围参数
		if (attackRangeParamsString != null && attackRangeParamsString.length() > 0) {
			String[] attackRangeParamsStrings = attackRangeParamsString.split("\\|"); 
			int[] attackRangeParams = new int[attackRangeParamsStrings.length] ; 
			for (int i = 0; i < attackRangeParamsStrings.length; i++) {
				int temp = Integer.parseInt(attackRangeParamsStrings[i]);
				attackRangeParams[i] = temp;
			}
			this.attackRangeParams = attackRangeParams ;			
		} else {
			this.attackRangeParams = new int[] {};
		}
		this.effectRangeType = Integer.parseInt(element.getAttribute("effectRangeType") == null || element.getAttribute("effectRangeType").length() == 0 ? "0"
			: element.getAttribute("effectRangeType")); // 生效范围类型
		String effectRangeParamsString = element.getAttribute("effectRangeParams"); // 生效范围参数
		if (effectRangeParamsString != null && effectRangeParamsString.length() > 0) {
			String[] effectRangeParamsStrings = effectRangeParamsString.split("\\|"); 
			int[] effectRangeParams = new int[effectRangeParamsStrings.length] ; 
			for (int i = 0; i < effectRangeParamsStrings.length; i++) {
				int temp = Integer.parseInt(effectRangeParamsStrings[i]);
				effectRangeParams[i] = temp;
			}
			this.effectRangeParams = effectRangeParams ;			
		} else {
			this.effectRangeParams = new int[] {};
		}
		this.targetType = Integer.parseInt(element.getAttribute("targetType") == null || element.getAttribute("targetType").length() == 0 ? "0"
			: element.getAttribute("targetType")); // 目标类型
		String targetTypeParamsString = element.getAttribute("targetTypeParams"); // 目标条件
		if (targetTypeParamsString != null && targetTypeParamsString.length() > 0) {
			String[] targetTypeParamsStrings = targetTypeParamsString.split("\\|"); 
			int[] targetTypeParams = new int[targetTypeParamsStrings.length] ; 
			for (int i = 0; i < targetTypeParamsStrings.length; i++) {
				int temp = Integer.parseInt(targetTypeParamsStrings[i]);
				targetTypeParams[i] = temp;
			}
			this.targetTypeParams = targetTypeParams ;			
		} else {
			this.targetTypeParams = new int[] {};
		}
		this.attackPosType = Integer.parseInt(element.getAttribute("attackPosType") == null || element.getAttribute("attackPosType").length() == 0 ? "0"
			: element.getAttribute("attackPosType")); // 打点类型
		String attackPosParamsString = element.getAttribute("attackPosParams"); // 打点条件参数
		if (attackPosParamsString != null && attackPosParamsString.length() > 0) {
			String[] attackPosParamsStrings = attackPosParamsString.split("\\|"); 
			int[] attackPosParams = new int[attackPosParamsStrings.length] ; 
			for (int i = 0; i < attackPosParamsStrings.length; i++) {
				int temp = Integer.parseInt(attackPosParamsStrings[i]);
				attackPosParams[i] = temp;
			}
			this.attackPosParams = attackPosParams ;			
		} else {
			this.attackPosParams = new int[] {};
		}
		this.cd = Integer.parseInt(element.getAttribute("cd") == null || element.getAttribute("cd").length() == 0 ? "0"
			: element.getAttribute("cd")); // 技能CD
		this.cost = Integer.parseInt(element.getAttribute("cost") == null || element.getAttribute("cost").length() == 0 ? "0"
			: element.getAttribute("cost")); // 技能消耗
		this.costEx = Integer.parseInt(element.getAttribute("costEx") == null || element.getAttribute("costEx").length() == 0 ? "0"
			: element.getAttribute("costEx")); // 专属点数消耗
		String useConParasString = element.getAttribute("useConParas"); // 使用条件
		if (useConParasString != null && useConParasString.length() > 0) {
			String[] useConParasStrings = useConParasString.split("\\|"); 
			int[] useConParas = new int[useConParasStrings.length] ; 
			for (int i = 0; i < useConParasStrings.length; i++) {
				int temp = Integer.parseInt(useConParasStrings[i]);
				useConParas[i] = temp;
			}
			this.useConParas = useConParas ;			
		} else {
			this.useConParas = new int[] {};
		}
		this.movePos = Integer.parseInt(element.getAttribute("movePos") == null || element.getAttribute("movePos").length() == 0 ? "0"
			: element.getAttribute("movePos")); // 角色位移
		this.attackType = Integer.parseInt(element.getAttribute("attackType") == null || element.getAttribute("attackType").length() == 0 ? "0"
			: element.getAttribute("attackType")); // 类型
		this.part = Integer.parseInt(element.getAttribute("part") == null || element.getAttribute("part").length() == 0 ? "0"
			: element.getAttribute("part")); // 伤害段数
		this.partType = Integer.parseInt(element.getAttribute("partType") == null || element.getAttribute("partType").length() == 0 ? "0"
			: element.getAttribute("partType")); // 伤害段数目标类型
		String valuesString = element.getAttribute("values"); // hp伤害数值
		if (valuesString != null && valuesString.length() > 0) {
			String[] valuesStrings = valuesString.split("\\|"); 
			int[] values = new int[valuesStrings.length] ; 
			for (int i = 0; i < valuesStrings.length; i++) {
				int temp = Integer.parseInt(valuesStrings[i]);
				values[i] = temp;
			}
			this.values = values ;			
		} else {
			this.values = new int[] {};
		}
		String valuesSanString = element.getAttribute("valuesSan"); // san伤害数值
		if (valuesSanString != null && valuesSanString.length() > 0) {
			String[] valuesSanStrings = valuesSanString.split("\\|"); 
			int[] valuesSan = new int[valuesSanStrings.length] ; 
			for (int i = 0; i < valuesSanStrings.length; i++) {
				int temp = Integer.parseInt(valuesSanStrings[i]);
				valuesSan[i] = temp;
			}
			this.valuesSan = valuesSan ;			
		} else {
			this.valuesSan = new int[] {};
		}
		this.hitRate = Integer.parseInt(element.getAttribute("hitRate") == null || element.getAttribute("hitRate").length() == 0 ? "0"
			: element.getAttribute("hitRate")); // 命中率
		String buffString = element.getAttribute("buff"); // 技能主Buff
		if (buffString != null && buffString.length() > 0) {
			String[] buffStrings = buffString.split("\\|"); 
			List<Integer> buff = new ArrayList<Integer>(buffStrings.length) ; 
			for (int i = 0; i < buffStrings.length; i++) {
				Integer temp = Integer.parseInt(buffStrings[i]);
				buff.add(temp);
			}
			this.buff = com.google.common.collect.ImmutableList.copyOf(buff);						
		} else {
			this.buff = java.util.Collections.emptyList();
		}
		String strengthenBuffString = element.getAttribute("strengthenBuff"); // 技能附加Buff
		if (strengthenBuffString != null && strengthenBuffString.length() > 0) {
			String[] strengthenBuffStrings = strengthenBuffString.split("\\|"); 
			List<Integer> strengthenBuff = new ArrayList<Integer>(strengthenBuffStrings.length) ; 
			for (int i = 0; i < strengthenBuffStrings.length; i++) {
				Integer temp = Integer.parseInt(strengthenBuffStrings[i]);
				strengthenBuff.add(temp);
			}
			this.strengthenBuff = com.google.common.collect.ImmutableList.copyOf(strengthenBuff);						
		} else {
			this.strengthenBuff = java.util.Collections.emptyList();
		}
		String summonedsString = element.getAttribute("summoneds"); // 召唤物
		if (summonedsString != null && summonedsString.length() > 0) {
			String[] summonedsStrings = summonedsString.split("\\|"); 
			List<Integer> summoneds = new ArrayList<Integer>(summonedsStrings.length) ; 
			for (int i = 0; i < summonedsStrings.length; i++) {
				Integer temp = Integer.parseInt(summonedsStrings[i]);
				summoneds.add(temp);
			}
			this.summoneds = com.google.common.collect.ImmutableList.copyOf(summoneds);						
		} else {
			this.summoneds = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public List<Integer> getSubIds() {
		return subIds;
	}
	
	public int getType() {
		return type;
	}
	
	public int getStage() {
		return stage;
	}
	
	public List<Entry<Integer,Integer>> getUpgradeCost() {
		return upgradeCost;
	}
	
	public int getOccupation() {
		return occupation;
	}
	
	public int getAttackRangeType() {
		return attackRangeType;
	}
	
	public int[] getAttackRangeParams() {
		return attackRangeParams;
	}
	
	public int getEffectRangeType() {
		return effectRangeType;
	}
	
	public int[] getEffectRangeParams() {
		return effectRangeParams;
	}
	
	public int getTargetType() {
		return targetType;
	}
	
	public int[] getTargetTypeParams() {
		return targetTypeParams;
	}
	
	public int getAttackPosType() {
		return attackPosType;
	}
	
	public int[] getAttackPosParams() {
		return attackPosParams;
	}
	
	public int getCd() {
		return cd;
	}
	
	public int getCost() {
		return cost;
	}
	
	public int getCostEx() {
		return costEx;
	}
	
	public int[] getUseConParas() {
		return useConParas;
	}
	
	public int getMovePos() {
		return movePos;
	}
	
	public int getAttackType() {
		return attackType;
	}
	
	public int getPart() {
		return part;
	}
	
	public int getPartType() {
		return partType;
	}
	
	public int[] getValues() {
		return values;
	}
	
	public int[] getValuesSan() {
		return valuesSan;
	}
	
	public int getHitRate() {
		return hitRate;
	}
	
	public List<Integer> getBuff() {
		return buff;
	}
	
	public List<Integer> getStrengthenBuff() {
		return strengthenBuff;
	}
	
	public List<Integer> getSummoneds() {
		return summoneds;
	}
	
}
