package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 肉鸽表
 * 
 * 工具生成的，不要手动修改
 */
 public class RoguelikeConfig {

	/** 肉鸽id */
	public final int ID;		
	/** 肉鸽类型 1-英雄 2-伙伴 3-全场AOE 4-纯加属性% */
	public final int RogueType;		
	/** 肉鸽底板品质 品质  1-白色  2-绿色  3-蓝色  4-紫色  5-金色  6-红色  7-彩色  8-永恒  9-唯一 */
	public final String quality;		
	/** 肉鸽名称 */
	public final String RogueName;		
	/** 肉鸽描述 */
	public final String RogueTips;		
	/** 召唤英雄id  默认直接顶掉该英雄本身职业所在上场位置   新手特做 */
	public final int CallHero;		
	/** 肉鸽星级显示 配置：品质id;星级数量  1-白色  2-绿色  3-蓝色  4-紫色  5-金色  6-红色  7-彩色  8-永恒  9-唯一 */
	public final int[] RogueStar;		
	/** 技能组id  调用HeroSkillGroup#技能组id 用来判定是同组id */
	public final int HeroSkillGroupId;		
	/** 技能组id标识  此列用来去（Almost_HeroSkill——HeroSkillGroup#技能组——SkillGroup序列）中找第几个数组 */
	public final int SkillGroupMark;		
	/** 多技能组id间出现权重 */
	public final int RogueWeight;		

	public RoguelikeConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 肉鸽id
		RogueType = Integer.parseInt(element.getAttribute("RogueType") == null || element.getAttribute("RogueType").length() == 0 ? "0"
			: element.getAttribute("RogueType")); // 肉鸽类型 1-英雄 2-伙伴 3-全场AOE 4-纯加属性%
		quality = element.getAttribute("quality"); // 肉鸽底板品质 品质  1-白色  2-绿色  3-蓝色  4-紫色  5-金色  6-红色  7-彩色  8-永恒  9-唯一
		RogueName = element.getAttribute("RogueName"); // 肉鸽名称
		RogueTips = element.getAttribute("RogueTips"); // 肉鸽描述
		CallHero = Integer.parseInt(element.getAttribute("CallHero") == null || element.getAttribute("CallHero").length() == 0 ? "0"
			: element.getAttribute("CallHero")); // 召唤英雄id  默认直接顶掉该英雄本身职业所在上场位置   新手特做
		String RogueStarString = element.getAttribute("RogueStar"); // 肉鸽星级显示 配置：品质id;星级数量  1-白色  2-绿色  3-蓝色  4-紫色  5-金色  6-红色  7-彩色  8-永恒  9-唯一
		if (RogueStarString != null && RogueStarString.length() > 0) {
			String[] RogueStarStrings = RogueStarString.split(";"); 
			int[] RogueStarTemp = new int[RogueStarStrings.length] ; 
			for (int i = 0; i < RogueStarStrings.length; i++) {
				int temp = Integer.parseInt(RogueStarStrings[i]);	
				RogueStarTemp[i] = temp;
			}
			RogueStar = RogueStarTemp ;			
		} else {
			RogueStar = new int[] {};
		}
		HeroSkillGroupId = Integer.parseInt(element.getAttribute("HeroSkillGroupId") == null || element.getAttribute("HeroSkillGroupId").length() == 0 ? "0"
			: element.getAttribute("HeroSkillGroupId")); // 技能组id  调用HeroSkillGroup#技能组id 用来判定是同组id
		SkillGroupMark = Integer.parseInt(element.getAttribute("SkillGroupMark") == null || element.getAttribute("SkillGroupMark").length() == 0 ? "0"
			: element.getAttribute("SkillGroupMark")); // 技能组id标识  此列用来去（Almost_HeroSkill——HeroSkillGroup#技能组——SkillGroup序列）中找第几个数组
		RogueWeight = Integer.parseInt(element.getAttribute("RogueWeight") == null || element.getAttribute("RogueWeight").length() == 0 ? "0"
			: element.getAttribute("RogueWeight")); // 多技能组id间出现权重
	}
	

}
