package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 福利类型
 * 
 * 工具生成的，不要手动修改
 */
 public class WelfareTypeEnumConfig {

	/** id */
	public final int ID;		
	/** 英文名称 */
	public final String name;		
	/** 说明 */
	public final String desc;		

	public WelfareTypeEnumConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // id
		name = element.getAttribute("name"); // 英文名称
		desc = element.getAttribute("desc"); // 说明
	}
	

}
