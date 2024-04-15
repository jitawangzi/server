package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 礼包
 * 
 * 工具生成的，不要手动修改
 */
 public class ShopGiftConfig {

	/** id */
	public final int ID;		
	/** 礼包组ID */
	public final int RewardID;		
	/** 包含物品 */
	public final int[][] Item;		
	/** 购买条件id */
	public final int Condition;		
	/** 折扣显示 万分比 */
	public final int Discount;		
	/** 限购次数 */
	public final int PurchaseCnt;		
	/** 售价 1=货币；货币id；数量 2=充值；数量 3=看广告 */
	public final int[] Price;		

	public ShopGiftConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // id
		RewardID = Integer.parseInt(element.getAttribute("RewardID") == null || element.getAttribute("RewardID").length() == 0 ? "0"
			: element.getAttribute("RewardID")); // 礼包组ID
		String ItemString = element.getAttribute("Item"); // 包含物品
		if (ItemString != null && ItemString.length() > 0) {
			String[] ItemStrings = ItemString.split("\\|"); 
			int[][] ItemTemp = new int[ItemStrings.length][] ; 
			for (int i = 0; i < ItemStrings.length; i++) {
				String[] ItemStrings2 = ItemStrings[i].split(";"); 
				int[] array = new int[ItemStrings2.length];
				for (int j = 0; j < ItemStrings2.length; j++) {
					int temp = Integer.parseInt(ItemStrings2[j]);	
					array[j] = temp;
				}
				ItemTemp[i] = array;
			}
			Item = ItemTemp ;			
		} else {
			Item = new int[][] {};
		}
		Condition = Integer.parseInt(element.getAttribute("Condition") == null || element.getAttribute("Condition").length() == 0 ? "0"
			: element.getAttribute("Condition")); // 购买条件id
		Discount = Integer.parseInt(element.getAttribute("Discount") == null || element.getAttribute("Discount").length() == 0 ? "0"
			: element.getAttribute("Discount")); // 折扣显示 万分比
		PurchaseCnt = Integer.parseInt(element.getAttribute("PurchaseCnt") == null || element.getAttribute("PurchaseCnt").length() == 0 ? "0"
			: element.getAttribute("PurchaseCnt")); // 限购次数
		String PriceString = element.getAttribute("Price"); // 售价 1=货币；货币id；数量 2=充值；数量 3=看广告
		if (PriceString != null && PriceString.length() > 0) {
			String[] PriceStrings = PriceString.split(";"); 
			int[] PriceTemp = new int[PriceStrings.length] ; 
			for (int i = 0; i < PriceStrings.length; i++) {
				int temp = Integer.parseInt(PriceStrings[i]);	
				PriceTemp[i] = temp;
			}
			Price = PriceTemp ;			
		} else {
			Price = new int[] {};
		}
	}
	

}
