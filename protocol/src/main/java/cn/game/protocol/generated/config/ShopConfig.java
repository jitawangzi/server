package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 商店
 * 
 * 工具生成的，不要手动修改
 */
 public class ShopConfig {

	/** id */
	public final int ID;		
	/** 商品实例 */
	public final int Item;		
	/** 显示条件ID */
	public final int ViewCondition;		
	/** 显示类型 */
	public final int ViewType;		

	public ShopConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // id
		Item = Integer.parseInt(element.getAttribute("Item") == null || element.getAttribute("Item").length() == 0 ? "0"
			: element.getAttribute("Item")); // 商品实例
		ViewCondition = Integer.parseInt(element.getAttribute("ViewCondition") == null || element.getAttribute("ViewCondition").length() == 0 ? "0"
			: element.getAttribute("ViewCondition")); // 显示条件ID
		ViewType = Integer.parseInt(element.getAttribute("ViewType") == null || element.getAttribute("ViewType").length() == 0 ? "0"
			: element.getAttribute("ViewType")); // 显示类型
	}
	

}
