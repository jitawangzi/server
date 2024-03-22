package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 灵武表
 * 
 * 工具生成的，不要手动修改
 */
 public class SoulWeaponListConfig {

	/** id -- 角色id */
	private final int id;		
	/** 属性值 -- 属性值 */
	private final List<List<Integer>> attribute;		
	/** 奇点槽位数量 -- 奇点槽位数量 */
	private final int singularityNumber;		
	/** 槽位限制等级 -- 槽位限制等级 */
	private final List<Integer> levelLimits;		

	public SoulWeaponListConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String attributeString = element.getAttribute("attribute"); // 属性值
		if (attributeString != null && attributeString.length() > 0) {
			String[] attributeStrings = attributeString.split("\\|"); 
			List<List<Integer>> attribute = new ArrayList<List<Integer>>(attributeStrings.length) ; 
			for (int i = 0; i < attributeStrings.length; i++) {
				String[] attributeStrings2 = attributeStrings[i].split(":"); 
				List<Integer> list = new ArrayList<Integer>(attributeStrings2.length) ; 
				for (int j = 0; j < attributeStrings2.length; j++) {
					Integer temp = Integer.parseInt(attributeStrings2[j]);
					list.add(temp) ; 
				}
				attribute.add(list);
			}
			this.attribute = com.google.common.collect.ImmutableList.copyOf(attribute);						
		} else {
			this.attribute = java.util.Collections.emptyList();
		}
		this.singularityNumber = Integer.parseInt(element.getAttribute("singularityNumber") == null || element.getAttribute("singularityNumber").length() == 0 ? "0"
			: element.getAttribute("singularityNumber")); // 奇点槽位数量
		String levelLimitsString = element.getAttribute("levelLimits"); // 槽位限制等级
		if (levelLimitsString != null && levelLimitsString.length() > 0) {
			String[] levelLimitsStrings = levelLimitsString.split("\\|"); 
			List<Integer> levelLimits = new ArrayList<Integer>(levelLimitsStrings.length) ; 
			for (int i = 0; i < levelLimitsStrings.length; i++) {
				Integer temp = Integer.parseInt(levelLimitsStrings[i]);
				levelLimits.add(temp);
			}
			this.levelLimits = com.google.common.collect.ImmutableList.copyOf(levelLimits);						
		} else {
			this.levelLimits = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public List<List<Integer>> getAttribute() {
		return attribute;
	}
	
	public int getSingularityNumber() {
		return singularityNumber;
	}
	
	public List<Integer> getLevelLimits() {
		return levelLimits;
	}
	
}
