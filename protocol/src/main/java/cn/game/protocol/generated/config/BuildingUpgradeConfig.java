package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 建筑升级建造表
 * 
 * 工具生成的，不要手动修改
 */
 public class BuildingUpgradeConfig {

	/** id */
	private final int id;		
	/** 建筑表id */
	private final int buildingId;		
	/** 等级 */
	private final int level;		
	/** 属性类型 -- 固定值类型 类型读取属性表 格式=类型:数值| */
	private final List<Entry<Integer,Integer>> attribute;		
	/** 升级消耗 -- 升级消耗，1级时表示建造的消耗 */
	private final List<Entry<Integer,Integer>> cost;		

	public BuildingUpgradeConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.buildingId = Integer.parseInt(element.getAttribute("buildingId") == null || element.getAttribute("buildingId").length() == 0 ? "0"
			: element.getAttribute("buildingId")); // 建筑表id
		this.level = Integer.parseInt(element.getAttribute("level") == null || element.getAttribute("level").length() == 0 ? "0"
			: element.getAttribute("level")); // 等级
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
		String costString = element.getAttribute("cost"); // 升级消耗
		if (costString != null && costString.length() > 0) {
			String[] costStrings = costString.split("\\|"); 
			List<Entry<Integer,Integer>> cost = new ArrayList<Entry<Integer,Integer>>(costStrings.length) ; 
			for (int i = 0; i < costStrings.length; i++) {
			    String[] split = costStrings[i].split(":", 2);
				cost.add(new Entry<Integer,Integer>()	{
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

			this.cost = com.google.common.collect.ImmutableList.copyOf(cost);						
		} else {
			this.cost = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getBuildingId() {
		return buildingId;
	}
	
	public int getLevel() {
		return level;
	}
	
	public List<Entry<Integer,Integer>> getAttribute() {
		return attribute;
	}
	
	public List<Entry<Integer,Integer>> getCost() {
		return cost;
	}
	
}
