package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 技能召唤物表
 * 
 * 工具生成的，不要手动修改
 */
 public class SkillSummonedConfig {

	/** 技能id -- 通用特效100001 角色技能 200101 (百位:角色序列  个位:特效序列) */
	private final int id;		
	/** 特效名称 */
	private final int summonedId;		
	/** 创建类型 -- 1 全部攻击范围 2 技能打点 3 技能目标（寄生） 4 随机一个敌方单位（寄生） */
	private final int createType;		

	public SkillSummonedConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 技能id
		this.summonedId = Integer.parseInt(element.getAttribute("summonedId") == null || element.getAttribute("summonedId").length() == 0 ? "0"
			: element.getAttribute("summonedId")); // 特效名称
		this.createType = Integer.parseInt(element.getAttribute("createType") == null || element.getAttribute("createType").length() == 0 ? "0"
			: element.getAttribute("createType")); // 创建类型
	}
	
	public int getId() {
		return id;
	}
	
	public int getSummonedId() {
		return summonedId;
	}
	
	public int getCreateType() {
		return createType;
	}
	
}
