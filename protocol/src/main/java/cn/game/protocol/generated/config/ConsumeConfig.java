package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 消耗表
 * 
 * 工具生成的，不要手动修改
 */
 public class ConsumeConfig {

	/** 消耗ID */
	public final int ID;		
	/** 消耗物品1 */
	public final int[][] cost;		

	public ConsumeConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 消耗ID
		String costString = element.getAttribute("cost"); // 消耗物品1
		if (costString != null && costString.length() > 0) {
			String[] costStrings = costString.split("\\|"); 
			int[][] costTemp = new int[costStrings.length][] ; 
			for (int i = 0; i < costStrings.length; i++) {
				String[] costStrings2 = costStrings[i].split(";"); 
				int[] array = new int[costStrings2.length];
				for (int j = 0; j < costStrings2.length; j++) {
					int temp = Integer.parseInt(costStrings2[j]);	
					array[j] = temp;
				}
				costTemp[i] = array;
			}
			cost = costTemp ;			
		} else {
			cost = new int[][] {};
		}
	}
	

}
