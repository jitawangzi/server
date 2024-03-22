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
	public final int item;		
	/** 显示条件ID */
	public final int viewCondition;		
	/** 显示类型 */
	public final int viewType;		

	public ShopConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // id
		item = Integer.parseInt(element.getAttribute("item") == null || element.getAttribute("item").length() == 0 ? "0"
			: element.getAttribute("item")); // 商品实例
		viewCondition = Integer.parseInt(element.getAttribute("viewCondition") == null || element.getAttribute("viewCondition").length() == 0 ? "0"
			: element.getAttribute("viewCondition")); // 显示条件ID
		viewType = Integer.parseInt(element.getAttribute("viewType") == null || element.getAttribute("viewType").length() == 0 ? "0"
			: element.getAttribute("viewType")); // 显示类型
	}
	

}
