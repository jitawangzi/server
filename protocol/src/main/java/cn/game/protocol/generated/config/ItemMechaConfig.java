package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 机甲道具
 * 
 * 工具生成的，不要手动修改
 */
 public class ItemMechaConfig {

	/** id -- id */
	private final int id;		
	/** 阵营限制 -- 1-昨日联盟 2-观察者 3-通天塔 4-深水重工 5-边缘者 6-自由联盟 7-未来人 8-克苏鲁 0-通用 */
	private final int CampLimit;		
	/** 升级成长属性 */
	private final List<Entry<Integer,Integer>> attribute;		
	/** 每级总属性 */
	private final List<Entry<Integer,Integer>> TotalAttribute;		

	public ItemMechaConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.CampLimit = Integer.parseInt(element.getAttribute("CampLimit") == null || element.getAttribute("CampLimit").length() == 0 ? "0"
			: element.getAttribute("CampLimit")); // 阵营限制
		String attributeString = element.getAttribute("attribute"); // 升级成长属性
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
		String TotalAttributeString = element.getAttribute("TotalAttribute"); // 每级总属性
		if (TotalAttributeString != null && TotalAttributeString.length() > 0) {
			String[] TotalAttributeStrings = TotalAttributeString.split("\\|"); 
			List<Entry<Integer,Integer>> TotalAttribute = new ArrayList<Entry<Integer,Integer>>(TotalAttributeStrings.length) ; 
			for (int i = 0; i < TotalAttributeStrings.length; i++) {
			    String[] split = TotalAttributeStrings[i].split(":", 2);
				TotalAttribute.add(new Entry<Integer,Integer>()	{
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

			this.TotalAttribute = com.google.common.collect.ImmutableList.copyOf(TotalAttribute);						
		} else {
			this.TotalAttribute = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getCampLimit() {
		return CampLimit;
	}
	
	public List<Entry<Integer,Integer>> getAttribute() {
		return attribute;
	}
	
	public List<Entry<Integer,Integer>> getTotalAttribute() {
		return TotalAttribute;
	}
	
}
