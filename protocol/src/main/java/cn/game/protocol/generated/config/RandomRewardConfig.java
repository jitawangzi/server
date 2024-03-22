package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 随机奖励
 * 
 * 工具生成的，不要手动修改
 */
 public class RandomRewardConfig {

	/** id */
	private final int id;		
	/** 物品id -- 资源表的id都可以配置 */
	private final int itemId;		
	/** 物品数量 */
	private final int num;		
	/** 权重 */
	private final int proportion;		
	/** 组id */
	private final int groupId;		

	public RandomRewardConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.itemId = Integer.parseInt(element.getAttribute("itemId") == null || element.getAttribute("itemId").length() == 0 ? "0"
			: element.getAttribute("itemId")); // 物品id
		this.num = Integer.parseInt(element.getAttribute("num") == null || element.getAttribute("num").length() == 0 ? "0"
			: element.getAttribute("num")); // 物品数量
		this.proportion = Integer.parseInt(element.getAttribute("proportion") == null || element.getAttribute("proportion").length() == 0 ? "0"
			: element.getAttribute("proportion")); // 权重
		this.groupId = Integer.parseInt(element.getAttribute("groupId") == null || element.getAttribute("groupId").length() == 0 ? "0"
			: element.getAttribute("groupId")); // 组id
	}
	
	public int getId() {
		return id;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public int getNum() {
		return num;
	}
	
	public int getProportion() {
		return proportion;
	}
	
	public int getGroupId() {
		return groupId;
	}
	
}
