package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 活动类型
 * 
 * 工具生成的，不要手动修改
 */
 public class ActivityTypeEnumConfig {

	/** 活动类型 */
	public final int ID;		
	/** 物品英文名 */
	public final String Name;		
	/** 物品名称 */
	public final String Desc;		

	public ActivityTypeEnumConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 活动类型
		Name = element.getAttribute("Name"); // 物品英文名
		Desc = element.getAttribute("Desc"); // 物品名称
	}
	

}
