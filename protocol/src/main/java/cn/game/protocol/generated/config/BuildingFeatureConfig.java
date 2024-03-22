package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 建筑功能类型表
 * 
 * 工具生成的，不要手动修改
 */
 public class BuildingFeatureConfig {

	/** id */
	private int id;		
	/** 英文名称 */
	private String name;		
	/** 名称 */
	private String desc;		

	public BuildingFeatureConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 英文名称
		this.desc = element.getAttribute("desc"); // 名称
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDesc() {
		return desc;
	}
	
}
