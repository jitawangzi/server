package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 强援
 * 
 * 工具生成的，不要手动修改
 */
 public class RescueConfig {

	/** 强援ID */
	public final int ID;		
	/** 强援名称 */
	public final String RescueName;		
	/** 修炼类型 1-潜力分页-基础属性 2-潜力分页-元素伤害 3-潜力分页-众生念力 4-强援分页  同组标识 */
	public final int CultivationType;		
	/** 同属性标识  id相同，说明是同一属性的不同品质 */
	public final int RescueMark;		
	/** 解锁条件  【等级解锁】 1；玩家等级 【章节解锁】 2；Map#地图id；BattleID 2;2;1003 界面显示2-1 只读每种第一个绿色即可 */
	public final int[] RescueUnlock;		
	/** 初始品质 1-白色（凡品） 2-绿色（素品） 3-蓝色（上品） 4-紫色（极品） 5-金色（臻品） 6-红色（仙品） 7-彩色（先天） 8-永恒（至宝） 9-唯一（灵宝） */
	public final int Quality;		
	/** 【等级换底板】  达到最低潜力等级；变为新潜力id  最后1档品质配置9999为满 */
	public final int RescueLv;		
	/** 【升级对应技能组id】  调用HeroSkillGroup#技能组表中id */
	public final int RescueLvHeroSkillGroup;		
	/** 【每次升级增加暴击伤害%】  百分比属性id；每级增加百分比值/10000  专属影响CultivationType=4中所有技能的暴击伤害 */
	public final int[] RescueMulHurtPerGrow;		
	/** 【每级消耗】  消耗货币id；初始值y1；固定系数a；固定系数b|消耗技能书id；初始值y1；固定系数a；固定系数b  计算公式： 当前值=INT((固定系数a*(当前等级+固定系数b)^2+初始值)/100+1)*10 */
	public final int[][] RescueConsume;		

	public RescueConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 强援ID
		RescueName = element.getAttribute("RescueName"); // 强援名称
		CultivationType = Integer.parseInt(element.getAttribute("CultivationType") == null || element.getAttribute("CultivationType").length() == 0 ? "0"
			: element.getAttribute("CultivationType")); // 修炼类型 1-潜力分页-基础属性 2-潜力分页-元素伤害 3-潜力分页-众生念力 4-强援分页  同组标识
		RescueMark = Integer.parseInt(element.getAttribute("RescueMark") == null || element.getAttribute("RescueMark").length() == 0 ? "0"
			: element.getAttribute("RescueMark")); // 同属性标识  id相同，说明是同一属性的不同品质
		String RescueUnlockString = element.getAttribute("RescueUnlock"); // 解锁条件  【等级解锁】 1；玩家等级 【章节解锁】 2；Map#地图id；BattleID 2;2;1003 界面显示2-1 只读每种第一个绿色即可
		if (RescueUnlockString != null && RescueUnlockString.length() > 0) {
			String[] RescueUnlockStrings = RescueUnlockString.split(";"); 
			int[] RescueUnlockTemp = new int[RescueUnlockStrings.length] ; 
			for (int i = 0; i < RescueUnlockStrings.length; i++) {
				int temp = Integer.parseInt(RescueUnlockStrings[i]);	
				RescueUnlockTemp[i] = temp;
			}
			RescueUnlock = RescueUnlockTemp ;			
		} else {
			RescueUnlock = new int[] {};
		}
		Quality = Integer.parseInt(element.getAttribute("Quality") == null || element.getAttribute("Quality").length() == 0 ? "0"
			: element.getAttribute("Quality")); // 初始品质 1-白色（凡品） 2-绿色（素品） 3-蓝色（上品） 4-紫色（极品） 5-金色（臻品） 6-红色（仙品） 7-彩色（先天） 8-永恒（至宝） 9-唯一（灵宝）
		RescueLv = Integer.parseInt(element.getAttribute("RescueLv") == null || element.getAttribute("RescueLv").length() == 0 ? "0"
			: element.getAttribute("RescueLv")); // 【等级换底板】  达到最低潜力等级；变为新潜力id  最后1档品质配置9999为满
		RescueLvHeroSkillGroup = Integer.parseInt(element.getAttribute("RescueLvHeroSkillGroup") == null || element.getAttribute("RescueLvHeroSkillGroup").length() == 0 ? "0"
			: element.getAttribute("RescueLvHeroSkillGroup")); // 【升级对应技能组id】  调用HeroSkillGroup#技能组表中id
		String RescueMulHurtPerGrowString = element.getAttribute("RescueMulHurtPerGrow"); // 【每次升级增加暴击伤害%】  百分比属性id；每级增加百分比值/10000  专属影响CultivationType=4中所有技能的暴击伤害
		if (RescueMulHurtPerGrowString != null && RescueMulHurtPerGrowString.length() > 0) {
			String[] RescueMulHurtPerGrowStrings = RescueMulHurtPerGrowString.split(";"); 
			int[] RescueMulHurtPerGrowTemp = new int[RescueMulHurtPerGrowStrings.length] ; 
			for (int i = 0; i < RescueMulHurtPerGrowStrings.length; i++) {
				int temp = Integer.parseInt(RescueMulHurtPerGrowStrings[i]);	
				RescueMulHurtPerGrowTemp[i] = temp;
			}
			RescueMulHurtPerGrow = RescueMulHurtPerGrowTemp ;			
		} else {
			RescueMulHurtPerGrow = new int[] {};
		}
		String RescueConsumeString = element.getAttribute("RescueConsume"); // 【每级消耗】  消耗货币id；初始值y1；固定系数a；固定系数b|消耗技能书id；初始值y1；固定系数a；固定系数b  计算公式： 当前值=INT((固定系数a*(当前等级+固定系数b)^2+初始值)/100+1)*10
		if (RescueConsumeString != null && RescueConsumeString.length() > 0) {
			String[] RescueConsumeStrings = RescueConsumeString.split("\\|"); 
			int[][] RescueConsumeTemp = new int[RescueConsumeStrings.length][] ; 
			for (int i = 0; i < RescueConsumeStrings.length; i++) {
				String[] RescueConsumeStrings2 = RescueConsumeStrings[i].split(";"); 
				int[] array = new int[RescueConsumeStrings2.length];
				for (int j = 0; j < RescueConsumeStrings2.length; j++) {
					int temp = Integer.parseInt(RescueConsumeStrings2[j]);	
					array[j] = temp;
				}
				RescueConsumeTemp[i] = array;
			}
			RescueConsume = RescueConsumeTemp ;			
		} else {
			RescueConsume = new int[][] {};
		}
	}
	

}
