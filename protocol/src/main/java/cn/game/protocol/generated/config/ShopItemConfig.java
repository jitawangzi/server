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
	public final int[][] Item;		
	/** 可买次数 0=不限次 */
	public final int PurchaseCnt;		
	/** 购买类型 1=正常购买 2=随机购买 3=每次充值首次免费 4=终身首次双倍 */
	public final int PurchaseType;		
	/** 购买参数 1=货币；货币ID；数量 2=充值；数量 3=广告 */
	public final int[] PurchaseParameter;		
	/** 折扣 90=蓝色标签 80=紫色标签 70=黄色标签 60=红色标签 50=大促标签 */
	public final int[][] Discount;		

	public ShopItemConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 商品id
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
		PurchaseCnt = Integer.parseInt(element.getAttribute("PurchaseCnt") == null || element.getAttribute("PurchaseCnt").length() == 0 ? "0"
			: element.getAttribute("PurchaseCnt")); // 可买次数 0=不限次
		PurchaseType = Integer.parseInt(element.getAttribute("PurchaseType") == null || element.getAttribute("PurchaseType").length() == 0 ? "0"
			: element.getAttribute("PurchaseType")); // 购买类型 1=正常购买 2=随机购买 3=每次充值首次免费 4=终身首次双倍
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
		String DiscountString = element.getAttribute("Discount"); // 折扣 90=蓝色标签 80=紫色标签 70=黄色标签 60=红色标签 50=大促标签
		if (DiscountString != null && DiscountString.length() > 0) {
			String[] DiscountStrings = DiscountString.split("\\|"); 
			int[][] DiscountTemp = new int[DiscountStrings.length][] ; 
			for (int i = 0; i < DiscountStrings.length; i++) {
				String[] DiscountStrings2 = DiscountStrings[i].split(";"); 
				int[] array = new int[DiscountStrings2.length];
				for (int j = 0; j < DiscountStrings2.length; j++) {
					int temp = Integer.parseInt(DiscountStrings2[j]);	
					array[j] = temp;
				}
				DiscountTemp[i] = array;
			}
			Discount = DiscountTemp ;			
		} else {
			Discount = new int[][] {};
		}
	}
	

}
