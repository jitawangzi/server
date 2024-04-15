package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 
 * 
 * 工具生成的，不要手动修改
 */
 public class BattleConfig {

	/** 战役ID */
	public final int ID;		
	/** 战役类型 */
	public final int BattleType;		
	/** 关卡ID */
	public final int BattleFieldID;		
	/** 前置战役ID */
	public final int preBattle;		
	/** 进入条件ID */
	public final int enterCondtion;		
	/** 挑战消耗ID */
	public final int cost;		
	/** 周几开启 */
	public final int[] openDay;		
	/** 次数上限 */
	public final int[] timesLimit;		
	/** 折算方法 */
	public final String ConvertAwardFUN;		
	/** 折算条件 */
	public final int[] FUNCondition;		
	/** 折算系数 */
	public final int[] FUNFactor;		
	/** 折算奖励 */
	public final int[] FUNRandom;		
	/** 失败奖励 */
	public final int FailRandom;		
	/** 胜利奖励 */
	public final int WinRandom;		
	/** 首次胜利奖励 */
	public final int OnlyWinRan;		
	/** 首次半血胜利奖励 */
	public final int OnlyWin50Ran;		
	/** 首次无损胜利奖励 */
	public final int OnlyWin100Ran;		

	public BattleConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 战役ID
		BattleType = Integer.parseInt(element.getAttribute("BattleType") == null || element.getAttribute("BattleType").length() == 0 ? "0"
			: element.getAttribute("BattleType")); // 战役类型
		BattleFieldID = Integer.parseInt(element.getAttribute("BattleFieldID") == null || element.getAttribute("BattleFieldID").length() == 0 ? "0"
			: element.getAttribute("BattleFieldID")); // 关卡ID
		preBattle = Integer.parseInt(element.getAttribute("preBattle") == null || element.getAttribute("preBattle").length() == 0 ? "0"
			: element.getAttribute("preBattle")); // 前置战役ID
		enterCondtion = Integer.parseInt(element.getAttribute("enterCondtion") == null || element.getAttribute("enterCondtion").length() == 0 ? "0"
			: element.getAttribute("enterCondtion")); // 进入条件ID
		cost = Integer.parseInt(element.getAttribute("cost") == null || element.getAttribute("cost").length() == 0 ? "0"
			: element.getAttribute("cost")); // 挑战消耗ID
		String openDayString = element.getAttribute("openDay"); // 周几开启
		if (openDayString != null && openDayString.length() > 0) {
			String[] openDayStrings = openDayString.split(";"); 
			int[] openDayTemp = new int[openDayStrings.length] ; 
			for (int i = 0; i < openDayStrings.length; i++) {
				int temp = Integer.parseInt(openDayStrings[i]);	
				openDayTemp[i] = temp;
			}
			openDay = openDayTemp ;			
		} else {
			openDay = new int[] {};
		}
		String timesLimitString = element.getAttribute("timesLimit"); // 次数上限
		if (timesLimitString != null && timesLimitString.length() > 0) {
			String[] timesLimitStrings = timesLimitString.split(";"); 
			int[] timesLimitTemp = new int[timesLimitStrings.length] ; 
			for (int i = 0; i < timesLimitStrings.length; i++) {
				int temp = Integer.parseInt(timesLimitStrings[i]);	
				timesLimitTemp[i] = temp;
			}
			timesLimit = timesLimitTemp ;			
		} else {
			timesLimit = new int[] {};
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
		FailRandom = Integer.parseInt(element.getAttribute("FailRandom") == null || element.getAttribute("FailRandom").length() == 0 ? "0"
			: element.getAttribute("FailRandom")); // 失败奖励
		WinRandom = Integer.parseInt(element.getAttribute("WinRandom") == null || element.getAttribute("WinRandom").length() == 0 ? "0"
			: element.getAttribute("WinRandom")); // 胜利奖励
		OnlyWinRan = Integer.parseInt(element.getAttribute("OnlyWinRan") == null || element.getAttribute("OnlyWinRan").length() == 0 ? "0"
			: element.getAttribute("OnlyWinRan")); // 首次胜利奖励
		OnlyWin50Ran = Integer.parseInt(element.getAttribute("OnlyWin50Ran") == null || element.getAttribute("OnlyWin50Ran").length() == 0 ? "0"
			: element.getAttribute("OnlyWin50Ran")); // 首次半血胜利奖励
		OnlyWin100Ran = Integer.parseInt(element.getAttribute("OnlyWin100Ran") == null || element.getAttribute("OnlyWin100Ran").length() == 0 ? "0"
			: element.getAttribute("OnlyWin100Ran")); // 首次无损胜利奖励
	}
	

}
