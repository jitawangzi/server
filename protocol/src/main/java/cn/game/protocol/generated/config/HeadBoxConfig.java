package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 头像框
 * 
 * 工具生成的，不要手动修改
 */
 public class HeadBoxConfig {

	/** 索引 */
	public final int ID;		
	/** 使用条件ID */
	public final int ConditionsUse;		

	public HeadBoxConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 索引
		ConditionsUse = Integer.parseInt(element.getAttribute("ConditionsUse") == null || element.getAttribute("ConditionsUse").length() == 0 ? "0"
			: element.getAttribute("ConditionsUse")); // 使用条件ID
	}
	

}
