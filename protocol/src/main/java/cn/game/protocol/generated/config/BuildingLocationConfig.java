package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 建筑位置表
 * 
 * 工具生成的，不要手动修改
 */
 public class BuildingLocationConfig {

	/** 位置id -- 每层预留100个id */
	private final int id;		
	/** 所属层 -- 1) 近景：装饰； 2) 第一层地面：用于放置建筑； 3) 第二层地面：用于放置建筑； 4) 远景和天空：装饰； 5）地面：装饰 */
	private final byte floor;		

	public BuildingLocationConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 位置id
		this.floor = Byte.parseByte(element.getAttribute("floor") == null || element.getAttribute("floor").length() == 0 ? "0"
			: element.getAttribute("floor")); // 所属层
	}
	
	public int getId() {
		return id;
	}
	
	public byte getFloor() {
		return floor;
	}
	
}
