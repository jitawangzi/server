package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 英雄源
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroSourceConfig {

	/** 英雄源ID */
	public final int ID;		
	/** 英雄名称 */
	public final String name;		
	/** 职业 1-战士 2-刺客 3-法师 4-牧师 5-射手 */
	public final int Career;		
	/** 英雄tips */
	public final String HeroTips;		
	/** 图鉴类型 1-取经天团 2-牛魔王家庭组 3-李天王家庭组 4-天宫打工组 5-西方黄牛组 6-地府追债组 7-海鲜大排档 8-乘风破浪女施主 9-妖魔欢乐组 10-车迟三人组 11-吃瓜群众组 */
	public final int CollectionID;		
	/** 英雄技能ID 调用HeroSkillGroup#技能组ID */
	public final int[] HeroSkillID;		
	/** 激活肉鸽id组 配置：品质;激活的肉鸽id1|...|等级;激活肉鸽idn 调用：Almost-技能&肉鸽——Roguelike#肉鸽表 */
	public final int[][] RoguelikeId;		
	/** 英雄资源id 调用ArtResource表 */
	public final int ArtResourceID;		

	public HeroSourceConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 英雄源ID
		name = element.getAttribute("name"); // 英雄名称
		Career = Integer.parseInt(element.getAttribute("Career") == null || element.getAttribute("Career").length() == 0 ? "0"
			: element.getAttribute("Career")); // 职业 1-战士 2-刺客 3-法师 4-牧师 5-射手
		HeroTips = element.getAttribute("HeroTips"); // 英雄tips
		CollectionID = Integer.parseInt(element.getAttribute("CollectionID") == null || element.getAttribute("CollectionID").length() == 0 ? "0"
			: element.getAttribute("CollectionID")); // 图鉴类型 1-取经天团 2-牛魔王家庭组 3-李天王家庭组 4-天宫打工组 5-西方黄牛组 6-地府追债组 7-海鲜大排档 8-乘风破浪女施主 9-妖魔欢乐组 10-车迟三人组 11-吃瓜群众组
		String HeroSkillIDString = element.getAttribute("HeroSkillID"); // 英雄技能ID 调用HeroSkillGroup#技能组ID
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
		String RoguelikeIdString = element.getAttribute("RoguelikeId"); // 激活肉鸽id组 配置：品质;激活的肉鸽id1|...|等级;激活肉鸽idn 调用：Almost-技能&肉鸽——Roguelike#肉鸽表
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
