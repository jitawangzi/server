package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 神元
 * 
 * 工具生成的，不要手动修改
 */
 public class AlchemyConfig {

	/** 炼金ID */
	public final int ID;		
	/** 炼金名称 */
	public final String Name;		
	/** 炼金解锁等级 （玩家等级） */
	public final int AlchemyLv;		
	/** 炼金属性  属性id;属性每级增量  百分比属性/10000用 */
	public final int[] AlchemyAttribute;		
	/** 炼金每级消耗金币增量  炼金消耗金币数（item=100002）=当前等级*每级消耗金币增量 */
	public final int AlchemyConsumeMoney;		
	/** 炼金图标Icon 文件名 */
	public final String Icon;		

	public AlchemyConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 炼金ID
		Name = element.getAttribute("Name"); // 炼金名称
		AlchemyLv = Integer.parseInt(element.getAttribute("AlchemyLv") == null || element.getAttribute("AlchemyLv").length() == 0 ? "0"
			: element.getAttribute("AlchemyLv")); // 炼金解锁等级 （玩家等级）
		String AlchemyAttributeString = element.getAttribute("AlchemyAttribute"); // 炼金属性  属性id;属性每级增量  百分比属性/10000用
		if (AlchemyAttributeString != null && AlchemyAttributeString.length() > 0) {
			String[] AlchemyAttributeStrings = AlchemyAttributeString.split(";"); 
			int[] AlchemyAttributeTemp = new int[AlchemyAttributeStrings.length] ; 
			for (int i = 0; i < AlchemyAttributeStrings.length; i++) {
				int temp = Integer.parseInt(AlchemyAttributeStrings[i]);	
				AlchemyAttributeTemp[i] = temp;
			}
			AlchemyAttribute = AlchemyAttributeTemp ;			
		} else {
			AlchemyAttribute = new int[] {};
		}
		AlchemyConsumeMoney = Integer.parseInt(element.getAttribute("AlchemyConsumeMoney") == null || element.getAttribute("AlchemyConsumeMoney").length() == 0 ? "0"
			: element.getAttribute("AlchemyConsumeMoney")); // 炼金每级消耗金币增量  炼金消耗金币数（item=100002）=当前等级*每级消耗金币增量
		Icon = element.getAttribute("Icon"); // 炼金图标Icon 文件名
	}
	

}
