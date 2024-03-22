package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 核心套装效果
 * 
 * 工具生成的，不要手动修改
 */
 public class CoreSuitEffectConfig {

	/** 套装id */
	private final int id;		
	/** 效果名称 */
	private final String name;		
	/** 套装属性 -- 属性id|属性值 */
	private final List<Entry<Integer,Integer>> SuitAttribute;		
	/** 套装特效 */
	private final int SuitEffect;		

	public CoreSuitEffectConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 套装id
		this.name = element.getAttribute("name"); // 效果名称
		String SuitAttributeString = element.getAttribute("SuitAttribute"); // 套装属性
		if (SuitAttributeString != null && SuitAttributeString.length() > 0) {
			String[] SuitAttributeStrings = SuitAttributeString.split("\\|"); 
			List<Entry<Integer,Integer>> SuitAttribute = new ArrayList<Entry<Integer,Integer>>(SuitAttributeStrings.length) ; 
			for (int i = 0; i < SuitAttributeStrings.length; i++) {
			    String[] split = SuitAttributeStrings[i].split(":", 2);
				SuitAttribute.add(new Entry<Integer,Integer>()	{
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

			this.SuitAttribute = com.google.common.collect.ImmutableList.copyOf(SuitAttribute);						
		} else {
			this.SuitAttribute = java.util.Collections.emptyList();
		}
		this.SuitEffect = Integer.parseInt(element.getAttribute("SuitEffect") == null || element.getAttribute("SuitEffect").length() == 0 ? "0"
			: element.getAttribute("SuitEffect")); // 套装特效
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public List<Entry<Integer,Integer>> getSuitAttribute() {
		return SuitAttribute;
	}
	
	public int getSuitEffect() {
		return SuitEffect;
	}
	
}
