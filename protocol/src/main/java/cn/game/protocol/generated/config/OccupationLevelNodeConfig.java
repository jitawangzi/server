package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 职介节点表
 * 
 * 工具生成的，不要手动修改
 */
 public class OccupationLevelNodeConfig {

	/** id -- id */
	private final int id;		
	/** 组id -- 组id */
	private final int groupsId;		
	/** 质点顺序 -- 质点顺序 */
	private final List<Integer> originSort;		
	/** 属性 -- 属性 */
	private final List<Entry<Integer,Integer>> attribute;		
	/** 技能 -- 技能 */
	private final int skill;		
	/** 技能效果 -- 技能效果 */
	private final List<Integer> skillEffect;		
	/** 升级消耗 -- 升级消耗 */
	private final List<Entry<Integer,Integer>> upgradeCost;		
	/** 限制等级 -- 限制等级 */
	private final int levelLimit;		

	public OccupationLevelNodeConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.groupsId = Integer.parseInt(element.getAttribute("groupsId") == null || element.getAttribute("groupsId").length() == 0 ? "0"
			: element.getAttribute("groupsId")); // 组id
		String originSortString = element.getAttribute("originSort"); // 质点顺序
		if (originSortString != null && originSortString.length() > 0) {
			String[] originSortStrings = originSortString.split("\\|"); 
			List<Integer> originSort = new ArrayList<Integer>(originSortStrings.length) ; 
			for (int i = 0; i < originSortStrings.length; i++) {
				Integer temp = Integer.parseInt(originSortStrings[i]);
				originSort.add(temp);
			}
			this.originSort = com.google.common.collect.ImmutableList.copyOf(originSort);						
		} else {
			this.originSort = java.util.Collections.emptyList();
		}
		String attributeString = element.getAttribute("attribute"); // 属性
		if (attributeString != null && attributeString.length() > 0) {
			String[] attributeStrings = attributeString.split("\\|"); 
			List<Entry<Integer,Integer>> attribute = new ArrayList<Entry<Integer,Integer>>(attributeStrings.length) ; 
			for (int i = 0; i < attributeStrings.length; i++) {
			    String[] split = attributeStrings[i].split(":", 2);
				attribute.add(new Entry<Integer,Integer>()	{
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

			this.attribute = com.google.common.collect.ImmutableList.copyOf(attribute);						
		} else {
			this.attribute = java.util.Collections.emptyList();
		}
		this.skill = Integer.parseInt(element.getAttribute("skill") == null || element.getAttribute("skill").length() == 0 ? "0"
			: element.getAttribute("skill")); // 技能
		String skillEffectString = element.getAttribute("skillEffect"); // 技能效果
		if (skillEffectString != null && skillEffectString.length() > 0) {
			String[] skillEffectStrings = skillEffectString.split("\\|"); 
			List<Integer> skillEffect = new ArrayList<Integer>(skillEffectStrings.length) ; 
			for (int i = 0; i < skillEffectStrings.length; i++) {
				Integer temp = Integer.parseInt(skillEffectStrings[i]);
				skillEffect.add(temp);
			}
			this.skillEffect = com.google.common.collect.ImmutableList.copyOf(skillEffect);						
		} else {
			this.skillEffect = java.util.Collections.emptyList();
		}
		String upgradeCostString = element.getAttribute("upgradeCost"); // 升级消耗
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
		this.levelLimit = Integer.parseInt(element.getAttribute("levelLimit") == null || element.getAttribute("levelLimit").length() == 0 ? "0"
			: element.getAttribute("levelLimit")); // 限制等级
	}
	
	public int getId() {
		return id;
	}
	
	public int getGroupsId() {
		return groupsId;
	}
	
	public List<Integer> getOriginSort() {
		return originSort;
	}
	
	public List<Entry<Integer,Integer>> getAttribute() {
		return attribute;
	}
	
	public int getSkill() {
		return skill;
	}
	
	public List<Integer> getSkillEffect() {
		return skillEffect;
	}
	
	public List<Entry<Integer,Integer>> getUpgradeCost() {
		return upgradeCost;
	}
	
	public int getLevelLimit() {
		return levelLimit;
	}
	
}
