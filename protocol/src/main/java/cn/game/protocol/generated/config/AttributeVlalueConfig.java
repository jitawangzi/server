package cn.game.protocol.generated.config;

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;


/**
 * 属性数值表
 * 
 * 工具生成的，不要手动修改
 */
 public class AttributeVlalueConfig {

	/** 成长属性id */
	public final int ID;		
	/** 属性ID1;属性值|属性ID2;属性值  所有属性都要填 */
	public final Map<Integer,Integer> AttributeVlalue;		

	public AttributeVlalueConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 成长属性id
		String AttributeVlalueString = element.getAttribute("AttributeVlalue"); // 属性ID1;属性值|属性ID2;属性值  所有属性都要填
		if (AttributeVlalueString != null && AttributeVlalueString.length() > 0) {
			String[] AttributeVlalueStrings = AttributeVlalueString.split("\\|"); 
			Map<Integer,Integer> AttributeVlalueTemp = new HashMap<Integer,Integer>(AttributeVlalueStrings.length) ; 
			for (int i = 0; i < AttributeVlalueStrings.length; i++) {
				String[] split = AttributeVlalueStrings[i].split(";", 2);
				Integer key = Integer.parseInt(split[0]);
				Integer value = Integer.parseInt(split[1]);
				AttributeVlalueTemp.put(key, value) ; 				
			}
			AttributeVlalue = com.google.common.collect.ImmutableMap.copyOf(AttributeVlalueTemp);
		}else{
			AttributeVlalue = java.util.Collections.emptyMap() ; 
		}
	}
	

}
