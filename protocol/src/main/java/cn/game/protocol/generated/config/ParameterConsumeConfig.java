package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 参数消耗
 * 
 * 工具生成的，不要手动修改
 */
 public class ParameterConsumeConfig {

	/** 表唯一id */
	public final int ID;		
	/** 消耗类型 消耗类型和参数两个值确定唯一的一条数据： 1英雄升级 */
	public final int type;		
	/** 消耗类型对应的参数 1英雄职业 */
	public final int typeParam;		
	/** 消耗物品1 考虑用money */
	public final int item1;		
	/** 消耗物品1公式系数1 */
	public final int[] item1Param;		
	/** 消耗物品2 */
	public final int item2;		
	/** 物品2公式系数 */
	public final int[] item2Param;		
	/** 消耗物品3id */
	public final int item3;		
	/** 物品3公式系数 */
	public final int[] item3Param;		

	public ParameterConsumeConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 表唯一id
		type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 消耗类型 消耗类型和参数两个值确定唯一的一条数据： 1英雄升级
		typeParam = Integer.parseInt(element.getAttribute("typeParam") == null || element.getAttribute("typeParam").length() == 0 ? "0"
			: element.getAttribute("typeParam")); // 消耗类型对应的参数 1英雄职业
		item1 = Integer.parseInt(element.getAttribute("item1") == null || element.getAttribute("item1").length() == 0 ? "0"
			: element.getAttribute("item1")); // 消耗物品1 考虑用money
		String item1ParamString = element.getAttribute("item1Param"); // 消耗物品1公式系数1
		if (item1ParamString != null && item1ParamString.length() > 0) {
			String[] item1ParamStrings = item1ParamString.split(";"); 
			int[] item1ParamTemp = new int[item1ParamStrings.length] ; 
			for (int i = 0; i < item1ParamStrings.length; i++) {
				int temp = Integer.parseInt(item1ParamStrings[i]);	
				item1ParamTemp[i] = temp;
			}
			item1Param = item1ParamTemp ;			
		} else {
			item1Param = new int[] {};
		}
		item2 = Integer.parseInt(element.getAttribute("item2") == null || element.getAttribute("item2").length() == 0 ? "0"
			: element.getAttribute("item2")); // 消耗物品2
		String item2ParamString = element.getAttribute("item2Param"); // 物品2公式系数
		if (item2ParamString != null && item2ParamString.length() > 0) {
			String[] item2ParamStrings = item2ParamString.split(";"); 
			int[] item2ParamTemp = new int[item2ParamStrings.length] ; 
			for (int i = 0; i < item2ParamStrings.length; i++) {
				int temp = Integer.parseInt(item2ParamStrings[i]);	
				item2ParamTemp[i] = temp;
			}
			item2Param = item2ParamTemp ;			
		} else {
			item2Param = new int[] {};
		}
		item3 = Integer.parseInt(element.getAttribute("item3") == null || element.getAttribute("item3").length() == 0 ? "0"
			: element.getAttribute("item3")); // 消耗物品3id
		String item3ParamString = element.getAttribute("item3Param"); // 物品3公式系数
		if (item3ParamString != null && item3ParamString.length() > 0) {
			String[] item3ParamStrings = item3ParamString.split(";"); 
			int[] item3ParamTemp = new int[item3ParamStrings.length] ; 
			for (int i = 0; i < item3ParamStrings.length; i++) {
				int temp = Integer.parseInt(item3ParamStrings[i]);	
				item3ParamTemp[i] = temp;
			}
			item3Param = item3ParamTemp ;			
		} else {
			item3Param = new int[] {};
		}
	}
	

}
