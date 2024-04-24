package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 物品表
 * 
 * 工具生成的，不要手动修改
 */
 public class ItemConfig {

	/** 物品ID 物品id6位=种类型1位+物品类型2位+品质1位+序列号2位 */
	public final int ID;		
	/** 总类型 1-货币 2-物品 3-装备 4-铁哥们 5-好友 6-宠物 */
	public final int TotalType;		
	/** 物品类型 1-英雄突破 2-英雄升级 3-扭蛋道具 4-英雄任选道具 5-货币类的集合 */
	public final int ItemType;		
	/** 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一 */
	public final int Quality;		
	/** 物品名称 */
	public final String Name;		
	/** 物品tips */
	public final String Tips;		
	/** 图标Icon 文件名 */
	public final String Icon;		
	/** 调用Awrard表中id */
	public final int AwardID;		
	/** 物品有效期 1-天数     配置：1;7 2-具体失效时间，失效为当天23:59:59，失效即物品消失     配置：2;20240705 */
	public final String[] Period;		

	public ItemConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 物品ID 物品id6位=种类型1位+物品类型2位+品质1位+序列号2位
		TotalType = Integer.parseInt(element.getAttribute("TotalType") == null || element.getAttribute("TotalType").length() == 0 ? "0"
			: element.getAttribute("TotalType")); // 总类型 1-货币 2-物品 3-装备 4-铁哥们 5-好友 6-宠物
		ItemType = Integer.parseInt(element.getAttribute("ItemType") == null || element.getAttribute("ItemType").length() == 0 ? "0"
			: element.getAttribute("ItemType")); // 物品类型 1-英雄突破 2-英雄升级 3-扭蛋道具 4-英雄任选道具 5-货币类的集合
		Quality = Integer.parseInt(element.getAttribute("Quality") == null || element.getAttribute("Quality").length() == 0 ? "0"
			: element.getAttribute("Quality")); // 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一
		Name = element.getAttribute("Name"); // 物品名称
		Tips = element.getAttribute("Tips"); // 物品tips
		Icon = element.getAttribute("Icon"); // 图标Icon 文件名
		AwardID = Integer.parseInt(element.getAttribute("AwardID") == null || element.getAttribute("AwardID").length() == 0 ? "0"
			: element.getAttribute("AwardID")); // 调用Awrard表中id
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
