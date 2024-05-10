package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 充值商店
 * 
 * 工具生成的，不要手动修改
 */
 public class RechargeStoreConfig {

	/** id */
	public final int ID;		
	/** 包含物品 */
	public final int Item;		
	/** 所属商店 商店总表ID */
	public final int Type;		

	public RechargeStoreConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // id
		Item = Integer.parseInt(element.getAttribute("Item") == null || element.getAttribute("Item").length() == 0 ? "0"
			: element.getAttribute("Item")); // 包含物品
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 所属商店 商店总表ID
	}
	

}
