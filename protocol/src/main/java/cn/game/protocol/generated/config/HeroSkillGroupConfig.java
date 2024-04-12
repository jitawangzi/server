package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 技能组
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroSkillGroupConfig {

	/** 技能组ID */
	public final int ID;		
	/** 技能名称 */
	public final String SkillName;		
	/** 所有技能id配置 配置：肉鸽1id;技能id1;技能id2|肉鸽2id;技能id1;技能id2 调用HeroSkill#技能id */
	public final int[][] SkillGroup;		
	/** 技能类型 1-主角 2-龙 3-场中AOE  新增列 */
	public final int SkillType;		
	/** 权重 技能 直接根据权重确定使用哪个技能 */
	public final int[] SkillWeight;		
	/** 技能使用次数用完就进入CD */
	public final int SkillTimes;		
	/** 技能强制cd时间毫秒 */
	public final int SkillCD;		
	/** 武将登场施放的技能： 配置：技能id1;技能id2 调用：SKill#技能 */
	public final int[] HeroAppearSkillGroup;		
	/** 权重 技能 直接根据权重确定使用那个技能 */
	public final int[] AppearSkillWeight;		
	/** 武将死亡施放的技能： 配置：技能id1;技能id2 调用：SKill#技能 */
	public final int[] HeroDieSkillGroup;		
	/** 权重 技能 直接根据权重确定使用那个技能 */
	public final int[] DieSkillWeight;		
	/** 武将复活施放的技能： 配置：技能id1;技能id2 调用：SKill#技能 */
	public final int[] HeroRevivedSkillGroup;		
	/** 权重 技能 直接根据权重确定使用那个技能 */
	public final int[] RevivedSkillWeight;		

	public HeroSkillGroupConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 技能组ID
		SkillName = element.getAttribute("SkillName"); // 技能名称
		String SkillGroupString = element.getAttribute("SkillGroup"); // 所有技能id配置 配置：肉鸽1id;技能id1;技能id2|肉鸽2id;技能id1;技能id2 调用HeroSkill#技能id
		if (SkillGroupString != null && SkillGroupString.length() > 0) {
			String[] SkillGroupStrings = SkillGroupString.split("\\|"); 
			int[][] SkillGroupTemp = new int[SkillGroupStrings.length][] ; 
			for (int i = 0; i < SkillGroupStrings.length; i++) {
				String[] SkillGroupStrings2 = SkillGroupStrings[i].split(";"); 
				int[] array = new int[SkillGroupStrings2.length];
				for (int j = 0; j < SkillGroupStrings2.length; j++) {
					int temp = Integer.parseInt(SkillGroupStrings2[j]);	
					array[j] = temp;
				}
				SkillGroupTemp[i] = array;
			}
			SkillGroup = SkillGroupTemp ;			
		} else {
			SkillGroup = new int[][] {};
		}
		SkillType = Integer.parseInt(element.getAttribute("SkillType") == null || element.getAttribute("SkillType").length() == 0 ? "0"
			: element.getAttribute("SkillType")); // 技能类型 1-主角 2-龙 3-场中AOE  新增列
		String SkillWeightString = element.getAttribute("SkillWeight"); // 权重 技能 直接根据权重确定使用哪个技能
		if (SkillWeightString != null && SkillWeightString.length() > 0) {
			String[] SkillWeightStrings = SkillWeightString.split(";"); 
			int[] SkillWeightTemp = new int[SkillWeightStrings.length] ; 
			for (int i = 0; i < SkillWeightStrings.length; i++) {
				int temp = Integer.parseInt(SkillWeightStrings[i]);	
				SkillWeightTemp[i] = temp;
			}
			SkillWeight = SkillWeightTemp ;			
		} else {
			SkillWeight = new int[] {};
		}
		SkillTimes = Integer.parseInt(element.getAttribute("SkillTimes") == null || element.getAttribute("SkillTimes").length() == 0 ? "0"
			: element.getAttribute("SkillTimes")); // 技能使用次数用完就进入CD
		SkillCD = Integer.parseInt(element.getAttribute("SkillCD") == null || element.getAttribute("SkillCD").length() == 0 ? "0"
			: element.getAttribute("SkillCD")); // 技能强制cd时间毫秒
		String HeroAppearSkillGroupString = element.getAttribute("HeroAppearSkillGroup"); // 武将登场施放的技能： 配置：技能id1;技能id2 调用：SKill#技能
		if (HeroAppearSkillGroupString != null && HeroAppearSkillGroupString.length() > 0) {
			String[] HeroAppearSkillGroupStrings = HeroAppearSkillGroupString.split(";"); 
			int[] HeroAppearSkillGroupTemp = new int[HeroAppearSkillGroupStrings.length] ; 
			for (int i = 0; i < HeroAppearSkillGroupStrings.length; i++) {
				int temp = Integer.parseInt(HeroAppearSkillGroupStrings[i]);	
				HeroAppearSkillGroupTemp[i] = temp;
			}
			HeroAppearSkillGroup = HeroAppearSkillGroupTemp ;			
		} else {
			HeroAppearSkillGroup = new int[] {};
		}
		String AppearSkillWeightString = element.getAttribute("AppearSkillWeight"); // 权重 技能 直接根据权重确定使用那个技能
		if (AppearSkillWeightString != null && AppearSkillWeightString.length() > 0) {
			String[] AppearSkillWeightStrings = AppearSkillWeightString.split(";"); 
			int[] AppearSkillWeightTemp = new int[AppearSkillWeightStrings.length] ; 
			for (int i = 0; i < AppearSkillWeightStrings.length; i++) {
				int temp = Integer.parseInt(AppearSkillWeightStrings[i]);	
				AppearSkillWeightTemp[i] = temp;
			}
			AppearSkillWeight = AppearSkillWeightTemp ;			
		} else {
			AppearSkillWeight = new int[] {};
		}
		String HeroDieSkillGroupString = element.getAttribute("HeroDieSkillGroup"); // 武将死亡施放的技能： 配置：技能id1;技能id2 调用：SKill#技能
		if (HeroDieSkillGroupString != null && HeroDieSkillGroupString.length() > 0) {
			String[] HeroDieSkillGroupStrings = HeroDieSkillGroupString.split(";"); 
			int[] HeroDieSkillGroupTemp = new int[HeroDieSkillGroupStrings.length] ; 
			for (int i = 0; i < HeroDieSkillGroupStrings.length; i++) {
				int temp = Integer.parseInt(HeroDieSkillGroupStrings[i]);	
				HeroDieSkillGroupTemp[i] = temp;
			}
			HeroDieSkillGroup = HeroDieSkillGroupTemp ;			
		} else {
			HeroDieSkillGroup = new int[] {};
		}
		String DieSkillWeightString = element.getAttribute("DieSkillWeight"); // 权重 技能 直接根据权重确定使用那个技能
		if (DieSkillWeightString != null && DieSkillWeightString.length() > 0) {
			String[] DieSkillWeightStrings = DieSkillWeightString.split(";"); 
			int[] DieSkillWeightTemp = new int[DieSkillWeightStrings.length] ; 
			for (int i = 0; i < DieSkillWeightStrings.length; i++) {
				int temp = Integer.parseInt(DieSkillWeightStrings[i]);	
				DieSkillWeightTemp[i] = temp;
			}
			DieSkillWeight = DieSkillWeightTemp ;			
		} else {
			DieSkillWeight = new int[] {};
		}
		String HeroRevivedSkillGroupString = element.getAttribute("HeroRevivedSkillGroup"); // 武将复活施放的技能： 配置：技能id1;技能id2 调用：SKill#技能
		if (HeroRevivedSkillGroupString != null && HeroRevivedSkillGroupString.length() > 0) {
			String[] HeroRevivedSkillGroupStrings = HeroRevivedSkillGroupString.split(";"); 
			int[] HeroRevivedSkillGroupTemp = new int[HeroRevivedSkillGroupStrings.length] ; 
			for (int i = 0; i < HeroRevivedSkillGroupStrings.length; i++) {
				int temp = Integer.parseInt(HeroRevivedSkillGroupStrings[i]);	
				HeroRevivedSkillGroupTemp[i] = temp;
			}
			HeroRevivedSkillGroup = HeroRevivedSkillGroupTemp ;			
		} else {
			HeroRevivedSkillGroup = new int[] {};
		}
		String RevivedSkillWeightString = element.getAttribute("RevivedSkillWeight"); // 权重 技能 直接根据权重确定使用那个技能
		if (RevivedSkillWeightString != null && RevivedSkillWeightString.length() > 0) {
			String[] RevivedSkillWeightStrings = RevivedSkillWeightString.split(";"); 
			int[] RevivedSkillWeightTemp = new int[RevivedSkillWeightStrings.length] ; 
			for (int i = 0; i < RevivedSkillWeightStrings.length; i++) {
				int temp = Integer.parseInt(RevivedSkillWeightStrings[i]);	
				RevivedSkillWeightTemp[i] = temp;
			}
			RevivedSkillWeight = RevivedSkillWeightTemp ;			
		} else {
			RevivedSkillWeight = new int[] {};
		}
	}
	

}
