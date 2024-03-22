package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 装备强化消耗表
 * 
 * 工具生成的，不要手动修改
 */
 public class EquipmentStrengthenCostConfig {

	/** id -- id */
	private final int id;		
	/** 固有装备升级需要资源 -- 升级到当前等级需要的资源数量 */
	private final List<Entry<Integer,Integer>> standardExp;		
	/** 稀有度 -- 当前等级的稀有度 1-白 2-绿 3-蓝 4-紫 5-橙 */
	private final int quality;		

	public EquipmentStrengthenCostConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String standardExpString = element.getAttribute("standardExp"); // 固有装备升级需要资源
		if (standardExpString != null && standardExpString.length() > 0) {
			String[] standardExpStrings = standardExpString.split("\\|"); 
			List<Entry<Integer,Integer>> standardExp = new ArrayList<Entry<Integer,Integer>>(standardExpStrings.length) ; 
			for (int i = 0; i < standardExpStrings.length; i++) {
			    String[] split = standardExpStrings[i].split(":", 2);
				standardExp.add(new Entry<Integer,Integer>()	{
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

			this.standardExp = com.google.common.collect.ImmutableList.copyOf(standardExp);						
		} else {
			this.standardExp = java.util.Collections.emptyList();
		}
		this.quality = Integer.parseInt(element.getAttribute("quality") == null || element.getAttribute("quality").length() == 0 ? "0"
			: element.getAttribute("quality")); // 稀有度
	}
	
	public int getId() {
		return id;
	}
	
	public List<Entry<Integer,Integer>> getStandardExp() {
		return standardExp;
	}
	
	public int getQuality() {
		return quality;
	}
	
}
