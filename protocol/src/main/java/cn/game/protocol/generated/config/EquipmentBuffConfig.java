package cn.game.protocol.generated.config;

import cn.game.util.Weightable;
import org.w3c.dom.Element;

/**
 * 装备buff表
 * 
 * 工具生成的，不要手动修改
 */
 public class EquipmentBuffConfig implements Weightable {

	/** 效果id */
	private final int id;		
	/** 词缀组 -- 1-子库1 2-子库2 3-子库3 。。。 */
	private final int group;		
	/** 稀有度限制 -- 1-白色(0个) 2-绿色(1个) 3-蓝色(2个) 4-紫色(3个) 5-橙色(4个) */
	private final int qualityLimit;		
	/** 权重 */
	private final int weight;		
	/** Buff表 */
	private final int buff;		
	/** 是否是传说效果 */
	private final boolean isLegend;		

	public EquipmentBuffConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 效果id
		this.group = Integer.parseInt(element.getAttribute("group") == null || element.getAttribute("group").length() == 0 ? "0"
			: element.getAttribute("group")); // 词缀组
		this.qualityLimit = Integer.parseInt(element.getAttribute("qualityLimit") == null || element.getAttribute("qualityLimit").length() == 0 ? "0"
			: element.getAttribute("qualityLimit")); // 稀有度限制
		this.weight = Integer.parseInt(element.getAttribute("weight") == null || element.getAttribute("weight").length() == 0 ? "0"
			: element.getAttribute("weight")); // 权重
		this.buff = Integer.parseInt(element.getAttribute("buff") == null || element.getAttribute("buff").length() == 0 ? "0"
			: element.getAttribute("buff")); // Buff表
		this.isLegend = Boolean.parseBoolean(element.getAttribute("isLegend") == null || element.getAttribute("isLegend").length() == 0 ? "false"
			: element.getAttribute("isLegend")); // 是否是传说效果
	}
	
	public int getId() {
		return id;
	}
	
	public int getGroup() {
		return group;
	}
	
	public int getQualityLimit() {
		return qualityLimit;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public int getBuff() {
		return buff;
	}
	
	public boolean getIsLegend() {
		return isLegend;
	}
	
	@Override
	public int weight() {
		return this.weight;
	}
}
