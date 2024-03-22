package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 芯片属性
 * 
 * 工具生成的，不要手动修改
 */
 public class ChipAttributeConfig {

	/** id */
	private final int id;		
	/** 组索引 -- 芯片颜色*100+稀有度*10+芯片类型 */
	private final int index;		
	/** 芯片属性 -- 属性类型:初始值:成长值|属性类型:初始值:成长值 */
	private final List<List<Integer>> chipAttribute;		
	/** 词缀属性类型 */
	private final int affixType;		
	/** 词缀属性 -- 随机下限:随机上限 */
	private final List<Entry<Integer,Integer>> affixAttribute;		
	/** 是否唯一 */
	private final int unique;		

	public ChipAttributeConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.index = Integer.parseInt(element.getAttribute("index") == null || element.getAttribute("index").length() == 0 ? "0"
			: element.getAttribute("index")); // 组索引
		String chipAttributeString = element.getAttribute("chipAttribute"); // 芯片属性
		if (chipAttributeString != null && chipAttributeString.length() > 0) {
			String[] chipAttributeStrings = chipAttributeString.split("\\|"); 
			List<List<Integer>> chipAttribute = new ArrayList<List<Integer>>(chipAttributeStrings.length) ; 
			for (int i = 0; i < chipAttributeStrings.length; i++) {
				String[] chipAttributeStrings2 = chipAttributeStrings[i].split(":"); 
				List<Integer> list = new ArrayList<Integer>(chipAttributeStrings2.length) ; 
				for (int j = 0; j < chipAttributeStrings2.length; j++) {
					Integer temp = Integer.parseInt(chipAttributeStrings2[j]);
					list.add(temp) ; 
				}
				chipAttribute.add(list);
			}
			this.chipAttribute = com.google.common.collect.ImmutableList.copyOf(chipAttribute);						
		} else {
			this.chipAttribute = java.util.Collections.emptyList();
		}
		this.affixType = Integer.parseInt(element.getAttribute("affixType") == null || element.getAttribute("affixType").length() == 0 ? "0"
			: element.getAttribute("affixType")); // 词缀属性类型
		String affixAttributeString = element.getAttribute("affixAttribute"); // 词缀属性
		if (affixAttributeString != null && affixAttributeString.length() > 0) {
			String[] affixAttributeStrings = affixAttributeString.split("\\|"); 
			List<Entry<Integer,Integer>> affixAttribute = new ArrayList<Entry<Integer,Integer>>(affixAttributeStrings.length) ; 
			for (int i = 0; i < affixAttributeStrings.length; i++) {
			    String[] split = affixAttributeStrings[i].split(":", 2);
				affixAttribute.add(new Entry<Integer,Integer>()	{
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

			this.affixAttribute = com.google.common.collect.ImmutableList.copyOf(affixAttribute);						
		} else {
			this.affixAttribute = java.util.Collections.emptyList();
		}
		this.unique = Integer.parseInt(element.getAttribute("unique") == null || element.getAttribute("unique").length() == 0 ? "0"
			: element.getAttribute("unique")); // 是否唯一
	}
	
	public int getId() {
		return id;
	}
	
	public int getIndex() {
		return index;
	}
	
	public List<List<Integer>> getChipAttribute() {
		return chipAttribute;
	}
	
	public int getAffixType() {
		return affixType;
	}
	
	public List<Entry<Integer,Integer>> getAffixAttribute() {
		return affixAttribute;
	}
	
	public int getUnique() {
		return unique;
	}
	
}
