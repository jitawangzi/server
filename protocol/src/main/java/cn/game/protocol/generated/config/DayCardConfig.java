package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 日租卡
 * 
 * 工具生成的，不要手动修改
 */
 public class DayCardConfig {

	/** 服务器天数 */
	public final int ID;		
	/** 英雄组 */
	public final int[] Hero;		
	/** 等级 */
	public final int Lv;		
	/** 当前品质+星级 1-白 2-绿 3-蓝 4-紫 5-金 6-红 7-彩 8-永恒 9-唯一 */
	public final int[] InitialQuality;		

	public DayCardConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 服务器天数
		String HeroString = element.getAttribute("Hero"); // 英雄组
		if (HeroString != null && HeroString.length() > 0) {
			String[] HeroStrings = HeroString.split(";"); 
			int[] HeroTemp = new int[HeroStrings.length] ; 
			for (int i = 0; i < HeroStrings.length; i++) {
				int temp = Integer.parseInt(HeroStrings[i]);	
				HeroTemp[i] = temp;
			}
			Hero = HeroTemp ;			
		} else {
			Hero = new int[] {};
		}
		Lv = Integer.parseInt(element.getAttribute("Lv") == null || element.getAttribute("Lv").length() == 0 ? "0"
			: element.getAttribute("Lv")); // 等级
		String InitialQualityString = element.getAttribute("InitialQuality"); // 当前品质+星级 1-白 2-绿 3-蓝 4-紫 5-金 6-红 7-彩 8-永恒 9-唯一
		if (InitialQualityString != null && InitialQualityString.length() > 0) {
			String[] InitialQualityStrings = InitialQualityString.split(";"); 
			int[] InitialQualityTemp = new int[InitialQualityStrings.length] ; 
			for (int i = 0; i < InitialQualityStrings.length; i++) {
				int temp = Integer.parseInt(InitialQualityStrings[i]);	
				InitialQualityTemp[i] = temp;
			}
			InitialQuality = InitialQualityTemp ;			
		} else {
			InitialQuality = new int[] {};
		}
	}
	

}
