package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 英雄时装
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroFashionConfig {

	/** 物品ID 物品id6位=种类型1位+物品类型2位+品质1位+序列号2位 */
	public final int ID;		
	/** 总类型 1-货币 2-物品 3-英雄 4-经验 5-能量 6-英雄武器 7-英雄装备 8-英雄时装 9-英雄宝石 10-龙 */
	public final int TotalType;		
	/** 物品类型 1-默认英雄武器类型 */
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
	/** 物品有效期 1-天数 配置：1;7 2-具体失效时间，失效为当天23:59:59，失效即物品消失 配置：2;20240705 */
	public final int[] Period;		
	/** 时装属性   属性id;属性数值  百分比属性/10000用 */
	public final int[][] FashionValve;		
	/** 时装升星属性1次性加成   属性id;属性数值  百分比属性/10000用 */
	public final int[] FashionStarValve;		
	/** 时装升星 消耗id  调用：【Almost配置表_通用_消耗表】—【消耗表#Consume】中ID */
	public final int FashionstarConsumeId;		
	/** 时装升星 消耗可替代道具及个数  道具id;道具个数 */
	public final int[] FashionStarReplace;		

	public HeroFashionConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 物品ID 物品id6位=种类型1位+物品类型2位+品质1位+序列号2位
		TotalType = Integer.parseInt(element.getAttribute("TotalType") == null || element.getAttribute("TotalType").length() == 0 ? "0"
			: element.getAttribute("TotalType")); // 总类型 1-货币 2-物品 3-英雄 4-经验 5-能量 6-英雄武器 7-英雄装备 8-英雄时装 9-英雄宝石 10-龙
		ItemType = Integer.parseInt(element.getAttribute("ItemType") == null || element.getAttribute("ItemType").length() == 0 ? "0"
			: element.getAttribute("ItemType")); // 物品类型 1-默认英雄武器类型
		Quality = Integer.parseInt(element.getAttribute("Quality") == null || element.getAttribute("Quality").length() == 0 ? "0"
			: element.getAttribute("Quality")); // 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一
		Name = element.getAttribute("Name"); // 物品名称
		Tips = element.getAttribute("Tips"); // 物品tips
		Icon = element.getAttribute("Icon"); // 图标Icon 文件名
		AwardID = Integer.parseInt(element.getAttribute("AwardID") == null || element.getAttribute("AwardID").length() == 0 ? "0"
			: element.getAttribute("AwardID")); // 调用Awrard表中id
		String PeriodString = element.getAttribute("Period"); // 物品有效期 1-天数 配置：1;7 2-具体失效时间，失效为当天23:59:59，失效即物品消失 配置：2;20240705
		if (PeriodString != null && PeriodString.length() > 0) {
			String[] PeriodStrings = PeriodString.split(";"); 
			int[] PeriodTemp = new int[PeriodStrings.length] ; 
			for (int i = 0; i < PeriodStrings.length; i++) {
				int temp = Integer.parseInt(PeriodStrings[i]);	
				PeriodTemp[i] = temp;
			}
			Period = PeriodTemp ;			
		} else {
			Period = new int[] {};
		}
		String FashionValveString = element.getAttribute("FashionValve"); // 时装属性   属性id;属性数值  百分比属性/10000用
		if (FashionValveString != null && FashionValveString.length() > 0) {
			String[] FashionValveStrings = FashionValveString.split("\\|"); 
			int[][] FashionValveTemp = new int[FashionValveStrings.length][] ; 
			for (int i = 0; i < FashionValveStrings.length; i++) {
				String[] FashionValveStrings2 = FashionValveStrings[i].split(";"); 
				int[] array = new int[FashionValveStrings2.length];
				for (int j = 0; j < FashionValveStrings2.length; j++) {
					int temp = Integer.parseInt(FashionValveStrings2[j]);	
					array[j] = temp;
				}
				FashionValveTemp[i] = array;
			}
			FashionValve = FashionValveTemp ;			
		} else {
			FashionValve = new int[][] {};
		}
		String FashionStarValveString = element.getAttribute("FashionStarValve"); // 时装升星属性1次性加成   属性id;属性数值  百分比属性/10000用
		if (FashionStarValveString != null && FashionStarValveString.length() > 0) {
			String[] FashionStarValveStrings = FashionStarValveString.split(";"); 
			int[] FashionStarValveTemp = new int[FashionStarValveStrings.length] ; 
			for (int i = 0; i < FashionStarValveStrings.length; i++) {
				int temp = Integer.parseInt(FashionStarValveStrings[i]);	
				FashionStarValveTemp[i] = temp;
			}
			FashionStarValve = FashionStarValveTemp ;			
		} else {
			FashionStarValve = new int[] {};
		}
		FashionstarConsumeId = Integer.parseInt(element.getAttribute("FashionstarConsumeId") == null || element.getAttribute("FashionstarConsumeId").length() == 0 ? "0"
			: element.getAttribute("FashionstarConsumeId")); // 时装升星 消耗id  调用：【Almost配置表_通用_消耗表】—【消耗表#Consume】中ID
		String FashionStarReplaceString = element.getAttribute("FashionStarReplace"); // 时装升星 消耗可替代道具及个数  道具id;道具个数
		if (FashionStarReplaceString != null && FashionStarReplaceString.length() > 0) {
			String[] FashionStarReplaceStrings = FashionStarReplaceString.split(";"); 
			int[] FashionStarReplaceTemp = new int[FashionStarReplaceStrings.length] ; 
			for (int i = 0; i < FashionStarReplaceStrings.length; i++) {
				int temp = Integer.parseInt(FashionStarReplaceStrings[i]);	
				FashionStarReplaceTemp[i] = temp;
			}
			FashionStarReplace = FashionStarReplaceTemp ;			
		} else {
			FashionStarReplace = new int[] {};
		}
	}
	

}
