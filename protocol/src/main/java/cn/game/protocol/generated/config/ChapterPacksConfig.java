package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 章节礼包
 * 
 * 工具生成的，不要手动修改
 */
 public class ChapterPacksConfig {

	/** ID */
	public final int ID;		
	/** 购买条件 */
	public final int Condition;		
	/** 礼包顺序 */
	public final int Order;		
	/** 礼包内容 ID；数量|ID；数量 */
	public final int[][] Item;		
	/** 超值折扣 */
	public final int Discount;		
	/** 折扣角标 */
	public final String CornerMarker;		
	/** 购买参数 1=货币；货币ID；数量 2=充值；数量 3=广告 */
	public final int[] PurchaseParameter;		

	public ChapterPacksConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Condition = Integer.parseInt(element.getAttribute("Condition") == null || element.getAttribute("Condition").length() == 0 ? "0"
			: element.getAttribute("Condition")); // 购买条件
		Order = Integer.parseInt(element.getAttribute("Order") == null || element.getAttribute("Order").length() == 0 ? "0"
			: element.getAttribute("Order")); // 礼包顺序
		String ItemString = element.getAttribute("Item"); // 礼包内容 ID；数量|ID；数量
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
		Discount = Integer.parseInt(element.getAttribute("Discount") == null || element.getAttribute("Discount").length() == 0 ? "0"
			: element.getAttribute("Discount")); // 超值折扣
		CornerMarker = element.getAttribute("CornerMarker"); // 折扣角标
		String PurchaseParameterString = element.getAttribute("PurchaseParameter"); // 购买参数 1=货币；货币ID；数量 2=充值；数量 3=广告
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
