package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 主线门表
 * 
 * 工具生成的，不要手动修改
 */
 public class MainlineDoorConfig {

	/** id -- id */
	private final int id;		
	/** 地图id -- 地图id */
	private final int mapId;		
	/** 资源 -- 资源 */
	private final String resource;		
	/** 入口类型 -- 1-室外左 2-室外右 3-房间入口 4-房间左 5-房间右 */
	private final int type;		
	/** 连接口 -- 连接口 */
	private final List<Integer> enter;		

	public MainlineDoorConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.mapId = Integer.parseInt(element.getAttribute("mapId") == null || element.getAttribute("mapId").length() == 0 ? "0"
			: element.getAttribute("mapId")); // 地图id
		this.resource = element.getAttribute("resource"); // 资源
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 入口类型
		String enterString = element.getAttribute("enter"); // 连接口
		if (enterString != null && enterString.length() > 0) {
			String[] enterStrings = enterString.split("\\|"); 
			List<Integer> enter = new ArrayList<Integer>(enterStrings.length) ; 
			for (int i = 0; i < enterStrings.length; i++) {
				Integer temp = Integer.parseInt(enterStrings[i]);
				enter.add(temp);
			}
			this.enter = com.google.common.collect.ImmutableList.copyOf(enter);						
		} else {
			this.enter = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getMapId() {
		return mapId;
	}
	
	public String getResource() {
		return resource;
	}
	
	public int getType() {
		return type;
	}
	
	public List<Integer> getEnter() {
		return enter;
	}
	
}
