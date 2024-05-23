package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 英雄
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroConfig {

	/** 英雄ID  必须3打头6位id 前三位代表品质 300-唯一   310-永恒 320-彩      330-红 340-金      350-紫 360-蓝      370-绿 380-白 后3位流水号 */
	public final int ID;		
	/** 英雄名称 */
	public final String name;		
	/** 职业 1-战士 2-刺客 3-法师 4-牧师 5-射手 */
	public final int Career;		
	/** 初始品质 1-白1星 2-绿1星 3-蓝1星 4-紫1星 5-金1星 6-红1星 7-彩1星 8-永恒1星 9-唯一1星 */
	public final int InitialQuality;		
	/** 最多能突破到的最高品质 1-白1星 2-绿1星 3-蓝1星 4-紫1星 5-金1星 6-红1星 7-彩1星 8-永恒1星 9-唯一1星 */
	public final int BreakQuality;		
	/** 图鉴类型  调用HeroBook#英雄图鉴表id */
	public final int CollectionID;		
	/** 【升级】 初始属性id  调用AttributeVlalue#属性数值表id   属性数值 = 初始值+（lv-1）*成长值 */
	public final int InitialAttributeId;		
	/** 【升级】 成长属性id   调用AttributeVlalue#属性数值表id */
	public final int GrowthAttributeId;		
	/** 【突破】 不同品质 增加的的属性 配置：品质id;属性id|...|品质id;属性id  调用AttributeVlalue#属性数值表id */
	public final int[][] BreakActivationAttribute;		
	/** 【初始+突破】 不同品质 激活英雄技能ID 调用HeroSkillGroup#技能组ID  需要把每个品质都配 调用HeroSkill-HeroSkillGroup#技能组表id 当前——第1组显示 品质+1——第2组显示-图标+觉醒标 品质+2——第3组显示 */
	public final int[][] HeroSkillID;		
	/** 【突破】 不同品质 激活肉鸽id组 配置：当前品质;激活的肉鸽id1|品质+1;激活肉鸽idn 仅客户端界面显示  肉鸽id调用HeroSkill-HeroSkill表id  卡牌界面不用显示 */
	public final int[][] RoguelikeId;		
	/** 英雄资源id 调用ArtResource表 */
	public final int ArtResourceID;		

	public HeroConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 英雄ID  必须3打头6位id 前三位代表品质 300-唯一   310-永恒 320-彩      330-红 340-金      350-紫 360-蓝      370-绿 380-白 后3位流水号
		name = element.getAttribute("name"); // 英雄名称
		Career = Integer.parseInt(element.getAttribute("Career") == null || element.getAttribute("Career").length() == 0 ? "0"
			: element.getAttribute("Career")); // 职业 1-战士 2-刺客 3-法师 4-牧师 5-射手
		InitialQuality = Integer.parseInt(element.getAttribute("InitialQuality") == null || element.getAttribute("InitialQuality").length() == 0 ? "0"
			: element.getAttribute("InitialQuality")); // 初始品质 1-白1星 2-绿1星 3-蓝1星 4-紫1星 5-金1星 6-红1星 7-彩1星 8-永恒1星 9-唯一1星
		BreakQuality = Integer.parseInt(element.getAttribute("BreakQuality") == null || element.getAttribute("BreakQuality").length() == 0 ? "0"
			: element.getAttribute("BreakQuality")); // 最多能突破到的最高品质 1-白1星 2-绿1星 3-蓝1星 4-紫1星 5-金1星 6-红1星 7-彩1星 8-永恒1星 9-唯一1星
		CollectionID = Integer.parseInt(element.getAttribute("CollectionID") == null || element.getAttribute("CollectionID").length() == 0 ? "0"
			: element.getAttribute("CollectionID")); // 图鉴类型  调用HeroBook#英雄图鉴表id
		InitialAttributeId = Integer.parseInt(element.getAttribute("InitialAttributeId") == null || element.getAttribute("InitialAttributeId").length() == 0 ? "0"
			: element.getAttribute("InitialAttributeId")); // 【升级】 初始属性id  调用AttributeVlalue#属性数值表id   属性数值 = 初始值+（lv-1）*成长值
		GrowthAttributeId = Integer.parseInt(element.getAttribute("GrowthAttributeId") == null || element.getAttribute("GrowthAttributeId").length() == 0 ? "0"
			: element.getAttribute("GrowthAttributeId")); // 【升级】 成长属性id   调用AttributeVlalue#属性数值表id
		String BreakActivationAttributeString = element.getAttribute("BreakActivationAttribute"); // 【突破】 不同品质 增加的的属性 配置：品质id;属性id|...|品质id;属性id  调用AttributeVlalue#属性数值表id
		if (BreakActivationAttributeString != null && BreakActivationAttributeString.length() > 0) {
			String[] BreakActivationAttributeStrings = BreakActivationAttributeString.split("\\|"); 
			int[][] BreakActivationAttributeTemp = new int[BreakActivationAttributeStrings.length][] ; 
			for (int i = 0; i < BreakActivationAttributeStrings.length; i++) {
				String[] BreakActivationAttributeStrings2 = BreakActivationAttributeStrings[i].split(";"); 
				int[] array = new int[BreakActivationAttributeStrings2.length];
				for (int j = 0; j < BreakActivationAttributeStrings2.length; j++) {
					int temp = Integer.parseInt(BreakActivationAttributeStrings2[j]);	
					array[j] = temp;
				}
				BreakActivationAttributeTemp[i] = array;
			}
			BreakActivationAttribute = BreakActivationAttributeTemp ;			
		} else {
			BreakActivationAttribute = new int[][] {};
		}
		String HeroSkillIDString = element.getAttribute("HeroSkillID"); // 【初始+突破】 不同品质 激活英雄技能ID 调用HeroSkillGroup#技能组ID  需要把每个品质都配 调用HeroSkill-HeroSkillGroup#技能组表id 当前——第1组显示 品质+1——第2组显示-图标+觉醒标 品质+2——第3组显示
		if (HeroSkillIDString != null && HeroSkillIDString.length() > 0) {
			String[] HeroSkillIDStrings = HeroSkillIDString.split("\\|"); 
			int[][] HeroSkillIDTemp = new int[HeroSkillIDStrings.length][] ; 
			for (int i = 0; i < HeroSkillIDStrings.length; i++) {
				String[] HeroSkillIDStrings2 = HeroSkillIDStrings[i].split(";"); 
				int[] array = new int[HeroSkillIDStrings2.length];
				for (int j = 0; j < HeroSkillIDStrings2.length; j++) {
					int temp = Integer.parseInt(HeroSkillIDStrings2[j]);	
					array[j] = temp;
				}
				HeroSkillIDTemp[i] = array;
			}
			HeroSkillID = HeroSkillIDTemp ;			
		} else {
			HeroSkillID = new int[][] {};
		}
		String RoguelikeIdString = element.getAttribute("RoguelikeId"); // 【突破】 不同品质 激活肉鸽id组 配置：当前品质;激活的肉鸽id1|品质+1;激活肉鸽idn 仅客户端界面显示  肉鸽id调用HeroSkill-HeroSkill表id  卡牌界面不用显示
		if (RoguelikeIdString != null && RoguelikeIdString.length() > 0) {
			String[] RoguelikeIdStrings = RoguelikeIdString.split("\\|"); 
			int[][] RoguelikeIdTemp = new int[RoguelikeIdStrings.length][] ; 
			for (int i = 0; i < RoguelikeIdStrings.length; i++) {
				String[] RoguelikeIdStrings2 = RoguelikeIdStrings[i].split(";"); 
				int[] array = new int[RoguelikeIdStrings2.length];
				for (int j = 0; j < RoguelikeIdStrings2.length; j++) {
					int temp = Integer.parseInt(RoguelikeIdStrings2[j]);	
					array[j] = temp;
				}
				RoguelikeIdTemp[i] = array;
			}
			RoguelikeId = RoguelikeIdTemp ;			
		} else {
			RoguelikeId = new int[][] {};
		}
		ArtResourceID = Integer.parseInt(element.getAttribute("ArtResourceID") == null || element.getAttribute("ArtResourceID").length() == 0 ? "0"
			: element.getAttribute("ArtResourceID")); // 英雄资源id 调用ArtResource表
	}
	

}
