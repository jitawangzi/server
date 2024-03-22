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
	public final int[][] items;		
	/** 可买次数 */
	public final int buyCount;		
	/** 购买类型 1=正常购买 2=随机折扣 3=每次重置首次免费 4=终身首次双倍 */
	public final int buyType;		
	/** 购买消耗 */
	public final int[] cost;		
	/** 折扣 */
	public final int[][] discount;		

	public ShopItemConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 商品id
		String itemsString = element.getAttribute("items"); // 包含物品
		if (itemsString != null && itemsString.length() > 0) {
			String[] itemsStrings = itemsString.split("\\|"); 
			int[][] itemsTemp = new int[itemsStrings.length][] ; 
			for (int i = 0; i < itemsStrings.length; i++) {
				String[] itemsStrings2 = itemsStrings[i].split(";"); 
				int[] array = new int[itemsStrings2.length];
				for (int j = 0; j < itemsStrings2.length; j++) {
					int temp = Integer.parseInt(itemsStrings2[j]);	
					array[j] = temp;
				}
				itemsTemp[i] = array;
			}
			items = itemsTemp ;			
		} else {
			items = new int[][] {};
		}
		buyCount = Integer.parseInt(element.getAttribute("buyCount") == null || element.getAttribute("buyCount").length() == 0 ? "0"
			: element.getAttribute("buyCount")); // 可买次数
		buyType = Integer.parseInt(element.getAttribute("buyType") == null || element.getAttribute("buyType").length() == 0 ? "0"
			: element.getAttribute("buyType")); // 购买类型 1=正常购买 2=随机折扣 3=每次重置首次免费 4=终身首次双倍
		String costString = element.getAttribute("cost"); // 购买消耗
		if (costString != null && costString.length() > 0) {
			String[] costStrings = costString.split(";"); 
			int[] costTemp = new int[costStrings.length] ; 
			for (int i = 0; i < costStrings.length; i++) {
				int temp = Integer.parseInt(costStrings[i]);	
				costTemp[i] = temp;
			}
			cost = costTemp ;			
		} else {
			cost = new int[] {};
		}
		String discountString = element.getAttribute("discount"); // 折扣
		if (discountString != null && discountString.length() > 0) {
			String[] discountStrings = discountString.split("\\|"); 
			int[][] discountTemp = new int[discountStrings.length][] ; 
			for (int i = 0; i < discountStrings.length; i++) {
				String[] discountStrings2 = discountStrings[i].split(";"); 
				int[] array = new int[discountStrings2.length];
				for (int j = 0; j < discountStrings2.length; j++) {
					int temp = Integer.parseInt(discountStrings2[j]);	
					array[j] = temp;
				}
				discountTemp[i] = array;
			}
			discount = discountTemp ;			
		} else {
			discount = new int[][] {};
		}
	}
	

}
