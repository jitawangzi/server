package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 技能标签列表
 * 
 * 工具生成的，不要手动修改
 */
 public class SkillTitleListConfig {

	/** id */
	private int id;		
	/** 描述 */
	private String desc;		

	public SkillTitleListConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.desc = element.getAttribute("desc"); // 描述
	}
	
	public int getId() {
		return id;
	}
	
	public String getDesc() {
		return desc;
	}
	
}
