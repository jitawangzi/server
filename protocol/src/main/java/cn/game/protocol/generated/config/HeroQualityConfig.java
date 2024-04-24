package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 英雄品质
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroQualityConfig {

	/** 品质ID */
	public final int ID;		
	/** 品质 */
	public final int quality;		
	/** 品质名称 */
	public final String Qname;		
	/** 英雄初始属性id 调用AttributeVlalue属性数值表 */
	public final int InitialAttribute;		
	/** 英雄成长属性id 调用AttributeVlalue属性数值表 */
	public final int GrowthAttribute;		
	/** 等级上限 */
	public final int LevelMax;		
	/** 星级上限 */
	public final int StarMax;		
	/** 星级成长属性id 调用AttributeVlalue属性数值表 */
	public final int StarGrowthAttribute;		

	public HeroQualityConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 品质ID
		quality = Integer.parseInt(element.getAttribute("quality") == null || element.getAttribute("quality").length() == 0 ? "0"
			: element.getAttribute("quality")); // 品质
		Qname = element.getAttribute("Qname"); // 品质名称
		InitialAttribute = Integer.parseInt(element.getAttribute("InitialAttribute") == null || element.getAttribute("InitialAttribute").length() == 0 ? "0"
			: element.getAttribute("InitialAttribute")); // 英雄初始属性id 调用AttributeVlalue属性数值表
		GrowthAttribute = Integer.parseInt(element.getAttribute("GrowthAttribute") == null || element.getAttribute("GrowthAttribute").length() == 0 ? "0"
			: element.getAttribute("GrowthAttribute")); // 英雄成长属性id 调用AttributeVlalue属性数值表
		LevelMax = Integer.parseInt(element.getAttribute("LevelMax") == null || element.getAttribute("LevelMax").length() == 0 ? "0"
			: element.getAttribute("LevelMax")); // 等级上限
		StarMax = Integer.parseInt(element.getAttribute("StarMax") == null || element.getAttribute("StarMax").length() == 0 ? "0"
			: element.getAttribute("StarMax")); // 星级上限
		StarGrowthAttribute = Integer.parseInt(element.getAttribute("StarGrowthAttribute") == null || element.getAttribute("StarGrowthAttribute").length() == 0 ? "0"
			: element.getAttribute("StarGrowthAttribute")); // 星级成长属性id 调用AttributeVlalue属性数值表
	}
	

}
