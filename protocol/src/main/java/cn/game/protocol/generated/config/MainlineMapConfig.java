package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 主线地图表
 * 
 * 工具生成的，不要手动修改
 */
 public class MainlineMapConfig {

	/** id -- id */
	private final int id;		
	/** 地区id -- 地区id */
	private final int areaId;		
	/** 类型 -- 类型 */
	private final int type;		
	/** 名称 -- 名称 */
	private final String name;		
	/** 图标 -- 图标 */
	private final String icon;		
	/** 是否可传送 -- 传送 */
	private final boolean transmit;		
	/** 场景资源 -- 场景资源 */
	private final String resource;		
	/** 解锁等级 -- 解锁等级 */
	private final int level;		
	/** 解锁条件 -- 解锁条件 */
	private final List<Integer> condition;		
	/** 刷新概率 -- 精英怪概率 */
	private final int refresh;		
	/** 普通怪物数量 -- min:max 从区间中随机一个数量 */
	private final List<Integer> norMonsterNumber;		

	public MainlineMapConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.areaId = Integer.parseInt(element.getAttribute("areaId") == null || element.getAttribute("areaId").length() == 0 ? "0"
			: element.getAttribute("areaId")); // 地区id
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 类型
		this.name = element.getAttribute("name"); // 名称
		this.icon = element.getAttribute("icon"); // 图标
		this.transmit = Boolean.parseBoolean(element.getAttribute("transmit") == null || element.getAttribute("transmit").length() == 0 ? "false"
			: element.getAttribute("transmit")); // 是否可传送
		this.resource = element.getAttribute("resource"); // 场景资源
		this.level = Integer.parseInt(element.getAttribute("level") == null || element.getAttribute("level").length() == 0 ? "0"
			: element.getAttribute("level")); // 解锁等级
		String conditionString = element.getAttribute("condition"); // 解锁条件
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
		this.refresh = Integer.parseInt(element.getAttribute("refresh") == null || element.getAttribute("refresh").length() == 0 ? "0"
			: element.getAttribute("refresh")); // 刷新概率
		String norMonsterNumberString = element.getAttribute("norMonsterNumber"); // 普通怪物数量
		if (norMonsterNumberString != null && norMonsterNumberString.length() > 0) {
			String[] norMonsterNumberStrings = norMonsterNumberString.split("\\|"); 
			List<Integer> norMonsterNumber = new ArrayList<Integer>(norMonsterNumberStrings.length) ; 
			for (int i = 0; i < norMonsterNumberStrings.length; i++) {
				Integer temp = Integer.parseInt(norMonsterNumberStrings[i]);
				norMonsterNumber.add(temp);
			}
			this.norMonsterNumber = com.google.common.collect.ImmutableList.copyOf(norMonsterNumber);						
		} else {
			this.norMonsterNumber = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getAreaId() {
		return areaId;
	}
	
	public int getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public boolean getTransmit() {
		return transmit;
	}
	
	public String getResource() {
		return resource;
	}
	
	public int getLevel() {
		return level;
	}
	
	public List<Integer> getCondition() {
		return condition;
	}
	
	public int getRefresh() {
		return refresh;
	}
	
	public List<Integer> getNorMonsterNumber() {
		return norMonsterNumber;
	}
	
}
