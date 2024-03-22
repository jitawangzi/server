package cn.game.protocol.generated.config;

import cn.game.util.Weightable;
import org.w3c.dom.Element;

/**
 * 事件表
 * 
 * 工具生成的，不要手动修改
 */
 public class EventConfig implements Weightable {

	/** id */
	private final int id;		
	/** 类型 -- 1-充满希望 2-良好 3-一般 4-较差 5-令人失望 6-遗骸事件 7-遗迹事件好 8-遗迹事件坏 */
	private final int type;		
	/** 事件图标 */
	private final String icon;		
	/** 权重 */
	private final int weight;		

	public EventConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 类型
		this.icon = element.getAttribute("icon"); // 事件图标
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
	
	public int getWeight() {
		return weight;
	}
	
	@Override
	public int weight() {
		return this.weight;
	}
}
