package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 职业天赋(分析室)
 * 
 * 工具生成的，不要手动修改
 */
 public class OccupationTalentNodeConfig {

	/** id -- id */
	private final int id;		
	/** 职业类型 -- 1-守护 2-先锋 3-异能 4-突袭 5-祈愿 */
	private final int occupation;		
	/** 节点类型 -- 1-天赋节点 2-强化节点 */
	private final int type;		
	/** 前置条件 -- 当前节点的解锁条件节点 */
	private final int preNode;		
	/** 所属组（流派/条） -- 1-该职业的第一条 2-该职业的第二条 3-该职业的第三天条 */
	private final int group;		
	/** Skill表 -- 天赋技能id|强化项索引 -1表示开启天赋 */
	private final int[] unlockTalentSkill;		
	/** 节点消耗 -- 当前节点解锁消耗的资源 */
	private final List<Entry<Integer,Integer>> cost;		

	public OccupationTalentNodeConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.occupation = Integer.parseInt(element.getAttribute("occupation") == null || element.getAttribute("occupation").length() == 0 ? "0"
			: element.getAttribute("occupation")); // 职业类型
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 节点类型
		this.preNode = Integer.parseInt(element.getAttribute("preNode") == null || element.getAttribute("preNode").length() == 0 ? "0"
			: element.getAttribute("preNode")); // 前置条件
		this.group = Integer.parseInt(element.getAttribute("group") == null || element.getAttribute("group").length() == 0 ? "0"
			: element.getAttribute("group")); // 所属组（流派/条）
		String unlockTalentSkillString = element.getAttribute("unlockTalentSkill"); // Skill表
		if (unlockTalentSkillString != null && unlockTalentSkillString.length() > 0) {
			String[] unlockTalentSkillStrings = unlockTalentSkillString.split("\\|"); 
			int[] unlockTalentSkill = new int[unlockTalentSkillStrings.length] ; 
			for (int i = 0; i < unlockTalentSkillStrings.length; i++) {
				int temp = Integer.parseInt(unlockTalentSkillStrings[i]);
				unlockTalentSkill[i] = temp;
			}
			this.unlockTalentSkill = unlockTalentSkill ;			
		} else {
			this.unlockTalentSkill = new int[] {};
		}
		String costString = element.getAttribute("cost"); // 节点消耗
		if (costString != null && costString.length() > 0) {
			String[] costStrings = costString.split("\\|"); 
			List<Entry<Integer,Integer>> cost = new ArrayList<Entry<Integer,Integer>>(costStrings.length) ; 
			for (int i = 0; i < costStrings.length; i++) {
			    String[] split = costStrings[i].split(":", 2);
				cost.add(new Entry<Integer,Integer>()	{
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

			this.cost = com.google.common.collect.ImmutableList.copyOf(cost);						
		} else {
			this.cost = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getOccupation() {
		return occupation;
	}
	
	public int getType() {
		return type;
	}
	
	public int getPreNode() {
		return preNode;
	}
	
	public int getGroup() {
		return group;
	}
	
	public int[] getUnlockTalentSkill() {
		return unlockTalentSkill;
	}
	
	public List<Entry<Integer,Integer>> getCost() {
		return cost;
	}
	
}
