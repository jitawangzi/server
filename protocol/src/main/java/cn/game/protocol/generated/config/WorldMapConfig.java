package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 区域地图
 * 
 * 工具生成的，不要手动修改
 */
 public class WorldMapConfig {

	/** id */
	private final int id;		

	public WorldMapConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
	}
	
	public int getId() {
		return id;
	}
	
}
