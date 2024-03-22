package cn.game.protocol.generated.config;

import cn.game.util.Weightable;
import org.w3c.dom.Element;

/**
 * 主城军械库表
 * 
 * 工具生成的，不要手动修改
 */
 public class CityEquipmentStoreConfig implements Weightable {

	/** id -- id */
	private final int id;		
	/** 页签类型 -- 1-购买商店 2-兑换商店 */
	private final int tabType;		
	/** 装备ID -- 读取装备表 */
	private final int equipmentId;		
	/** 花费的资源id */
	private final int costGoodsId;		
	/** 花费的资源数量 */
	private final int costGoodsCount;		
	/** 解锁条件 -- 通常用主线进度解锁 */
	private final int condition;		
	/** 随机权重 */
	private final int weight;		
	/** 等级 -- 代表装备的强度 */
	private final int level;		

	public CityEquipmentStoreConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.tabType = Integer.parseInt(element.getAttribute("tabType") == null || element.getAttribute("tabType").length() == 0 ? "0"
			: element.getAttribute("tabType")); // 页签类型
		this.equipmentId = Integer.parseInt(element.getAttribute("equipmentId") == null || element.getAttribute("equipmentId").length() == 0 ? "0"
			: element.getAttribute("equipmentId")); // 装备ID
		this.costGoodsId = Integer.parseInt(element.getAttribute("costGoodsId") == null || element.getAttribute("costGoodsId").length() == 0 ? "0"
			: element.getAttribute("costGoodsId")); // 花费的资源id
		this.costGoodsCount = Integer.parseInt(element.getAttribute("costGoodsCount") == null || element.getAttribute("costGoodsCount").length() == 0 ? "0"
			: element.getAttribute("costGoodsCount")); // 花费的资源数量
		this.condition = Integer.parseInt(element.getAttribute("condition") == null || element.getAttribute("condition").length() == 0 ? "0"
			: element.getAttribute("condition")); // 解锁条件
		this.weight = Integer.parseInt(element.getAttribute("weight") == null || element.getAttribute("weight").length() == 0 ? "0"
			: element.getAttribute("weight")); // 随机权重
		this.level = Integer.parseInt(element.getAttribute("level") == null || element.getAttribute("level").length() == 0 ? "0"
			: element.getAttribute("level")); // 等级
	}
	
	public int getId() {
		return id;
	}
	
	public int getTabType() {
		return tabType;
	}
	
	public int getEquipmentId() {
		return equipmentId;
	}
	
	public int getCostGoodsId() {
		return costGoodsId;
	}
	
	public int getCostGoodsCount() {
		return costGoodsCount;
	}
	
	public int getCondition() {
		return condition;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public int getLevel() {
		return level;
	}
	
	@Override
	public int weight() {
		return this.weight;
	}
}
