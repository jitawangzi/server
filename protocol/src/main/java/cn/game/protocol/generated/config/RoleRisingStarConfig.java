package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 角色升星表
 * 
 * 工具生成的，不要手动修改
 */
 public class RoleRisingStarConfig {

	/** 星级id -- 星级id */
	private final int id;		
	/** 消耗资源 -- 消耗碎片 */
	private final int cost;		

	public RoleRisingStarConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 星级id
		this.cost = Integer.parseInt(element.getAttribute("cost") == null || element.getAttribute("cost").length() == 0 ? "0"
			: element.getAttribute("cost")); // 消耗资源
	}
	
	public int getId() {
		return id;
	}
	
	public int getCost() {
		return cost;
	}
	
}
