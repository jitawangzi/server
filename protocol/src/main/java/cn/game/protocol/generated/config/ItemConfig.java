package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 物品表
 * 
 * 工具生成的，不要手动修改
 */
 public class ItemConfig {

	/** 物品ID  物品2打头6位 前3位=总类型+0+物品类型 后3位是流水号 */
	public final int ID;		
	/** 总类型 1-货币 2-物品 3-装备 4-铁哥们 5-好友 6-宠物 */
	public final int TotalType;		
	/** 物品类型 1-英雄突破 2-英雄升级 3-请神道具 4-英雄任选道具 5-材料 6-挂机奖励道具 */
	public final int ItemType;		
	/** 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一 */
	public final int Quality;		
	/** 物品名称 */
	public final String Name;		
	/** 物品tips */
	public final String Tips;		
	/** 参数 物品类型为6时，参数：小时 物品类型为4时，参数：品质 */
	public final int Para;		
	/** 图标Icon 文件名  调用：west\src\First_party\art\xiyou UI\icon_图标 */
	public final String Icon;		
	/** 物品有效期 1-天数     配置：1;7 2-具体失效时间，失效为当天23:59:59，失效即物品消失     配置：2;20240705 */
	public final String[] Period;		

	public ItemConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 物品ID  物品2打头6位 前3位=总类型+0+物品类型 后3位是流水号
		TotalType = Integer.parseInt(element.getAttribute("TotalType") == null || element.getAttribute("TotalType").length() == 0 ? "0"
			: element.getAttribute("TotalType")); // 总类型 1-货币 2-物品 3-装备 4-铁哥们 5-好友 6-宠物
		ItemType = Integer.parseInt(element.getAttribute("ItemType") == null || element.getAttribute("ItemType").length() == 0 ? "0"
			: element.getAttribute("ItemType")); // 物品类型 1-英雄突破 2-英雄升级 3-请神道具 4-英雄任选道具 5-材料 6-挂机奖励道具
		Quality = Integer.parseInt(element.getAttribute("Quality") == null || element.getAttribute("Quality").length() == 0 ? "0"
			: element.getAttribute("Quality")); // 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一
		Name = element.getAttribute("Name"); // 物品名称
		Tips = element.getAttribute("Tips"); // 物品tips
		Para = Integer.parseInt(element.getAttribute("Para") == null || element.getAttribute("Para").length() == 0 ? "0"
			: element.getAttribute("Para")); // 参数 物品类型为6时，参数：小时 物品类型为4时，参数：品质
		Icon = element.getAttribute("Icon"); // 图标Icon 文件名  调用：west\src\First_party\art\xiyou UI\icon_图标
		String PeriodString = element.getAttribute("Period"); // 物品有效期 1-天数     配置：1;7 2-具体失效时间，失效为当天23:59:59，失效即物品消失     配置：2;20240705
		if (PeriodString != null && PeriodString.length() > 0) {
			String[] PeriodStrings = PeriodString.split(";"); 
			String[] PeriodTemp = new String[PeriodStrings.length] ; 
			for (int i = 0; i < PeriodStrings.length; i++) {
				String temp = PeriodStrings[i];	
				PeriodTemp[i] = temp;
			}
			Period = PeriodTemp ;			
		} else {
			Period = new String[] {};
		}
	}
	

}
