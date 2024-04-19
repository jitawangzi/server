package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 英雄武器
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroSwordConfig {

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
	/** 武器属性   属性id;属性数值 百分比属性/10000用 */
	public final int[] SwordValve;		
	/** 武器升星属性1次性加成   属性id;属性数值 百分比属性/10000用 */
	public final int[] SwordStarValve;		
	/** 武器升星 消耗id  调用：【Almost配置表_通用_消耗表】—【消耗表#Consume】中ID */
	public final int StarConsumeId;		
	/** 武器升星 消耗可替代道具及个数  道具id;道具个数 */
	public final int[] StarReplace;		

	public HeroSwordConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 物品ID 物品id6位=种类型1位+物品类型2位+品质1位+序列号2位
		this.TotalType = Integer.parseInt(element.getAttribute("TotalType") == null || element.getAttribute("TotalType").length() == 0 ? "0"
			: element.getAttribute("TotalType")); // 总类型 1-货币 2-物品 3-英雄 4-经验 5-能量 6-英雄武器 7-英雄装备 8-英雄时装 9-英雄宝石 10-龙
		this.ItemType = Integer.parseInt(element.getAttribute("ItemType") == null || element.getAttribute("ItemType").length() == 0 ? "0"
			: element.getAttribute("ItemType")); // 物品类型 1-默认英雄武器类型
		this.Quality = Integer.parseInt(element.getAttribute("Quality") == null || element.getAttribute("Quality").length() == 0 ? "0"
			: element.getAttribute("Quality")); // 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一
		this.Name = element.getAttribute("Name"); // 物品名称
		this.Tips = element.getAttribute("Tips"); // 物品tips
		this.Icon = element.getAttribute("Icon"); // 图标Icon 文件名
		this.AwardID = Integer.parseInt(element.getAttribute("AwardID") == null || element.getAttribute("AwardID").length() == 0 ? "0"
			: element.getAttribute("AwardID")); // 调用Awrard表中id
		String PeriodString = element.getAttribute("Period"); // 物品有效期 1-天数 配置：1;7 2-具体失效时间，失效为当天23:59:59，失效即物品消失 配置：2;20240705
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
		String SwordValveString = element.getAttribute("SwordValve"); // 武器属性   属性id;属性数值 百分比属性/10000用
		if (SwordValveString != null && SwordValveString.length() > 0) {
			String[] SwordValveStrings = SwordValveString.split(";"); 
			int[] SwordValve = new int[SwordValveStrings.length] ; 
			for (int i = 0; i < SwordValveStrings.length; i++) {
				int temp = Integer.parseInt(SwordValveStrings[i]);
				SwordValve[i] = temp;
			}
			this.SwordValve = SwordValve ;			
		} else {
			this.SwordValve = new int[] {};
		}
		String SwordStarValveString = element.getAttribute("SwordStarValve"); // 武器升星属性1次性加成   属性id;属性数值 百分比属性/10000用
		if (SwordStarValveString != null && SwordStarValveString.length() > 0) {
			String[] SwordStarValveStrings = SwordStarValveString.split(";"); 
			int[] SwordStarValve = new int[SwordStarValveStrings.length] ; 
			for (int i = 0; i < SwordStarValveStrings.length; i++) {
				int temp = Integer.parseInt(SwordStarValveStrings[i]);
				SwordStarValve[i] = temp;
			}
			this.SwordStarValve = SwordStarValve ;			
		} else {
			this.SwordStarValve = new int[] {};
		}
		this.StarConsumeId = Integer.parseInt(element.getAttribute("StarConsumeId") == null || element.getAttribute("StarConsumeId").length() == 0 ? "0"
			: element.getAttribute("StarConsumeId")); // 武器升星 消耗id  调用：【Almost配置表_通用_消耗表】—【消耗表#Consume】中ID
		String StarReplaceString = element.getAttribute("StarReplace"); // 武器升星 消耗可替代道具及个数  道具id;道具个数
		if (StarReplaceString != null && StarReplaceString.length() > 0) {
			String[] StarReplaceStrings = StarReplaceString.split(";"); 
			int[] StarReplace = new int[StarReplaceStrings.length] ; 
			for (int i = 0; i < StarReplaceStrings.length; i++) {
				int temp = Integer.parseInt(StarReplaceStrings[i]);
				StarReplace[i] = temp;
			}
			this.StarReplace = StarReplace ;			
		} else {
			this.StarReplace = new int[] {};
		}
	}
	

}
