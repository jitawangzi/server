package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 战役
 * 
 * 工具生成的，不要手动修改
 */
 public class BattleConfig {

	/** 战役ID */
	public final int ID;		
	/** 战役类型 1-主线 2-历练精英 */
	public final int BattleType;		
	/** 推荐战斗力  直接填入战斗力的具体数值 */
	public final int AtkValue;		
	/** 前置战役ID  调用本表id列 */
	public final int preBattle;		
	/** 每次进入消耗id  调用Consume表id 主线固定消耗5体力 */
	public final int cost;		
	/** 胜利奖励  掉落表id */
	public final int WinRandom;		
	/** 失败奖励  掉落表id */
	public final int FailRandom;		
	/** 章节宝箱触发条件   宝箱1坚持分钟数;宝箱2坚持分钟数;宝箱3通关 2;4;999 其中999为通关标识 */
	public final int[] BattleBoxTrigger;		
	/** 章节宝箱奖励掉落id组 */
	public final int[] BattleBoxRandomId;		
	/** 折算方法 */
	public final String ConvertAwardFUN;		
	/** 折算条件 */
	public final int[] FUNCondition;		
	/** 折算系数 */
	public final int[] FUNFactor;		
	/** 折算奖励 */
	public final int[] FUNRandom;		

	public BattleConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 战役ID
		BattleType = Integer.parseInt(element.getAttribute("BattleType") == null || element.getAttribute("BattleType").length() == 0 ? "0"
			: element.getAttribute("BattleType")); // 战役类型 1-主线 2-历练精英
		AtkValue = Integer.parseInt(element.getAttribute("AtkValue") == null || element.getAttribute("AtkValue").length() == 0 ? "0"
			: element.getAttribute("AtkValue")); // 推荐战斗力  直接填入战斗力的具体数值
		preBattle = Integer.parseInt(element.getAttribute("preBattle") == null || element.getAttribute("preBattle").length() == 0 ? "0"
			: element.getAttribute("preBattle")); // 前置战役ID  调用本表id列
		cost = Integer.parseInt(element.getAttribute("cost") == null || element.getAttribute("cost").length() == 0 ? "0"
			: element.getAttribute("cost")); // 每次进入消耗id  调用Consume表id 主线固定消耗5体力
		WinRandom = Integer.parseInt(element.getAttribute("WinRandom") == null || element.getAttribute("WinRandom").length() == 0 ? "0"
			: element.getAttribute("WinRandom")); // 胜利奖励  掉落表id
		FailRandom = Integer.parseInt(element.getAttribute("FailRandom") == null || element.getAttribute("FailRandom").length() == 0 ? "0"
			: element.getAttribute("FailRandom")); // 失败奖励  掉落表id
		String BattleBoxTriggerString = element.getAttribute("BattleBoxTrigger"); // 章节宝箱触发条件   宝箱1坚持分钟数;宝箱2坚持分钟数;宝箱3通关 2;4;999 其中999为通关标识
		if (BattleBoxTriggerString != null && BattleBoxTriggerString.length() > 0) {
			String[] BattleBoxTriggerStrings = BattleBoxTriggerString.split(";"); 
			int[] BattleBoxTriggerTemp = new int[BattleBoxTriggerStrings.length] ; 
			for (int i = 0; i < BattleBoxTriggerStrings.length; i++) {
				int temp = Integer.parseInt(BattleBoxTriggerStrings[i]);	
				BattleBoxTriggerTemp[i] = temp;
			}
			BattleBoxTrigger = BattleBoxTriggerTemp ;			
		} else {
			BattleBoxTrigger = new int[] {};
		}
		String BattleBoxRandomIdString = element.getAttribute("BattleBoxRandomId"); // 章节宝箱奖励掉落id组
		if (BattleBoxRandomIdString != null && BattleBoxRandomIdString.length() > 0) {
			String[] BattleBoxRandomIdStrings = BattleBoxRandomIdString.split(";"); 
			int[] BattleBoxRandomIdTemp = new int[BattleBoxRandomIdStrings.length] ; 
			for (int i = 0; i < BattleBoxRandomIdStrings.length; i++) {
				int temp = Integer.parseInt(BattleBoxRandomIdStrings[i]);	
				BattleBoxRandomIdTemp[i] = temp;
			}
			BattleBoxRandomId = BattleBoxRandomIdTemp ;			
		} else {
			BattleBoxRandomId = new int[] {};
		}
		ConvertAwardFUN = element.getAttribute("ConvertAwardFUN"); // 折算方法
		String FUNConditionString = element.getAttribute("FUNCondition"); // 折算条件
		if (FUNConditionString != null && FUNConditionString.length() > 0) {
			String[] FUNConditionStrings = FUNConditionString.split(";"); 
			int[] FUNConditionTemp = new int[FUNConditionStrings.length] ; 
			for (int i = 0; i < FUNConditionStrings.length; i++) {
				int temp = Integer.parseInt(FUNConditionStrings[i]);	
				FUNConditionTemp[i] = temp;
			}
			FUNCondition = FUNConditionTemp ;			
		} else {
			FUNCondition = new int[] {};
		}
		String FUNFactorString = element.getAttribute("FUNFactor"); // 折算系数
		if (FUNFactorString != null && FUNFactorString.length() > 0) {
			String[] FUNFactorStrings = FUNFactorString.split(";"); 
			int[] FUNFactorTemp = new int[FUNFactorStrings.length] ; 
			for (int i = 0; i < FUNFactorStrings.length; i++) {
				int temp = Integer.parseInt(FUNFactorStrings[i]);	
				FUNFactorTemp[i] = temp;
			}
			FUNFactor = FUNFactorTemp ;			
		} else {
			FUNFactor = new int[] {};
		}
		String FUNRandomString = element.getAttribute("FUNRandom"); // 折算奖励
		if (FUNRandomString != null && FUNRandomString.length() > 0) {
			String[] FUNRandomStrings = FUNRandomString.split(";"); 
			int[] FUNRandomTemp = new int[FUNRandomStrings.length] ; 
			for (int i = 0; i < FUNRandomStrings.length; i++) {
				int temp = Integer.parseInt(FUNRandomStrings[i]);	
				FUNRandomTemp[i] = temp;
			}
			FUNRandom = FUNRandomTemp ;			
		} else {
			FUNRandom = new int[] {};
		}
	}
	

}
