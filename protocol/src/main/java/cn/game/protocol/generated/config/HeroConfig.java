package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 英雄
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroConfig {

	/** 英雄ID */
	public final int ID;		
	/** 品质英雄ID */
	public final int QualityID;		
	/** 英雄名称 */
	public final String name;		
	/** 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一 */
	public final int quality;		
	/** 英雄职业 1-战士 2-刺客 3-法师 4-牧师 5-射手 */
	public final int Career;		
	/** 等级上限 */
	public final int levelMax;		
	/** 初始属性 属性ID；属性值 */
	public final int[][] InitialAttribute;		
	/** 每级成长属性属性 属性ID；属性增量 */
	public final int[][] Growth;		
	/** 英雄标签 0-无标签 1-S标签 2-S狂标签 3-S奥标签 4-S血标签 5-S战标签 6-S超标签 */
	public final int HeroTag;		
	/** 品质框 1-白 2-绿 3-蓝 4-紫 5-金 6-红 7-彩 */
	public final int IconQuality;		
	/** 星级标签 0-无星 1-1星 2-2星 3-3星 4-4星 5-5星 */
	public final int IconStar;		
	/** 英雄i说明 */
	public final String HeroTips;		
	/** 技能说明 */
	public final String[] SkillTips;		
	/** 品质说明 */
	public final String[] QualityTips;		
	/** 突破重生 0=无法重生 品质武将id;数量|道具_职业替身id;数量 */
	public final int[] HeroFragment;		
	/** 英雄技能ID  调用Almost_HeroSkill—HeroSkillGroup#技能组——ID */
	public final int[] HeroSkillID;		
	/** 英雄资源id  调用ArtResource表 */
	public final String ArtResourceID;		
	/** 初始武器id  调用HeroSword#英雄武器 */
	public final int InitialSword;		
	/** 初始时装id */
	public final int InitialClothes;		
	/** 初始龙id */
	public final int InitiaDragon;		

	public HeroConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 英雄ID
		QualityID = Integer.parseInt(element.getAttribute("QualityID") == null || element.getAttribute("QualityID").length() == 0 ? "0"
			: element.getAttribute("QualityID")); // 品质英雄ID
		name = element.getAttribute("name"); // 英雄名称
		quality = Integer.parseInt(element.getAttribute("quality") == null || element.getAttribute("quality").length() == 0 ? "0"
			: element.getAttribute("quality")); // 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一
		Career = Integer.parseInt(element.getAttribute("Career") == null || element.getAttribute("Career").length() == 0 ? "0"
			: element.getAttribute("Career")); // 英雄职业 1-战士 2-刺客 3-法师 4-牧师 5-射手
		levelMax = Integer.parseInt(element.getAttribute("levelMax") == null || element.getAttribute("levelMax").length() == 0 ? "0"
			: element.getAttribute("levelMax")); // 等级上限
		String InitialAttributeString = element.getAttribute("InitialAttribute"); // 初始属性 属性ID；属性值
		if (InitialAttributeString != null && InitialAttributeString.length() > 0) {
			String[] InitialAttributeStrings = InitialAttributeString.split("\\|"); 
			int[][] InitialAttributeTemp = new int[InitialAttributeStrings.length][] ; 
			for (int i = 0; i < InitialAttributeStrings.length; i++) {
				String[] InitialAttributeStrings2 = InitialAttributeStrings[i].split(";"); 
				int[] array = new int[InitialAttributeStrings2.length];
				for (int j = 0; j < InitialAttributeStrings2.length; j++) {
					int temp = Integer.parseInt(InitialAttributeStrings2[j]);	
					array[j] = temp;
				}
				InitialAttributeTemp[i] = array;
			}
			InitialAttribute = InitialAttributeTemp ;			
		} else {
			InitialAttribute = new int[][] {};
		}
		String GrowthString = element.getAttribute("Growth"); // 每级成长属性属性 属性ID；属性增量
		if (GrowthString != null && GrowthString.length() > 0) {
			String[] GrowthStrings = GrowthString.split("\\|"); 
			int[][] GrowthTemp = new int[GrowthStrings.length][] ; 
			for (int i = 0; i < GrowthStrings.length; i++) {
				String[] GrowthStrings2 = GrowthStrings[i].split(";"); 
				int[] array = new int[GrowthStrings2.length];
				for (int j = 0; j < GrowthStrings2.length; j++) {
					int temp = Integer.parseInt(GrowthStrings2[j]);	
					array[j] = temp;
				}
				GrowthTemp[i] = array;
			}
			Growth = GrowthTemp ;			
		} else {
			Growth = new int[][] {};
		}
		HeroTag = Integer.parseInt(element.getAttribute("HeroTag") == null || element.getAttribute("HeroTag").length() == 0 ? "0"
			: element.getAttribute("HeroTag")); // 英雄标签 0-无标签 1-S标签 2-S狂标签 3-S奥标签 4-S血标签 5-S战标签 6-S超标签
		IconQuality = Integer.parseInt(element.getAttribute("IconQuality") == null || element.getAttribute("IconQuality").length() == 0 ? "0"
			: element.getAttribute("IconQuality")); // 品质框 1-白 2-绿 3-蓝 4-紫 5-金 6-红 7-彩
		IconStar = Integer.parseInt(element.getAttribute("IconStar") == null || element.getAttribute("IconStar").length() == 0 ? "0"
			: element.getAttribute("IconStar")); // 星级标签 0-无星 1-1星 2-2星 3-3星 4-4星 5-5星
		HeroTips = element.getAttribute("HeroTips"); // 英雄i说明
		String SkillTipsString = element.getAttribute("SkillTips"); // 技能说明
		if (SkillTipsString != null && SkillTipsString.length() > 0) {
			String[] SkillTipsStrings = SkillTipsString.split(";"); 
			String[] SkillTipsTemp = new String[SkillTipsStrings.length] ; 
			for (int i = 0; i < SkillTipsStrings.length; i++) {
				String temp = SkillTipsStrings[i];	
				SkillTipsTemp[i] = temp;
			}
			SkillTips = SkillTipsTemp ;			
		} else {
			SkillTips = new String[] {};
		}
		String QualityTipsString = element.getAttribute("QualityTips"); // 品质说明
		if (QualityTipsString != null && QualityTipsString.length() > 0) {
			String[] QualityTipsStrings = QualityTipsString.split(";"); 
			String[] QualityTipsTemp = new String[QualityTipsStrings.length] ; 
			for (int i = 0; i < QualityTipsStrings.length; i++) {
				String temp = QualityTipsStrings[i];	
				QualityTipsTemp[i] = temp;
			}
			QualityTips = QualityTipsTemp ;			
		} else {
			QualityTips = new String[] {};
		}
		String HeroFragmentString = element.getAttribute("HeroFragment"); // 突破重生 0=无法重生 品质武将id;数量|道具_职业替身id;数量
		if (HeroFragmentString != null && HeroFragmentString.length() > 0) {
			String[] HeroFragmentStrings = HeroFragmentString.split(";"); 
			int[] HeroFragmentTemp = new int[HeroFragmentStrings.length] ; 
			for (int i = 0; i < HeroFragmentStrings.length; i++) {
				int temp = Integer.parseInt(HeroFragmentStrings[i]);	
				HeroFragmentTemp[i] = temp;
			}
			HeroFragment = HeroFragmentTemp ;			
		} else {
			HeroFragment = new int[] {};
		}
		String HeroSkillIDString = element.getAttribute("HeroSkillID"); // 英雄技能ID  调用Almost_HeroSkill—HeroSkillGroup#技能组——ID
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
		ArtResourceID = element.getAttribute("ArtResourceID"); // 英雄资源id  调用ArtResource表
		InitialSword = Integer.parseInt(element.getAttribute("InitialSword") == null || element.getAttribute("InitialSword").length() == 0 ? "0"
			: element.getAttribute("InitialSword")); // 初始武器id  调用HeroSword#英雄武器
		InitialClothes = Integer.parseInt(element.getAttribute("InitialClothes") == null || element.getAttribute("InitialClothes").length() == 0 ? "0"
			: element.getAttribute("InitialClothes")); // 初始时装id
		InitiaDragon = Integer.parseInt(element.getAttribute("InitiaDragon") == null || element.getAttribute("InitiaDragon").length() == 0 ? "0"
			: element.getAttribute("InitiaDragon")); // 初始龙id
	}
	

}
