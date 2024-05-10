package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 商店总表
 * 
 * 工具生成的，不要手动修改
 */
 public class ShopConfig {

	/** id */
	public final int ID;		
	/** 类型 */
	public final int Type;		
	/** 页签顺序 */
	public final int Order;		
	/** 商店名称 */
	public final String Name;		
	/** 刷新规则 0=不需要刷新 1=每天0点刷新 */
	public final int Refresh;		

	public ShopConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // id
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 类型
		Order = Integer.parseInt(element.getAttribute("Order") == null || element.getAttribute("Order").length() == 0 ? "0"
			: element.getAttribute("Order")); // 页签顺序
		Name = element.getAttribute("Name"); // 商店名称
		Refresh = Integer.parseInt(element.getAttribute("Refresh") == null || element.getAttribute("Refresh").length() == 0 ? "0"
			: element.getAttribute("Refresh")); // 刷新规则 0=不需要刷新 1=每天0点刷新
	}
	

}
