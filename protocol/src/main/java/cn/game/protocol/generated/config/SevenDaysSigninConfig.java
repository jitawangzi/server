package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 7日签到
 * 
 * 工具生成的，不要手动修改
 */
 public class SevenDaysSigninConfig {

	/** ID */
	public final int ID;		
	/** 登录天数 */
	public final int Days;		
	/** 包含物品 */
	public final int[][] Item;		

	public SevenDaysSigninConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Days = Integer.parseInt(element.getAttribute("Days") == null || element.getAttribute("Days").length() == 0 ? "0"
			: element.getAttribute("Days")); // 登录天数
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
	}
	

}
