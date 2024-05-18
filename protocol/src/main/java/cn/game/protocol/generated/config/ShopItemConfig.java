package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 商品
 * 
 * 工具生成的，不要手动修改
 */
 public class ShopItemConfig {

	/** 商品id */
	public final int ID;		
	/** 包含物品 */
	public final int[] Item;		
	/** 购买参数 1=货币；货币ID；数量 2=充值；数量(免费无需配置) 3=广告 */
	public final int[] PurchaseParameter;		

	public ShopItemConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 商品id
		String ItemString = element.getAttribute("Item"); // 包含物品
		if (ItemString != null && ItemString.length() > 0) {
			String[] ItemStrings = ItemString.split(";"); 
			int[] ItemTemp = new int[ItemStrings.length] ; 
			for (int i = 0; i < ItemStrings.length; i++) {
				int temp = Integer.parseInt(ItemStrings[i]);	
				ItemTemp[i] = temp;
			}
			Item = ItemTemp ;			
		} else {
			Item = new int[] {};
		}
		String PurchaseParameterString = element.getAttribute("PurchaseParameter"); // 购买参数 1=货币；货币ID；数量 2=充值；数量(免费无需配置) 3=广告
		if (PurchaseParameterString != null && PurchaseParameterString.length() > 0) {
			String[] PurchaseParameterStrings = PurchaseParameterString.split(";"); 
			int[] PurchaseParameterTemp = new int[PurchaseParameterStrings.length] ; 
			for (int i = 0; i < PurchaseParameterStrings.length; i++) {
				int temp = Integer.parseInt(PurchaseParameterStrings[i]);	
				PurchaseParameterTemp[i] = temp;
			}
			PurchaseParameter = PurchaseParameterTemp ;			
		} else {
			PurchaseParameter = new int[] {};
		}
	}
	

}
