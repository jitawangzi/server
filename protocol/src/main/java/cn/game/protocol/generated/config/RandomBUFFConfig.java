package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 随机
 * 
 * 工具生成的，不要手动修改
 */
 public class RandomBUFFConfig {

	/** ID */
	public final int ID;		
	/** BUFF类型 1=增益 2=减益 */
	public final int BuffType;		
	/** BUFF属性 */
	public final int[][] Attribute;		
	/** 权重 */
	public final int Weight;		

	public RandomBUFFConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		BuffType = Integer.parseInt(element.getAttribute("BuffType") == null || element.getAttribute("BuffType").length() == 0 ? "0"
			: element.getAttribute("BuffType")); // BUFF类型 1=增益 2=减益
		String AttributeString = element.getAttribute("Attribute"); // BUFF属性
		if (AttributeString != null && AttributeString.length() > 0) {
			String[] AttributeStrings = AttributeString.split("\\|"); 
			int[][] AttributeTemp = new int[AttributeStrings.length][] ; 
			for (int i = 0; i < AttributeStrings.length; i++) {
				String[] AttributeStrings2 = AttributeStrings[i].split(";"); 
				int[] array = new int[AttributeStrings2.length];
				for (int j = 0; j < AttributeStrings2.length; j++) {
					int temp = Integer.parseInt(AttributeStrings2[j]);	
					array[j] = temp;
				}
				AttributeTemp[i] = array;
			}
			Attribute = AttributeTemp ;			
		} else {
			Attribute = new int[][] {};
		}
		Weight = Integer.parseInt(element.getAttribute("Weight") == null || element.getAttribute("Weight").length() == 0 ? "0"
			: element.getAttribute("Weight")); // 权重
	}
	

}
