package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 装备属性模板
 * 
 * 工具生成的，不要手动修改
 */
 public class EquipmentAttributteTemplateConfig {

	/** id -- id */
	private final int id;		
	/** 强化等级 */
	private final int strength;		
	/** 模板类型 -- 1-一类武器 2-一类护具 3-二类武器 4-二类护具 5-三类武器 6-三类护具 7-四类武器 8-四类护具 */
	private final int type;		
	/** 属性类型 -- 属性:属性值 累积总值：当前等级属性+初始属性 额外增加值显示:下一级属性-当前等级属性值 */
	private final List<Entry<Integer,Integer>> attribute;		

	public EquipmentAttributteTemplateConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.strength = Integer.parseInt(element.getAttribute("strength") == null || element.getAttribute("strength").length() == 0 ? "0"
			: element.getAttribute("strength")); // 强化等级
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 模板类型
		String attributeString = element.getAttribute("attribute"); // 属性类型
		if (attributeString != null && attributeString.length() > 0) {
			String[] attributeStrings = attributeString.split("\\|"); 
			List<Entry<Integer,Integer>> attribute = new ArrayList<Entry<Integer,Integer>>(attributeStrings.length) ; 
			for (int i = 0; i < attributeStrings.length; i++) {
			    String[] split = attributeStrings[i].split(":", 2);
				attribute.add(new Entry<Integer,Integer>()	{
					@Override
					public Integer setValue(Integer value) {
						return null;
					}
					@Override
					public Integer getValue() {
						try {
							return Integer.parseInt(split[1]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						throw new NullPointerException();	
					}
					@Override
					public Integer getKey() {
						try {
							return Integer.parseInt(split[0]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						throw new NullPointerException();
					}
				}) ; 
			}

			this.attribute = com.google.common.collect.ImmutableList.copyOf(attribute);						
		} else {
			this.attribute = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getStrength() {
		return strength;
	}
	
	public int getType() {
		return type;
	}
	
	public List<Entry<Integer,Integer>> getAttribute() {
		return attribute;
	}
	
}
