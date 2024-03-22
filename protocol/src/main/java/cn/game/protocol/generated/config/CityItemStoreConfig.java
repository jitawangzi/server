package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 主城道具库表
 * 
 * 工具生成的，不要手动修改
 */
 public class CityItemStoreConfig {

	/** id */
	private final int id;		
	/** 道具ID -- 读取item表ID */
	private final int itemId;		
	/** 最大库存数量 -- 商品上架的 最大数量，即商品上架的最大堆叠数量够 */
	private final int stockMax;		
	/** 解锁条件 -- 通常用主线进度解锁 */
	private final int condition;		
	/** 花费的资源id */
	private final int costGoodsId;		
	/** 花费的资源数量 */
	private final int costGoodsCount;		
	/** 补货周期 -- 多少天补货一次 */
	private final int cycle;		
	/** 补货数量 -- 一次补充多少个 */
	private final int cycleNumbers;		

	public CityItemStoreConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.itemId = Integer.parseInt(element.getAttribute("itemId") == null || element.getAttribute("itemId").length() == 0 ? "0"
			: element.getAttribute("itemId")); // 道具ID
		this.stockMax = Integer.parseInt(element.getAttribute("stockMax") == null || element.getAttribute("stockMax").length() == 0 ? "0"
			: element.getAttribute("stockMax")); // 最大库存数量
		this.condition = Integer.parseInt(element.getAttribute("condition") == null || element.getAttribute("condition").length() == 0 ? "0"
			: element.getAttribute("condition")); // 解锁条件
		this.costGoodsId = Integer.parseInt(element.getAttribute("costGoodsId") == null || element.getAttribute("costGoodsId").length() == 0 ? "0"
			: element.getAttribute("costGoodsId")); // 花费的资源id
		this.costGoodsCount = Integer.parseInt(element.getAttribute("costGoodsCount") == null || element.getAttribute("costGoodsCount").length() == 0 ? "0"
			: element.getAttribute("costGoodsCount")); // 花费的资源数量
		this.cycle = Integer.parseInt(element.getAttribute("cycle") == null || element.getAttribute("cycle").length() == 0 ? "0"
			: element.getAttribute("cycle")); // 补货周期
		this.cycleNumbers = Integer.parseInt(element.getAttribute("cycleNumbers") == null || element.getAttribute("cycleNumbers").length() == 0 ? "0"
			: element.getAttribute("cycleNumbers")); // 补货数量
	}
	
	public int getId() {
		return id;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public int getStockMax() {
		return stockMax;
	}
	
	public int getCondition() {
		return condition;
	}
	
	public int getCostGoodsId() {
		return costGoodsId;
	}
	
	public int getCostGoodsCount() {
		return costGoodsCount;
	}
	
	public int getCycle() {
		return cycle;
	}
	
	public int getCycleNumbers() {
		return cycleNumbers;
	}
	
}
