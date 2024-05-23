package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 潜力
 * 
 * 工具生成的，不要手动修改
 */
 public class PotentialConfig {

	/** 潜力ID */
	public final int ID;		
	/** 修炼类型 1-潜力分页-基础属性 2-潜力分页-元素伤害 3-潜力分页-众生念力 4-强援分页  同组标识 */
	public final int CultivationType;		
	/** 同属性标识  id相同，说明是同一属性的不同品质 */
	public final int PotentialMark;		
	/** 解锁条件  【等级解锁】 1；玩家等级 【章节解锁】 2；Map#地图id；BattleID 2;2;1003 界面显示2-1 只读每种第一个绿色即可 */
	public final int[] PotentialUnlock;		
	/** 初始品质 1-白色（凡品） 2-绿色（素品） 3-蓝色（上品） 4-紫色（极品） 5-金色（臻品） 6-红色（仙品） 7-彩色（先天） 8-永恒（至宝） 9-唯一（灵宝） */
	public final int Quality;		
	/** 【等级换底板】  达到最低潜力等级；变为新潜力id   最后1档品质配置9999为满 */
	public final int[] PotentialLv;		
	/** 【初始值】  【数值属性配置】   属性id；1级初始值 【百分比属性配置】   属性id；1级初始值/10000  界面小数预留2位-0.0x */
	public final int[] PotentialBase;		
	/** 【成长值】  【数值属性配置】   属性id；每级成长 【百分比属性配置】   属性id；每级成长值/10000 计算公式： 当前值 = 初始值 +（lv-1）*每级成长值 */
	public final int[] PotentialGrow;		
	/** 【每级消耗】  消耗货币id；初始值y1；固定系数a；固定系数b 100206-潜力升级道具-灵韵珠  计算公式： 当前值=INT((固定系数a*(当前等级+固定系数b)^2+初始值)/100+1)*10 */
	public final int[] PotentialConsume;		
	/** 【突破】  每xx级1次突破；突破所需货币id；每次突破固定数量消耗 100207-潜力突破道具-蕴灵液  每个品质下，突破所需道具数量是固定的 */
	public final int[] PotentialBreak;		

	public PotentialConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 潜力ID
		CultivationType = Integer.parseInt(element.getAttribute("CultivationType") == null || element.getAttribute("CultivationType").length() == 0 ? "0"
			: element.getAttribute("CultivationType")); // 修炼类型 1-潜力分页-基础属性 2-潜力分页-元素伤害 3-潜力分页-众生念力 4-强援分页  同组标识
		PotentialMark = Integer.parseInt(element.getAttribute("PotentialMark") == null || element.getAttribute("PotentialMark").length() == 0 ? "0"
			: element.getAttribute("PotentialMark")); // 同属性标识  id相同，说明是同一属性的不同品质
		String PotentialUnlockString = element.getAttribute("PotentialUnlock"); // 解锁条件  【等级解锁】 1；玩家等级 【章节解锁】 2；Map#地图id；BattleID 2;2;1003 界面显示2-1 只读每种第一个绿色即可
		if (PotentialUnlockString != null && PotentialUnlockString.length() > 0) {
			String[] PotentialUnlockStrings = PotentialUnlockString.split(";"); 
			int[] PotentialUnlockTemp = new int[PotentialUnlockStrings.length] ; 
			for (int i = 0; i < PotentialUnlockStrings.length; i++) {
				int temp = Integer.parseInt(PotentialUnlockStrings[i]);	
				PotentialUnlockTemp[i] = temp;
			}
			PotentialUnlock = PotentialUnlockTemp ;			
		} else {
			PotentialUnlock = new int[] {};
		}
		Quality = Integer.parseInt(element.getAttribute("Quality") == null || element.getAttribute("Quality").length() == 0 ? "0"
			: element.getAttribute("Quality")); // 初始品质 1-白色（凡品） 2-绿色（素品） 3-蓝色（上品） 4-紫色（极品） 5-金色（臻品） 6-红色（仙品） 7-彩色（先天） 8-永恒（至宝） 9-唯一（灵宝）
		String PotentialLvString = element.getAttribute("PotentialLv"); // 【等级换底板】  达到最低潜力等级；变为新潜力id   最后1档品质配置9999为满
		if (PotentialLvString != null && PotentialLvString.length() > 0) {
			String[] PotentialLvStrings = PotentialLvString.split(";"); 
			int[] PotentialLvTemp = new int[PotentialLvStrings.length] ; 
			for (int i = 0; i < PotentialLvStrings.length; i++) {
				int temp = Integer.parseInt(PotentialLvStrings[i]);	
				PotentialLvTemp[i] = temp;
			}
			PotentialLv = PotentialLvTemp ;			
		} else {
			PotentialLv = new int[] {};
		}
		String PotentialBaseString = element.getAttribute("PotentialBase"); // 【初始值】  【数值属性配置】   属性id；1级初始值 【百分比属性配置】   属性id；1级初始值/10000  界面小数预留2位-0.0x
		if (PotentialBaseString != null && PotentialBaseString.length() > 0) {
			String[] PotentialBaseStrings = PotentialBaseString.split(";"); 
			int[] PotentialBaseTemp = new int[PotentialBaseStrings.length] ; 
			for (int i = 0; i < PotentialBaseStrings.length; i++) {
				int temp = Integer.parseInt(PotentialBaseStrings[i]);	
				PotentialBaseTemp[i] = temp;
			}
			PotentialBase = PotentialBaseTemp ;			
		} else {
			PotentialBase = new int[] {};
		}
		String PotentialGrowString = element.getAttribute("PotentialGrow"); // 【成长值】  【数值属性配置】   属性id；每级成长 【百分比属性配置】   属性id；每级成长值/10000 计算公式： 当前值 = 初始值 +（lv-1）*每级成长值
		if (PotentialGrowString != null && PotentialGrowString.length() > 0) {
			String[] PotentialGrowStrings = PotentialGrowString.split(";"); 
			int[] PotentialGrowTemp = new int[PotentialGrowStrings.length] ; 
			for (int i = 0; i < PotentialGrowStrings.length; i++) {
				int temp = Integer.parseInt(PotentialGrowStrings[i]);	
				PotentialGrowTemp[i] = temp;
			}
			PotentialGrow = PotentialGrowTemp ;			
		} else {
			PotentialGrow = new int[] {};
		}
		String PotentialConsumeString = element.getAttribute("PotentialConsume"); // 【每级消耗】  消耗货币id；初始值y1；固定系数a；固定系数b 100206-潜力升级道具-灵韵珠  计算公式： 当前值=INT((固定系数a*(当前等级+固定系数b)^2+初始值)/100+1)*10
		if (PotentialConsumeString != null && PotentialConsumeString.length() > 0) {
			String[] PotentialConsumeStrings = PotentialConsumeString.split(";"); 
			int[] PotentialConsumeTemp = new int[PotentialConsumeStrings.length] ; 
			for (int i = 0; i < PotentialConsumeStrings.length; i++) {
				int temp = Integer.parseInt(PotentialConsumeStrings[i]);	
				PotentialConsumeTemp[i] = temp;
			}
			PotentialConsume = PotentialConsumeTemp ;			
		} else {
			PotentialConsume = new int[] {};
		}
		String PotentialBreakString = element.getAttribute("PotentialBreak"); // 【突破】  每xx级1次突破；突破所需货币id；每次突破固定数量消耗 100207-潜力突破道具-蕴灵液  每个品质下，突破所需道具数量是固定的
		if (PotentialBreakString != null && PotentialBreakString.length() > 0) {
			String[] PotentialBreakStrings = PotentialBreakString.split(";"); 
			int[] PotentialBreakTemp = new int[PotentialBreakStrings.length] ; 
			for (int i = 0; i < PotentialBreakStrings.length; i++) {
				int temp = Integer.parseInt(PotentialBreakStrings[i]);	
				PotentialBreakTemp[i] = temp;
			}
			PotentialBreak = PotentialBreakTemp ;			
		} else {
			PotentialBreak = new int[] {};
		}
	}
	

}
