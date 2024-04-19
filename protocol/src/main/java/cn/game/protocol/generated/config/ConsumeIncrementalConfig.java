package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 递增消耗
 * 
 * 工具生成的，不要手动修改
 */
 public class ConsumeIncrementalConfig {

	/** 增量消耗ID */
	public final int ID;		
	/** 增量消耗组ID */
	public final int ConsumeINCRGroupID;		
	/** 次数索引 */
	public final int Order;		
	/** 消耗物品 */
	public final int[][] ConsumeItem;		

	public ConsumeIncrementalConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 增量消耗ID
		ConsumeINCRGroupID = Integer.parseInt(element.getAttribute("ConsumeINCRGroupID") == null || element.getAttribute("ConsumeINCRGroupID").length() == 0 ? "0"
			: element.getAttribute("ConsumeINCRGroupID")); // 增量消耗组ID
		Order = Integer.parseInt(element.getAttribute("Order") == null || element.getAttribute("Order").length() == 0 ? "0"
			: element.getAttribute("Order")); // 次数索引
		String ConsumeItemString = element.getAttribute("ConsumeItem"); // 消耗物品
		if (ConsumeItemString != null && ConsumeItemString.length() > 0) {
			String[] ConsumeItemStrings = ConsumeItemString.split("\\|"); 
			int[][] ConsumeItemTemp = new int[ConsumeItemStrings.length][] ; 
			for (int i = 0; i < ConsumeItemStrings.length; i++) {
				String[] ConsumeItemStrings2 = ConsumeItemStrings[i].split(";"); 
				int[] array = new int[ConsumeItemStrings2.length];
				for (int j = 0; j < ConsumeItemStrings2.length; j++) {
					int temp = Integer.parseInt(ConsumeItemStrings2[j]);	
					array[j] = temp;
				}
				ConsumeItemTemp[i] = array;
			}
			ConsumeItem = ConsumeItemTemp ;			
		} else {
			ConsumeItem = new int[][] {};
		}
	}
	

}
