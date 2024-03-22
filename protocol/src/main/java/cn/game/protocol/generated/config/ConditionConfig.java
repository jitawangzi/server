package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 条件
 * 
 * 工具生成的，不要手动修改
 */
 public class ConditionConfig {

	/** 条件ID */
	public final int ID;		
	/** 条件类型 */
	public final int type;		
	/** 运算逻辑， 计算数值使用 1=大于 2=大于等于 3=等于 4=小于等于 5=小于 6=不等于 */
	public final int operator;		
	/** id参数 */
	public final int idParam;		
	/** 条件数值 */
	public final int numParam;		
	/** 扩展参数 */
	public final int[] extParam;		

	public ConditionConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 条件ID
		type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 条件类型
		operator = Integer.parseInt(element.getAttribute("operator") == null || element.getAttribute("operator").length() == 0 ? "0"
			: element.getAttribute("operator")); // 运算逻辑， 计算数值使用 1=大于 2=大于等于 3=等于 4=小于等于 5=小于 6=不等于
		idParam = Integer.parseInt(element.getAttribute("idParam") == null || element.getAttribute("idParam").length() == 0 ? "0"
			: element.getAttribute("idParam")); // id参数
		numParam = Integer.parseInt(element.getAttribute("numParam") == null || element.getAttribute("numParam").length() == 0 ? "0"
			: element.getAttribute("numParam")); // 条件数值
		String extParamString = element.getAttribute("extParam"); // 扩展参数
		if (extParamString != null && extParamString.length() > 0) {
			String[] extParamStrings = extParamString.split(";"); 
			int[] extParamTemp = new int[extParamStrings.length] ; 
			for (int i = 0; i < extParamStrings.length; i++) {
				int temp = Integer.parseInt(extParamStrings[i]);	
				extParamTemp[i] = temp;
			}
			extParam = extParamTemp ;			
		} else {
			extParam = new int[] {};
		}
	}
	

}
