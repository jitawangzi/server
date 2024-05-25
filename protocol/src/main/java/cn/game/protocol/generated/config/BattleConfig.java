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
	/** 章节ID */
	public final int Chapter;		
	/** 战役类型 1-主线 2-历练精英 */
	public final int BattleType;		
	/** 前置战役ID  调用本表id列 */
	public final int preBattle;		
	/** 每次进入消耗id  调用Consume表id 主线固定消耗5体力 */
	public final int cost;		
	/** 胜利奖励  掉落表id */
	public final int WinRandom;		
	/** 失败已通关波次 */
	public final int[] FailRandomTrigger;		
	/** 失败获得奖励掉落id组  掉落表id */
	public final int[] FailRandom;		
	/** BUFF条数  1-增益 2-减益 读取HeroSkill——GamePlayRandomBuff#玩法随机buff表中，不同玩法对应的buff进行随机 */
	public final int[][] Cnt;		
	/** 章节宝箱触发条件  宝箱1达成波次；宝箱2达成波次；宝箱3达成波次 */
	public final int[] BattleBoxTrigger;		
	/** 章节宝箱奖励掉落id组  宝箱1掉落id；宝箱2掉落id；宝箱3掉落id */
	public final int[] BattleBoxRandomId;		
	/** 该战役总波次数 */
	public final int BattleRoundNum;		
	/** 每波次合成时 刷出装备个数 */
	public final int[] EquipRefreshNum;		
	/** 日常-积分条件  累计积分数量： Asset#资产表 积分id：100302 */
	public final int[] DailyIntegralCondition;		
	/** 日常-积分获得宝箱  调用掉落id */
	public final int[] DailyInBoxRandomId;		
	/** 战斗背景图美术资源 调用：\merge\src\First_party\art\xiyou UI\bg_背景 */
	public final String MainInterBack;		

	public BattleConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 战役ID
		Chapter = Integer.parseInt(element.getAttribute("Chapter") == null || element.getAttribute("Chapter").length() == 0 ? "0"
			: element.getAttribute("Chapter")); // 章节ID
		BattleType = Integer.parseInt(element.getAttribute("BattleType") == null || element.getAttribute("BattleType").length() == 0 ? "0"
			: element.getAttribute("BattleType")); // 战役类型 1-主线 2-历练精英
		preBattle = Integer.parseInt(element.getAttribute("preBattle") == null || element.getAttribute("preBattle").length() == 0 ? "0"
			: element.getAttribute("preBattle")); // 前置战役ID  调用本表id列
		cost = Integer.parseInt(element.getAttribute("cost") == null || element.getAttribute("cost").length() == 0 ? "0"
			: element.getAttribute("cost")); // 每次进入消耗id  调用Consume表id 主线固定消耗5体力
		WinRandom = Integer.parseInt(element.getAttribute("WinRandom") == null || element.getAttribute("WinRandom").length() == 0 ? "0"
			: element.getAttribute("WinRandom")); // 胜利奖励  掉落表id
		String FailRandomTriggerString = element.getAttribute("FailRandomTrigger"); // 失败已通关波次
		if (FailRandomTriggerString != null && FailRandomTriggerString.length() > 0) {
			String[] FailRandomTriggerStrings = FailRandomTriggerString.split(";"); 
			int[] FailRandomTriggerTemp = new int[FailRandomTriggerStrings.length] ; 
			for (int i = 0; i < FailRandomTriggerStrings.length; i++) {
				int temp = Integer.parseInt(FailRandomTriggerStrings[i]);	
				FailRandomTriggerTemp[i] = temp;
			}
			FailRandomTrigger = FailRandomTriggerTemp ;			
		} else {
			FailRandomTrigger = new int[] {};
		}
		String FailRandomString = element.getAttribute("FailRandom"); // 失败获得奖励掉落id组  掉落表id
		if (FailRandomString != null && FailRandomString.length() > 0) {
			String[] FailRandomStrings = FailRandomString.split(";"); 
			int[] FailRandomTemp = new int[FailRandomStrings.length] ; 
			for (int i = 0; i < FailRandomStrings.length; i++) {
				int temp = Integer.parseInt(FailRandomStrings[i]);	
				FailRandomTemp[i] = temp;
			}
			FailRandom = FailRandomTemp ;			
		} else {
			FailRandom = new int[] {};
		}
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
		String BattleBoxTriggerString = element.getAttribute("BattleBoxTrigger"); // 章节宝箱触发条件  宝箱1达成波次；宝箱2达成波次；宝箱3达成波次
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
		String BattleBoxRandomIdString = element.getAttribute("BattleBoxRandomId"); // 章节宝箱奖励掉落id组  宝箱1掉落id；宝箱2掉落id；宝箱3掉落id
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
		BattleRoundNum = Integer.parseInt(element.getAttribute("BattleRoundNum") == null || element.getAttribute("BattleRoundNum").length() == 0 ? "0"
			: element.getAttribute("BattleRoundNum")); // 该战役总波次数
		String EquipRefreshNumString = element.getAttribute("EquipRefreshNum"); // 每波次合成时 刷出装备个数
		if (EquipRefreshNumString != null && EquipRefreshNumString.length() > 0) {
			String[] EquipRefreshNumStrings = EquipRefreshNumString.split(";"); 
			int[] EquipRefreshNumTemp = new int[EquipRefreshNumStrings.length] ; 
			for (int i = 0; i < EquipRefreshNumStrings.length; i++) {
				int temp = Integer.parseInt(EquipRefreshNumStrings[i]);	
				EquipRefreshNumTemp[i] = temp;
			}
			EquipRefreshNum = EquipRefreshNumTemp ;			
		} else {
			EquipRefreshNum = new int[] {};
		}
		String DailyIntegralConditionString = element.getAttribute("DailyIntegralCondition"); // 日常-积分条件  累计积分数量： Asset#资产表 积分id：100302
		if (DailyIntegralConditionString != null && DailyIntegralConditionString.length() > 0) {
			String[] DailyIntegralConditionStrings = DailyIntegralConditionString.split(";"); 
			int[] DailyIntegralConditionTemp = new int[DailyIntegralConditionStrings.length] ; 
			for (int i = 0; i < DailyIntegralConditionStrings.length; i++) {
				int temp = Integer.parseInt(DailyIntegralConditionStrings[i]);	
				DailyIntegralConditionTemp[i] = temp;
			}
			DailyIntegralCondition = DailyIntegralConditionTemp ;			
		} else {
			DailyIntegralCondition = new int[] {};
		}
		String DailyInBoxRandomIdString = element.getAttribute("DailyInBoxRandomId"); // 日常-积分获得宝箱  调用掉落id
		if (DailyInBoxRandomIdString != null && DailyInBoxRandomIdString.length() > 0) {
			String[] DailyInBoxRandomIdStrings = DailyInBoxRandomIdString.split(";"); 
			int[] DailyInBoxRandomIdTemp = new int[DailyInBoxRandomIdStrings.length] ; 
			for (int i = 0; i < DailyInBoxRandomIdStrings.length; i++) {
				int temp = Integer.parseInt(DailyInBoxRandomIdStrings[i]);	
				DailyInBoxRandomIdTemp[i] = temp;
			}
			DailyInBoxRandomId = DailyInBoxRandomIdTemp ;			
		} else {
			DailyInBoxRandomId = new int[] {};
		}
		MainInterBack = element.getAttribute("MainInterBack"); // 战斗背景图美术资源 调用：\merge\src\First_party\art\xiyou UI\bg_背景
	}
	

}
