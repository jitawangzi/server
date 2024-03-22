package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 主线地区表
 * 
 * 工具生成的，不要手动修改
 */
 public class MainlineAreaConfig {

	/** id -- id */
	private final int id;		
	/** 时空类型 -- 时空类型 */
	private final int type;		
	/** 地图资源 -- 地图资源 */
	private final String resource;		

	public MainlineAreaConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 时空类型
		this.resource = element.getAttribute("resource"); // 地图资源
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
	
	public String getResource() {
		return resource;
	}
	
}
