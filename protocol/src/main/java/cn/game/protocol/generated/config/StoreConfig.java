package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 商店
 * 
 * 工具生成的，不要手动修改
 */
 public class StoreConfig {

	/** 商品id -- 商品id */
	private final int id;		
	/** 一级页签类型 -- 页签分类 */
	private final int firstTabType;		
	/** 二级分类 -- 页签分类 */
	private final int tabType;		
	/** 物品id -- 物品id */
	private final int itemId;		
	/** 货币类型 -- 货币类型 */
	private final int itemUnitPrice;		
	/** 数量 -- 数量 */
	private final int number;		
	/** 折扣 -- 折扣 */
	private final float discount;		
	/** 总价 -- 总价 */
	private final int totalPrcie;		
	/** 限制参数1 -- 限制参数1 */
	private final int limitParameters1;		
	/** 限制参数2 -- 限制参数2 */
	private final int limitParameters2;		
	/** 是否上架 -- 是否上架 */
	private final boolean upDown;		

	public StoreConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 商品id
		this.firstTabType = Integer.parseInt(element.getAttribute("firstTabType") == null || element.getAttribute("firstTabType").length() == 0 ? "0"
			: element.getAttribute("firstTabType")); // 一级页签类型
		this.tabType = Integer.parseInt(element.getAttribute("tabType") == null || element.getAttribute("tabType").length() == 0 ? "0"
			: element.getAttribute("tabType")); // 二级分类
		this.itemId = Integer.parseInt(element.getAttribute("itemId") == null || element.getAttribute("itemId").length() == 0 ? "0"
			: element.getAttribute("itemId")); // 物品id
		this.itemUnitPrice = Integer.parseInt(element.getAttribute("itemUnitPrice") == null || element.getAttribute("itemUnitPrice").length() == 0 ? "0"
			: element.getAttribute("itemUnitPrice")); // 货币类型
		this.number = Integer.parseInt(element.getAttribute("number") == null || element.getAttribute("number").length() == 0 ? "0"
			: element.getAttribute("number")); // 数量
		this.discount = Float.parseFloat(element.getAttribute("discount") == null || element.getAttribute("discount").length() == 0 ? "0"
			: element.getAttribute("discount")); // 折扣
		this.totalPrcie = Integer.parseInt(element.getAttribute("totalPrcie") == null || element.getAttribute("totalPrcie").length() == 0 ? "0"
			: element.getAttribute("totalPrcie")); // 总价
		this.limitParameters1 = Integer.parseInt(element.getAttribute("limitParameters1") == null || element.getAttribute("limitParameters1").length() == 0 ? "0"
			: element.getAttribute("limitParameters1")); // 限制参数1
		this.limitParameters2 = Integer.parseInt(element.getAttribute("limitParameters2") == null || element.getAttribute("limitParameters2").length() == 0 ? "0"
			: element.getAttribute("limitParameters2")); // 限制参数2
		this.upDown = Boolean.parseBoolean(element.getAttribute("upDown") == null || element.getAttribute("upDown").length() == 0 ? "false"
			: element.getAttribute("upDown")); // 是否上架
	}
	
	public int getId() {
		return id;
	}
	
	public int getFirstTabType() {
		return firstTabType;
	}
	
	public int getTabType() {
		return tabType;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public int getItemUnitPrice() {
		return itemUnitPrice;
	}
	
	public int getNumber() {
		return number;
	}
	
	public float getDiscount() {
		return discount;
	}
	
	public int getTotalPrcie() {
		return totalPrcie;
	}
	
	public int getLimitParameters1() {
		return limitParameters1;
	}
	
	public int getLimitParameters2() {
		return limitParameters2;
	}
	
	public boolean getUpDown() {
		return upDown;
	}
	
}
