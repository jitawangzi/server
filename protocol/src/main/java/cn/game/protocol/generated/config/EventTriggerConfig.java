package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 突发事件触发器
 * 
 * 工具生成的，不要手动修改
 */
 public class EventTriggerConfig {

	/** id */
	private final int id;		
	/** 事件类型 -- 1-经验事件 2-金币事件 3-核心事件 4-芯片事件 5-礼物事件 6-卡卷事件 */
	private final int type;		
	/** 随机权重 -- 当前 */
	private final int proportion;		
	/** 每日最大次数 */
	private final int maxNum;		

	public EventTriggerConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 事件类型
		this.proportion = Integer.parseInt(element.getAttribute("proportion") == null || element.getAttribute("proportion").length() == 0 ? "0"
			: element.getAttribute("proportion")); // 随机权重
		this.maxNum = Integer.parseInt(element.getAttribute("maxNum") == null || element.getAttribute("maxNum").length() == 0 ? "0"
			: element.getAttribute("maxNum")); // 每日最大次数
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
	
	public int getProportion() {
		return proportion;
	}
	
	public int getMaxNum() {
		return maxNum;
	}
	
}
