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
	/** 战役类型 1-主线 2-道心历练 3-心魔试炼 */
	public final int BattleType;		
	/** 推荐战力  >1万，显示XX.Y万 >1亿，显示XX.Y亿  空=对战力无要求 */
	public final int AtkValue;		
	/** 前置战役ID  调用本表id列 */
	public final int preBattle;		
	/** 每次进入消耗id  调用Consume表id 主线固定消耗5体力 */
	public final int cost;		
	/** 胜利奖励  掉落表id */
	public final int WinRandom;		
	/** 失败奖励  掉落表id */
	public final int FailRandom;		
	/** BUFF条数  1-增益 2-减益 读取HeroSkill——GamePlayRandomBuff#玩法随机buff表中，不同玩法对应的buff进行随机 */
	public final int[][] Cnt;		
	/** 首通奖励  调用：Random掉落ID 固定必给奖励 */
	public final int FirstPassReward;		
	/** 扫荡奖励  调用：Random掉落ID 目前配置扫荡奖励=通关奖励 */
	public final int SweepReward;		
	/** 通关奖励宝箱  调用：Random掉落ID 额外固定奖励，有UI */
	public final int ClearGameReward;		
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
			: element.getAttribute("BattleType")); // 战役类型 1-主线 2-道心历练 3-心魔试炼
		AtkValue = Integer.parseInt(element.getAttribute("AtkValue") == null || element.getAttribute("AtkValue").length() == 0 ? "0"
			: element.getAttribute("AtkValue")); // 推荐战力  >1万，显示XX.Y万 >1亿，显示XX.Y亿  空=对战力无要求
		preBattle = Integer.parseInt(element.getAttribute("preBattle") == null || element.getAttribute("preBattle").length() == 0 ? "0"
			: element.getAttribute("preBattle")); // 前置战役ID  调用本表id列
		cost = Integer.parseInt(element.getAttribute("cost") == null || element.getAttribute("cost").length() == 0 ? "0"
			: element.getAttribute("cost")); // 每次进入消耗id  调用Consume表id 主线固定消耗5体力
		WinRandom = Integer.parseInt(element.getAttribute("WinRandom") == null || element.getAttribute("WinRandom").length() == 0 ? "0"
			: element.getAttribute("WinRandom")); // 胜利奖励  掉落表id
		FailRandom = Integer.parseInt(element.getAttribute("FailRandom") == null || element.getAttribute("FailRandom").length() == 0 ? "0"
			: element.getAttribute("FailRandom")); // 失败奖励  掉落表id
		String CntString = element.getAttribute("Cnt"); // BUFF条数  1-增益 2-减益 读取HeroSkill——GamePlayRandomBuff#玩法随机buff表中，不同玩法对应的buff进行随机
		if (CntString != null && CntString.length() > 0) {
			String[] CntStrings = CntString.split("\\|"); 
			int[][] CntTemp = new int[CntStrings.length][] ; 
			for (int i = 0; i < CntStrings.length; i++) {
				String[] CntStrings2 = CntStrings[i].split(";"); 
				int[] array = new int[CntStrings2.length];
				for (int j = 0; j < CntStrings2.length; j++) {
					int temp = Integer.parseInt(CntStrings2[j]);	
					array[j] = temp;
				}
				CntTemp[i] = array;
			}
			Cnt = CntTemp ;			
		} else {
			Cnt = new int[][] {};
		}
		FirstPassReward = Integer.parseInt(element.getAttribute("FirstPassReward") == null || element.getAttribute("FirstPassReward").length() == 0 ? "0"
			: element.getAttribute("FirstPassReward")); // 首通奖励  调用：Random掉落ID 固定必给奖励
		SweepReward = Integer.parseInt(element.getAttribute("SweepReward") == null || element.getAttribute("SweepReward").length() == 0 ? "0"
			: element.getAttribute("SweepReward")); // 扫荡奖励  调用：Random掉落ID 目前配置扫荡奖励=通关奖励
		ClearGameReward = Integer.parseInt(element.getAttribute("ClearGameReward") == null || element.getAttribute("ClearGameReward").length() == 0 ? "0"
			: element.getAttribute("ClearGameReward")); // 通关奖励宝箱  调用：Random掉落ID 额外固定奖励，有UI
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
