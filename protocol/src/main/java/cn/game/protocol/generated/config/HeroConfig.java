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
	/** 英雄ID （英雄4位+品质1位） 第1位打头： 1-英雄 2-怪物 3-小头目 4-大BOSS 5-宠物 6-朋友 */
	public final int heroID;		
	/** 英雄名称 */
	public final String name;		
	/** 品质 1-白2-绿3-蓝 4-紫5-紫16-紫2 7-金8-金19-金210-金3 11-红12-红113-红214-红315-红4 16-彩 */
	public final int quality;		
	/** 英雄职业 1-战士 2-刺客 3-法师 4-牧师 5-射手 */
	public final int Career;		
	/** 等级上限 */
	public final int levelMax;		
	/** 初始属性 属性ID；属性值 */
	public final int[][] InitialAttribute;		
	/** 每级成长属性属性 属性ID；属性增量 */
	public final int[][] Growth;		
	/** 英雄技能组  Heroskill中HeroSkillGroup#技能组ID */
	public final int[] HeroSkillID;		
	/** 英雄标签 0-无标签 1-S标签 2-S狂标签 3-S奥标签 4-S血标签 5-S战标签 6-S超标签 */
	public final int tag;		
	/** 品质框 1-白 2-绿 3-蓝 4-紫 5-金 6-红 7-彩 */
	public final int qualityFrame;		
	/** 星级标签 0-无星 1-1星 2-2星 3-3星 4-4星 5-5星 */
	public final int startTag;		
	/** 突破重生 0=无法重生 品质武将id;数量|道具_职业替身id;数量 */
	public final int[] HeroFragment;		

	public HeroConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 品质英雄ID
		heroID = Integer.parseInt(element.getAttribute("heroID") == null || element.getAttribute("heroID").length() == 0 ? "0"
			: element.getAttribute("heroID")); // 英雄ID （英雄4位+品质1位） 第1位打头： 1-英雄 2-怪物 3-小头目 4-大BOSS 5-宠物 6-朋友
		name = element.getAttribute("name"); // 英雄名称
		quality = Integer.parseInt(element.getAttribute("quality") == null || element.getAttribute("quality").length() == 0 ? "0"
			: element.getAttribute("quality")); // 品质 1-白2-绿3-蓝 4-紫5-紫16-紫2 7-金8-金19-金210-金3 11-红12-红113-红214-红315-红4 16-彩
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
		String HeroSkillIDString = element.getAttribute("HeroSkillID"); // 英雄技能组  Heroskill中HeroSkillGroup#技能组ID
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
		tag = Integer.parseInt(element.getAttribute("tag") == null || element.getAttribute("tag").length() == 0 ? "0"
			: element.getAttribute("tag")); // 英雄标签 0-无标签 1-S标签 2-S狂标签 3-S奥标签 4-S血标签 5-S战标签 6-S超标签
		qualityFrame = Integer.parseInt(element.getAttribute("qualityFrame") == null || element.getAttribute("qualityFrame").length() == 0 ? "0"
			: element.getAttribute("qualityFrame")); // 品质框 1-白 2-绿 3-蓝 4-紫 5-金 6-红 7-彩
		startTag = Integer.parseInt(element.getAttribute("startTag") == null || element.getAttribute("startTag").length() == 0 ? "0"
			: element.getAttribute("startTag")); // 星级标签 0-无星 1-1星 2-2星 3-3星 4-4星 5-5星
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
	}
	

}
