package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 佣兵名称库
 * 
 * 工具生成的，不要手动修改
 */
 public class MercenaryNameConfig {

	/** id -- id */
	private final int id;		
	/** 角色id */
	private final int nameId;		

	public MercenaryNameConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.nameId = Integer.parseInt(element.getAttribute("nameId") == null || element.getAttribute("nameId").length() == 0 ? "0"
			: element.getAttribute("nameId")); // 角色id
	}
	
	public int getId() {
		return id;
	}
	
	public int getNameId() {
		return nameId;
	}
	
}
