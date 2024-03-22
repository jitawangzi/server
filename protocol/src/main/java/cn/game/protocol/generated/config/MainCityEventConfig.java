package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 主城事件表
 * 
 * 工具生成的，不要手动修改
 */
 public class MainCityEventConfig {

	/** id */
	private int id;		
	/** 类型 */
	private int type;		
	/** 事件图标 */
	private String icon;		
	/** 事件描述 */
	private String des;		
	/** 权重 */
	private int weight;		

	public MainCityEventConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 类型
		this.icon = element.getAttribute("icon"); // 事件图标
		this.des = element.getAttribute("des"); // 事件描述
		this.weight = Integer.parseInt(element.getAttribute("weight") == null || element.getAttribute("weight").length() == 0 ? "0"
			: element.getAttribute("weight")); // 权重
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public String getDes() {
		return des;
	}
	
	public int getWeight() {
		return weight;
	}
	
}
