package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 角色晋升表
 * 
 * 工具生成的，不要手动修改
 */
 public class RolePromotionConfig {

	/** 星级id -- 晋升等级 */
	private final int id;		
	/** 消耗资源 -- 升级到下一级所需的晋升点数 10级资源达成也不变了 */
	private final int cost;		

	public RolePromotionConfig (Element element) throws Exception {
	
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
