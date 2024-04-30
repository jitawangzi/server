package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 地图
 * 
 * 工具生成的，不要手动修改
 */
 public class MapConfig {

	/** 地图ID */
	public final int ID;		

	public MapConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 地图ID
	}
	

}
