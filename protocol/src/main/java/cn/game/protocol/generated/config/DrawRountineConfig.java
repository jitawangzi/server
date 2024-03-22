package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 普通卡池配置表
 * 
 * 工具生成的，不要手动修改
 */
 public class DrawRountineConfig {

	/** id -- id */
	private final int id;		
	/** 角色id -- id */
	private final int roleId;		
	/** 权重 -- 权重 */
	private final int weight;		

	public DrawRountineConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.roleId = Integer.parseInt(element.getAttribute("roleId") == null || element.getAttribute("roleId").length() == 0 ? "0"
			: element.getAttribute("roleId")); // 角色id
		this.weight = Integer.parseInt(element.getAttribute("weight") == null || element.getAttribute("weight").length() == 0 ? "0"
			: element.getAttribute("weight")); // 权重
	}
	
	public int getId() {
		return id;
	}
	
	public int getRoleId() {
		return roleId;
	}
	
	public int getWeight() {
		return weight;
	}
	
}
