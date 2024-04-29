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
	/** 关卡ID  调用：BattleField#关卡 */
	public final int BattleFieldID;		
	/** 前置战役ID  调用本表id列 */
	public final int preBattle;		
	/** 每次进入消耗id  调用Consume表id 主线固定消耗5体力 */
	public final int cost;		
	/** 胜利奖励  掉落表id */
	public final int WinRandom;		
	/** 失败奖励  掉落表id */
	public final int FailRandom;		
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
		BattleFieldID = Integer.parseInt(element.getAttribute("BattleFieldID") == null || element.getAttribute("BattleFieldID").length() == 0 ? "0"
			: element.getAttribute("BattleFieldID")); // 关卡ID  调用：BattleField#关卡
		preBattle = Integer.parseInt(element.getAttribute("preBattle") == null || element.getAttribute("preBattle").length() == 0 ? "0"
			: element.getAttribute("preBattle")); // 前置战役ID  调用本表id列
		cost = Integer.parseInt(element.getAttribute("cost") == null || element.getAttribute("cost").length() == 0 ? "0"
			: element.getAttribute("cost")); // 每次进入消耗id  调用Consume表id 主线固定消耗5体力
		WinRandom = Integer.parseInt(element.getAttribute("WinRandom") == null || element.getAttribute("WinRandom").length() == 0 ? "0"
			: element.getAttribute("WinRandom")); // 胜利奖励  掉落表id
		FailRandom = Integer.parseInt(element.getAttribute("FailRandom") == null || element.getAttribute("FailRandom").length() == 0 ? "0"
			: element.getAttribute("FailRandom")); // 失败奖励  掉落表id
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
