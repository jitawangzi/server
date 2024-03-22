package cn.game.protocol.generated.config;

import cn.game.util.Weightable;
import org.w3c.dom.Element;

/**
 * 宝箱投放表
 * 
 * 工具生成的，不要手动修改
 */
 public class RPBoxExpectationConfig implements Weightable {

	/** id */
	private final int id;		
	/** 资源id */
	private final int resourceId;		
	/** 权重 */
	private final int weight;		
	/** 最小数量 */
	private final int min;		
	/** 最大数量 */
	private final int max;		
	/** 地貌 -- 1-工业区 2-流放区 3-乐园区 4-地下区 5-序章 6-程序开发 7-美术演示 */
	private final int landforms;		

	public RPBoxExpectationConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.resourceId = Integer.parseInt(element.getAttribute("resourceId") == null || element.getAttribute("resourceId").length() == 0 ? "0"
			: element.getAttribute("resourceId")); // 资源id
		this.weight = Integer.parseInt(element.getAttribute("weight") == null || element.getAttribute("weight").length() == 0 ? "0"
			: element.getAttribute("weight")); // 权重
		this.min = Integer.parseInt(element.getAttribute("min") == null || element.getAttribute("min").length() == 0 ? "0"
			: element.getAttribute("min")); // 最小数量
		this.max = Integer.parseInt(element.getAttribute("max") == null || element.getAttribute("max").length() == 0 ? "0"
			: element.getAttribute("max")); // 最大数量
		this.landforms = Integer.parseInt(element.getAttribute("landforms") == null || element.getAttribute("landforms").length() == 0 ? "0"
			: element.getAttribute("landforms")); // 地貌
	}
	
	public int getId() {
		return id;
	}
	
	public int getResourceId() {
		return resourceId;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
	public int getLandforms() {
		return landforms;
	}
	
	@Override
	public int weight() {
		return this.weight;
	}
}
