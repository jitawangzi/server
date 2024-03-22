package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 建筑表
 * 
 * 工具生成的，不要手动修改
 */
 public class BuildingConfig {

	/** id -- id */
	private final int id;		
	/** 建筑名称 */
	private final String name;		
	/** 建筑类型 -- 1-基础建筑 2-高级建筑 3-装饰建筑 4-背景和天空 5-近景 6-地面 */
	private final int type;		
	/** 建筑枚举 */
	private final int featureType;		
	/** 是否默认 -- 1-默认 0-不默认 */
	private final boolean ifDefault;		
	/** 固定位置 -- 获得这个建筑时，默认生成的位置 */
	private final int defaultLocation;		
	/** 最大建造数量 -- 建筑的最大建造数量 */
	private final int buildingNumbersMax;		
	/** 所属层 -- 当前建筑可放置的层数 1) 近景：装饰； 2) 第一层地面：用于放置建筑； 3) 第二层地面：用于放置建筑； 4) 远景和天空：装饰； 5）地面：装饰 */
	private final List<Integer> floor;		
	/** 阵营 -- 1-马戏团 2-深水重工 3-昨日联盟 4-群星乐园 5-风铃群落 6-伊娜教会 7-七海神社 */
	private final int camp;		
	/** 条件 */
	private final int condition;		

	public BuildingConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 建筑名称
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 建筑类型
		this.featureType = Integer.parseInt(element.getAttribute("featureType") == null || element.getAttribute("featureType").length() == 0 ? "0"
			: element.getAttribute("featureType")); // 建筑枚举
		this.ifDefault = Boolean.parseBoolean(element.getAttribute("ifDefault") == null || element.getAttribute("ifDefault").length() == 0 ? "false"
			: element.getAttribute("ifDefault")); // 是否默认
		this.defaultLocation = Integer.parseInt(element.getAttribute("defaultLocation") == null || element.getAttribute("defaultLocation").length() == 0 ? "0"
			: element.getAttribute("defaultLocation")); // 固定位置
		this.buildingNumbersMax = Integer.parseInt(element.getAttribute("buildingNumbersMax") == null || element.getAttribute("buildingNumbersMax").length() == 0 ? "0"
			: element.getAttribute("buildingNumbersMax")); // 最大建造数量
		String floorString = element.getAttribute("floor"); // 所属层
		if (floorString != null && floorString.length() > 0) {
			String[] floorStrings = floorString.split("\\|"); 
			List<Integer> floor = new ArrayList<Integer>(floorStrings.length) ; 
			for (int i = 0; i < floorStrings.length; i++) {
				Integer temp = Integer.parseInt(floorStrings[i]);
				floor.add(temp);
			}
			this.floor = com.google.common.collect.ImmutableList.copyOf(floor);						
		} else {
			this.floor = java.util.Collections.emptyList();
		}
		this.camp = Integer.parseInt(element.getAttribute("camp") == null || element.getAttribute("camp").length() == 0 ? "0"
			: element.getAttribute("camp")); // 阵营
		this.condition = Integer.parseInt(element.getAttribute("condition") == null || element.getAttribute("condition").length() == 0 ? "0"
			: element.getAttribute("condition")); // 条件
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getType() {
		return type;
	}
	
	public int getFeatureType() {
		return featureType;
	}
	
	public boolean getIfDefault() {
		return ifDefault;
	}
	
	public int getDefaultLocation() {
		return defaultLocation;
	}
	
	public int getBuildingNumbersMax() {
		return buildingNumbersMax;
	}
	
	public List<Integer> getFloor() {
		return floor;
	}
	
	public int getCamp() {
		return camp;
	}
	
	public int getCondition() {
		return condition;
	}
	
}
