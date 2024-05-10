package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 首充
 * 
 * 工具生成的，不要手动修改
 */
 public class FirstChargeConfig {

	/** 索引 */
	public final int ID;		
	/** 活动ID */
	public final int ActivityiD;		
	/** 天数 */
	public final int Order;		
	/** 前置条件 购买完前一个礼包之后显示下一个礼包 未领取不影响购买 */
	public final int Preconditions;		
	/** 包含物品 */
	public final int[][] Item;		
	/** 售价 */
	public final int[] Price;		

	public FirstChargeConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 索引
		ActivityiD = Integer.parseInt(element.getAttribute("ActivityiD") == null || element.getAttribute("ActivityiD").length() == 0 ? "0"
			: element.getAttribute("ActivityiD")); // 活动ID
		Order = Integer.parseInt(element.getAttribute("Order") == null || element.getAttribute("Order").length() == 0 ? "0"
			: element.getAttribute("Order")); // 天数
		Preconditions = Integer.parseInt(element.getAttribute("Preconditions") == null || element.getAttribute("Preconditions").length() == 0 ? "0"
			: element.getAttribute("Preconditions")); // 前置条件 购买完前一个礼包之后显示下一个礼包 未领取不影响购买
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
		String PriceString = element.getAttribute("Price"); // 售价
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
