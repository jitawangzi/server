package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 英雄
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroConfig {

	/** 品质英雄ID */
	public final int ID;		
	/** 英雄ID */
	public final int HeroID;		
	/** 英雄名称 */
	public final String name;		
	/** 品质 1-白色 2-绿色 3-蓝色（1星） 4-紫色（2星） 5-金色（3星） 6-红色（4星） 7-彩色（5星） 8-永恒（5星） 9-唯一（5星） */
	public final int quality;		
	/** 星级 0-无星 1-1星 2-2星 3-3星 4-4星 5-5星 */
	public final int Star;		
	/** 英雄职业 1-战士 2-刺客 3-法师 4-牧师 5-射手 */
	public final int Career;		
	/** 等级上限 */
	public final int levelMax;		
	/** 英雄初始属性id 调用AttributeVlalue属性数值表 */
	public final int InitialAttribute1;		
	/** 英雄成长属性id 调用AttributeVlalue属性数值表 */
	public final int InitialAttribute2;		
	/** 英雄i说明 */
	public final String HeroTips;		
	/** 重生 品质英雄id;数量|道具id;数量 */
	public final int[][] HeroFragment;		
	/** 英雄技能ID  调用HeroSkill—HeroSkillGroup#技能组——ID */
	public final int[] HeroSkillID;		
	/** 英雄资源id 调用ArtResource表 */
	public final String ArtResourceID;		
	/** 突破目标ID */
	public final int PromoteTarget;		
	/** 突破消耗 */
	public final int[][] PromoteConsume;		
	/** 万能耗材ID */
	public final int StandinItemId;		

	public HeroConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 品质英雄ID
		HeroID = Integer.parseInt(element.getAttribute("HeroID") == null || element.getAttribute("HeroID").length() == 0 ? "0"
			: element.getAttribute("HeroID")); // 英雄ID
		name = element.getAttribute("name"); // 英雄名称
		quality = Integer.parseInt(element.getAttribute("quality") == null || element.getAttribute("quality").length() == 0 ? "0"
			: element.getAttribute("quality")); // 品质 1-白色 2-绿色 3-蓝色（1星） 4-紫色（2星） 5-金色（3星） 6-红色（4星） 7-彩色（5星） 8-永恒（5星） 9-唯一（5星）
		Star = Integer.parseInt(element.getAttribute("Star") == null || element.getAttribute("Star").length() == 0 ? "0"
			: element.getAttribute("Star")); // 星级 0-无星 1-1星 2-2星 3-3星 4-4星 5-5星
		Career = Integer.parseInt(element.getAttribute("Career") == null || element.getAttribute("Career").length() == 0 ? "0"
			: element.getAttribute("Career")); // 英雄职业 1-战士 2-刺客 3-法师 4-牧师 5-射手
		levelMax = Integer.parseInt(element.getAttribute("levelMax") == null || element.getAttribute("levelMax").length() == 0 ? "0"
			: element.getAttribute("levelMax")); // 等级上限
		InitialAttribute1 = Integer.parseInt(element.getAttribute("InitialAttribute1") == null || element.getAttribute("InitialAttribute1").length() == 0 ? "0"
			: element.getAttribute("InitialAttribute1")); // 英雄初始属性id 调用AttributeVlalue属性数值表
		InitialAttribute2 = Integer.parseInt(element.getAttribute("InitialAttribute2") == null || element.getAttribute("InitialAttribute2").length() == 0 ? "0"
			: element.getAttribute("InitialAttribute2")); // 英雄成长属性id 调用AttributeVlalue属性数值表
		HeroTips = element.getAttribute("HeroTips"); // 英雄i说明
		String HeroFragmentString = element.getAttribute("HeroFragment"); // 重生 品质英雄id;数量|道具id;数量
		if (HeroFragmentString != null && HeroFragmentString.length() > 0) {
			String[] HeroFragmentStrings = HeroFragmentString.split("\\|"); 
			int[][] HeroFragmentTemp = new int[HeroFragmentStrings.length][] ; 
			for (int i = 0; i < HeroFragmentStrings.length; i++) {
				String[] HeroFragmentStrings2 = HeroFragmentStrings[i].split(";"); 
				int[] array = new int[HeroFragmentStrings2.length];
				for (int j = 0; j < HeroFragmentStrings2.length; j++) {
					int temp = Integer.parseInt(HeroFragmentStrings2[j]);	
					array[j] = temp;
				}
				HeroFragmentTemp[i] = array;
			}
			HeroFragment = HeroFragmentTemp ;			
		} else {
			HeroFragment = new int[][] {};
		}
		String HeroSkillIDString = element.getAttribute("HeroSkillID"); // 英雄技能ID  调用HeroSkill—HeroSkillGroup#技能组——ID
		if (HeroSkillIDString != null && HeroSkillIDString.length() > 0) {
			String[] HeroSkillIDStrings = HeroSkillIDString.split(";"); 
			int[] HeroSkillIDTemp = new int[HeroSkillIDStrings.length] ; 
			for (int i = 0; i < HeroSkillIDStrings.length; i++) {
				int temp = Integer.parseInt(HeroSkillIDStrings[i]);	
				HeroSkillIDTemp[i] = temp;
			}
			HeroSkillID = HeroSkillIDTemp ;			
		} else {
			HeroSkillID = new int[] {};
		}
		ArtResourceID = element.getAttribute("ArtResourceID"); // 英雄资源id 调用ArtResource表
		PromoteTarget = Integer.parseInt(element.getAttribute("PromoteTarget") == null || element.getAttribute("PromoteTarget").length() == 0 ? "0"
			: element.getAttribute("PromoteTarget")); // 突破目标ID
		String PromoteConsumeString = element.getAttribute("PromoteConsume"); // 突破消耗
		if (PromoteConsumeString != null && PromoteConsumeString.length() > 0) {
			String[] PromoteConsumeStrings = PromoteConsumeString.split("\\|"); 
			int[][] PromoteConsumeTemp = new int[PromoteConsumeStrings.length][] ; 
			for (int i = 0; i < PromoteConsumeStrings.length; i++) {
				String[] PromoteConsumeStrings2 = PromoteConsumeStrings[i].split(";"); 
				int[] array = new int[PromoteConsumeStrings2.length];
				for (int j = 0; j < PromoteConsumeStrings2.length; j++) {
					int temp = Integer.parseInt(PromoteConsumeStrings2[j]);	
					array[j] = temp;
				}
				PromoteConsumeTemp[i] = array;
			}
			PromoteConsume = PromoteConsumeTemp ;			
		} else {
			PromoteConsume = new int[][] {};
		}
		StandinItemId = Integer.parseInt(element.getAttribute("StandinItemId") == null || element.getAttribute("StandinItemId").length() == 0 ? "0"
			: element.getAttribute("StandinItemId")); // 万能耗材ID
	}
	

}
