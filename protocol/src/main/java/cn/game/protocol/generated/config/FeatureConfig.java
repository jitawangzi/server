package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 系统开启权限
 * 
 * 工具生成的，不要手动修改
 */
 public class FeatureConfig {

	/** id */
	private final int id;		
	/** 英文名称 */
	private final String name;		
	/** 限制条件 */
	private final List<Integer> condition;		

	public FeatureConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 英文名称
		String conditionString = element.getAttribute("condition"); // 限制条件
		if (conditionString != null && conditionString.length() > 0) {
			String[] conditionStrings = conditionString.split("\\|"); 
			List<Integer> condition = new ArrayList<Integer>(conditionStrings.length) ; 
			for (int i = 0; i < conditionStrings.length; i++) {
				Integer temp = Integer.parseInt(conditionStrings[i]);
				condition.add(temp);
			}
			this.condition = com.google.common.collect.ImmutableList.copyOf(condition);						
		} else {
			this.condition = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public List<Integer> getCondition() {
		return condition;
	}
	
}
