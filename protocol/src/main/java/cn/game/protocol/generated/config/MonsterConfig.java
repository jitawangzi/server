package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 怪物
 * 
 * 工具生成的，不要手动修改
 */
 public class MonsterConfig {

	/** 怪物ID——被关卡表调用 第1位打头： 1-英雄 2-怪物 3-小头目（在小怪中随机出现） 4-大BOSS（头上N个血条，进入会有boss来袭） */
	private final int ID;		
	/** 怪物名称 */
	private final String name;		
	/** 怪物类型 1-小怪 2-小boss（在小怪中随机出现） 3-大BOSS（头上N个血条，进入会有boss来袭） */
	private final int CareerType;		
	/** 怪物职业及固定站位 1-骑士[前排1] 2-战士[前排2] 3-刺客[前排3] 4-法师[后排1] 5-牧师[后排2] 6-射手[后排3] */
	private final int Career;		
	/** 怪物i说明 */
	private final String MonsterTips;		
	/** 怪物属性id  调用MonsterAttribute#怪物属性第1列id */
	private final String MonsterAttributeID;		
	/** 怪物技能 调用skill表中Skill#技能分页id  其中skill表中StatusGroup列，包含星级增长技能效果 */
	private final int MonsterSkill;		
	/** 怪物技能装填时间  毫秒 */
	private final int SkillLoadingTime;		
	/** 资源id  调用ArtResource表 */
	private final String ArtResourceID;		

	public MonsterConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 怪物ID——被关卡表调用 第1位打头： 1-英雄 2-怪物 3-小头目（在小怪中随机出现） 4-大BOSS（头上N个血条，进入会有boss来袭）
		this.name = element.getAttribute("name"); // 怪物名称
		this.CareerType = Integer.parseInt(element.getAttribute("CareerType") == null || element.getAttribute("CareerType").length() == 0 ? "0"
			: element.getAttribute("CareerType")); // 怪物类型 1-小怪 2-小boss（在小怪中随机出现） 3-大BOSS（头上N个血条，进入会有boss来袭）
		this.Career = Integer.parseInt(element.getAttribute("Career") == null || element.getAttribute("Career").length() == 0 ? "0"
			: element.getAttribute("Career")); // 怪物职业及固定站位 1-骑士[前排1] 2-战士[前排2] 3-刺客[前排3] 4-法师[后排1] 5-牧师[后排2] 6-射手[后排3]
		this.MonsterTips = element.getAttribute("MonsterTips"); // 怪物i说明
		this.MonsterAttributeID = element.getAttribute("MonsterAttributeID"); // 怪物属性id  调用MonsterAttribute#怪物属性第1列id
		this.MonsterSkill = Integer.parseInt(element.getAttribute("MonsterSkill") == null || element.getAttribute("MonsterSkill").length() == 0 ? "0"
			: element.getAttribute("MonsterSkill")); // 怪物技能 调用skill表中Skill#技能分页id  其中skill表中StatusGroup列，包含星级增长技能效果
		this.SkillLoadingTime = Integer.parseInt(element.getAttribute("SkillLoadingTime") == null || element.getAttribute("SkillLoadingTime").length() == 0 ? "0"
			: element.getAttribute("SkillLoadingTime")); // 怪物技能装填时间  毫秒
		this.ArtResourceID = element.getAttribute("ArtResourceID"); // 资源id  调用ArtResource表
	}
	
	public int getID() {
		return ID;
	}
	
	public String getName() {
		return name;
	}
	
	public int getCareerType() {
		return CareerType;
	}
	
	public int getCareer() {
		return Career;
	}
	
	public String getMonsterTips() {
		return MonsterTips;
	}
	
	public String getMonsterAttributeID() {
		return MonsterAttributeID;
	}
	
	public int getMonsterSkill() {
		return MonsterSkill;
	}
	
	public int getSkillLoadingTime() {
		return SkillLoadingTime;
	}
	
	public String getArtResourceID() {
		return ArtResourceID;
	}
	
}
