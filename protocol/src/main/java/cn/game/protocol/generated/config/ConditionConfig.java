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
