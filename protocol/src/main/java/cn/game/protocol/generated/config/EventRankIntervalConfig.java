package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 等级段
 * 
 * 工具生成的，不要手动修改
 */
 public class EventRankIntervalConfig {

	/** id */
	private final int id;		
	/** 最小等级 */
	private final int min;		
	/** 最大等级 */
	private final int max;		

	public EventRankIntervalConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.min = Integer.parseInt(element.getAttribute("min") == null || element.getAttribute("min").length() == 0 ? "0"
			: element.getAttribute("min")); // 最小等级
		this.max = Integer.parseInt(element.getAttribute("max") == null || element.getAttribute("max").length() == 0 ? "0"
			: element.getAttribute("max")); // 最大等级
	}
	
	public int getId() {
		return id;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
}
