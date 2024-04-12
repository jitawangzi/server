package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 肉鸽表
 * 
 * 工具生成的，不要手动修改
 */
 public class RoguelikeConfig {

	/** 肉鸽id  被Almost配置表_养成_Dragon——DragonSkill#龙技能——RoguelikeId列调用 */
	public final int ID;		
	/** 肉鸽类型 1-主角 2-龙 3-全场AOE */
	public final int RogueType;		
	/** 技能组id  调用HeroSkillGroup#技能组id 用来判定是同组id */
	public final int HeroSkillGroupId;		
	/** 多技能组id间出现权重 */
	public final int RogueWeight;		
	/** 同组肉鸽序列 */
	public final int Queue;		
	/** 技能id  调用HeroSkill#技能中id */
	public final int RogueID;		
	/** 肉鸽名称 */
	public final String RogueName;		
	/** 肉鸽底板品质 品质  1-白色  2-绿色  3-蓝色  4-紫色  5-金色  6-红色  7-彩色  8-永恒  9-唯一 */
	public final String quality;		
	/** 技能组id标识  此列用来去（Almost_HeroSkill——HeroSkillGroup#技能组——SkillGroup序列）中找第几个数组 */
	public final int SkillGroupMark;		

	public RoguelikeConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 肉鸽id  被Almost配置表_养成_Dragon——DragonSkill#龙技能——RoguelikeId列调用
		RogueType = Integer.parseInt(element.getAttribute("RogueType") == null || element.getAttribute("RogueType").length() == 0 ? "0"
			: element.getAttribute("RogueType")); // 肉鸽类型 1-主角 2-龙 3-全场AOE
		HeroSkillGroupId = Integer.parseInt(element.getAttribute("HeroSkillGroupId") == null || element.getAttribute("HeroSkillGroupId").length() == 0 ? "0"
			: element.getAttribute("HeroSkillGroupId")); // 技能组id  调用HeroSkillGroup#技能组id 用来判定是同组id
		RogueWeight = Integer.parseInt(element.getAttribute("RogueWeight") == null || element.getAttribute("RogueWeight").length() == 0 ? "0"
			: element.getAttribute("RogueWeight")); // 多技能组id间出现权重
		Queue = Integer.parseInt(element.getAttribute("Queue") == null || element.getAttribute("Queue").length() == 0 ? "0"
			: element.getAttribute("Queue")); // 同组肉鸽序列
		RogueID = Integer.parseInt(element.getAttribute("RogueID") == null || element.getAttribute("RogueID").length() == 0 ? "0"
			: element.getAttribute("RogueID")); // 技能id  调用HeroSkill#技能中id
		RogueName = element.getAttribute("RogueName"); // 肉鸽名称
		quality = element.getAttribute("quality"); // 肉鸽底板品质 品质  1-白色  2-绿色  3-蓝色  4-紫色  5-金色  6-红色  7-彩色  8-永恒  9-唯一
		SkillGroupMark = Integer.parseInt(element.getAttribute("SkillGroupMark") == null || element.getAttribute("SkillGroupMark").length() == 0 ? "0"
			: element.getAttribute("SkillGroupMark")); // 技能组id标识  此列用来去（Almost_HeroSkill——HeroSkillGroup#技能组——SkillGroup序列）中找第几个数组
	}
	

}
