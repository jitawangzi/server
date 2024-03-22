package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 货币表
 * 
 * 工具生成的，不要手动修改
 */
 public class MoneyConfig {

	/** 物品ID */
	private final int ID;		
	/** 总类型 1-货币 2-物品 3-装备 4-铁哥们 5-好友 6-宠物 */
	private final int TotalType;		
	/** 物品类型 1-钻石 2-金币 */
	private final int ItemType;		
	/** 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一 */
	private final int Quality;		
	/** 物品名称 */
	private final String Name;		
	/** 物品tips */
	private final String Tips;		
	/** 图标Icon 文件名 */
	private final String Icon;		
	/** 货币有效期 1-天数     配置：1;7 2-具体失效时间，失效为当天23:59:59，失效即物品消失     配置：2;20240705  例如节日专用货币，到期直接全部删除 */
	private final int[] Period;		

	public MoneyConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 物品ID
		this.TotalType = Integer.parseInt(element.getAttribute("TotalType") == null || element.getAttribute("TotalType").length() == 0 ? "0"
			: element.getAttribute("TotalType")); // 总类型 1-货币 2-物品 3-装备 4-铁哥们 5-好友 6-宠物
		this.ItemType = Integer.parseInt(element.getAttribute("ItemType") == null || element.getAttribute("ItemType").length() == 0 ? "0"
			: element.getAttribute("ItemType")); // 物品类型 1-钻石 2-金币
		this.Quality = Integer.parseInt(element.getAttribute("Quality") == null || element.getAttribute("Quality").length() == 0 ? "0"
			: element.getAttribute("Quality")); // 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一
		this.Name = element.getAttribute("Name"); // 物品名称
		this.Tips = element.getAttribute("Tips"); // 物品tips
		this.Icon = element.getAttribute("Icon"); // 图标Icon 文件名
		String PeriodString = element.getAttribute("Period"); // 货币有效期 1-天数     配置：1;7 2-具体失效时间，失效为当天23:59:59，失效即物品消失     配置：2;20240705  例如节日专用货币，到期直接全部删除
		if (PeriodString != null && PeriodString.length() > 0) {
			String[] PeriodStrings = PeriodString.split(";"); 
			int[] Period = new int[PeriodStrings.length] ; 
			for (int i = 0; i < PeriodStrings.length; i++) {
				int temp = Integer.parseInt(PeriodStrings[i]);
				Period[i] = temp;
			}
			this.Period = Period ;			
		} else {
			this.Period = new int[] {};
		}
	}
	
	public int getID() {
		return ID;
	}
	
	public int getTotalType() {
		return TotalType;
	}
	
	public int getItemType() {
		return ItemType;
	}
	
	public int getQuality() {
		return Quality;
	}
	
	public String getName() {
		return Name;
	}
	
	public String getTips() {
		return Tips;
	}
	
	public String getIcon() {
		return Icon;
	}
	
	public int[] getPeriod() {
		return Period;
	}
	
}
