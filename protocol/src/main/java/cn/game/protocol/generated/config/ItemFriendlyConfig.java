package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 亲密度道具
 * 
 * 工具生成的，不要手动修改
 */
 public class ItemFriendlyConfig {

	/** id -- 相当于亲密度等级 */
	private final int id;		
	/** 类型 -- 1-蛋糕 2-鲜花 */
	private final int type;		
	/** 值 */
	private final int value;		

	public ItemFriendlyConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 类型
		this.value = Integer.parseInt(element.getAttribute("value") == null || element.getAttribute("value").length() == 0 ? "0"
			: element.getAttribute("value")); // 值
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
	
	public int getValue() {
		return value;
	}
	
}
