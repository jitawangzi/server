package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 龙
 * 
 * 工具生成的，不要手动修改
 */
 public class DragonConfig {

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
	/** 龙属性   属性id;激活获得属性数值;升1星增幅 百分比属性/10000用  416爆炸伤害% +116全部元素伤害% 418精神伤害% +116全部元素伤害% 420中毒伤害% +116全部元素伤害%  最多升5星 */
	public final int[][] DragonStarValve;		
	/** 龙升星 消耗id  调用：【Almost配置表_通用_消耗表】—【消耗表#Consume】中ID */
	public final int DragonConsumeId;		
	/** 升星 消耗可替代道具及个数  道具id;道具个数 */
	public final int[] DragonStarReplace;		
	/** 龙获得后 自动战中学会龙技能  调用技能id=Almost配置表_战斗_Skill——HeroSkill#技能 */
	public final int DragonConsumeSkillId;		
	/** 龙资源id  调用ArtResource表 */
	public final String DragonArtResourceID;		

	public DragonConfig (Element element) throws Exception {
	
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
		String DragonStarValveString = element.getAttribute("DragonStarValve"); // 龙属性   属性id;激活获得属性数值;升1星增幅 百分比属性/10000用  416爆炸伤害% +116全部元素伤害% 418精神伤害% +116全部元素伤害% 420中毒伤害% +116全部元素伤害%  最多升5星
		if (DragonStarValveString != null && DragonStarValveString.length() > 0) {
			String[] DragonStarValveStrings = DragonStarValveString.split("\\|"); 
			int[][] DragonStarValveTemp = new int[DragonStarValveStrings.length][] ; 
			for (int i = 0; i < DragonStarValveStrings.length; i++) {
				String[] DragonStarValveStrings2 = DragonStarValveStrings[i].split(";"); 
				int[] array = new int[DragonStarValveStrings2.length];
				for (int j = 0; j < DragonStarValveStrings2.length; j++) {
					int temp = Integer.parseInt(DragonStarValveStrings2[j]);	
					array[j] = temp;
				}
				DragonStarValveTemp[i] = array;
			}
			DragonStarValve = DragonStarValveTemp ;			
		} else {
			DragonStarValve = new int[][] {};
		}
		DragonConsumeId = Integer.parseInt(element.getAttribute("DragonConsumeId") == null || element.getAttribute("DragonConsumeId").length() == 0 ? "0"
			: element.getAttribute("DragonConsumeId")); // 龙升星 消耗id  调用：【Almost配置表_通用_消耗表】—【消耗表#Consume】中ID
		String DragonStarReplaceString = element.getAttribute("DragonStarReplace"); // 升星 消耗可替代道具及个数  道具id;道具个数
		if (DragonStarReplaceString != null && DragonStarReplaceString.length() > 0) {
			String[] DragonStarReplaceStrings = DragonStarReplaceString.split(";"); 
			int[] DragonStarReplaceTemp = new int[DragonStarReplaceStrings.length] ; 
			for (int i = 0; i < DragonStarReplaceStrings.length; i++) {
				int temp = Integer.parseInt(DragonStarReplaceStrings[i]);	
				DragonStarReplaceTemp[i] = temp;
			}
			DragonStarReplace = DragonStarReplaceTemp ;			
		} else {
			DragonStarReplace = new int[] {};
		}
		DragonConsumeSkillId = Integer.parseInt(element.getAttribute("DragonConsumeSkillId") == null || element.getAttribute("DragonConsumeSkillId").length() == 0 ? "0"
			: element.getAttribute("DragonConsumeSkillId")); // 龙获得后 自动战中学会龙技能  调用技能id=Almost配置表_战斗_Skill——HeroSkill#技能
		DragonArtResourceID = element.getAttribute("DragonArtResourceID"); // 龙资源id  调用ArtResource表
	}
	

}
