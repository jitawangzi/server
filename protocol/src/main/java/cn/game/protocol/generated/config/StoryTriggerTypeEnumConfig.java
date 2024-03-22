package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 触发器条件表
 * 
 * 工具生成的，不要手动修改
 */
 public class StoryTriggerTypeEnumConfig {

	/** id -- id */
	private final int id;		
	/** 名称 -- 名称 */
	private final String name;		

	public StoryTriggerTypeEnumConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 名称
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
}
