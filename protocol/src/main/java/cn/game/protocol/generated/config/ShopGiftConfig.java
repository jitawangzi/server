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
	public final int group;		
	/** 包含物品 */
	public final int[][] items;		
	/** 购买条件id */
	public final int buyCondtion;		
	/** 售价 */
	public final int[] cost;		

	public ShopGiftConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // id
		group = Integer.parseInt(element.getAttribute("group") == null || element.getAttribute("group").length() == 0 ? "0"
			: element.getAttribute("group")); // 礼包组ID
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
		buyCondtion = Integer.parseInt(element.getAttribute("buyCondtion") == null || element.getAttribute("buyCondtion").length() == 0 ? "0"
			: element.getAttribute("buyCondtion")); // 购买条件id
		String costString = element.getAttribute("cost"); // 售价
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
	}
	

}
