package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 巡逻
 * 
 * 工具生成的，不要手动修改
 */
 public class PatrolConfig {

	/** 巡逻ID */
	public final int ID;		
	/** 要求通关 战役ID */
	public final int ConditionBattleID;		
	/** 每5分钟金币收益 */
	public final int IncomeGold;		
	/** 每5分钟经验收益 */
	public final int IncomeEXP;		
	/** 每5分钟掉落ID */
	public final int[] IncomeRandomID;		
	/** 单次巡逻时长 单位：分钟 */
	public final int OnceDuration;		
	/** 快速巡逻时长 单位：分钟 */
	public final int FastDuration;		
	/** 巡逻时间上限 单位：分钟 */
	public final int TimeLimit;		
	/** 快速巡逻次数 */
	public final int FastTriesLimit;		
	/** 月卡附加次数 999=无限 */
	public final int CardsNumber;		
	/** 快速巡逻消耗ID */
	public final int FastConsumeID;		
	/** 广告巡逻次数 */
	public final int AdvertisementTriesLimit;		

	public PatrolConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 巡逻ID
		ConditionBattleID = Integer.parseInt(element.getAttribute("ConditionBattleID") == null || element.getAttribute("ConditionBattleID").length() == 0 ? "0"
			: element.getAttribute("ConditionBattleID")); // 要求通关 战役ID
		IncomeGold = Integer.parseInt(element.getAttribute("IncomeGold") == null || element.getAttribute("IncomeGold").length() == 0 ? "0"
			: element.getAttribute("IncomeGold")); // 每5分钟金币收益
		IncomeEXP = Integer.parseInt(element.getAttribute("IncomeEXP") == null || element.getAttribute("IncomeEXP").length() == 0 ? "0"
			: element.getAttribute("IncomeEXP")); // 每5分钟经验收益
		String IncomeRandomIDString = element.getAttribute("IncomeRandomID"); // 每5分钟掉落ID
		if (IncomeRandomIDString != null && IncomeRandomIDString.length() > 0) {
			String[] IncomeRandomIDStrings = IncomeRandomIDString.split(";"); 
			int[] IncomeRandomIDTemp = new int[IncomeRandomIDStrings.length] ; 
			for (int i = 0; i < IncomeRandomIDStrings.length; i++) {
				int temp = Integer.parseInt(IncomeRandomIDStrings[i]);	
				IncomeRandomIDTemp[i] = temp;
			}
			IncomeRandomID = IncomeRandomIDTemp ;			
		} else {
			IncomeRandomID = new int[] {};
		}
		OnceDuration = Integer.parseInt(element.getAttribute("OnceDuration") == null || element.getAttribute("OnceDuration").length() == 0 ? "0"
			: element.getAttribute("OnceDuration")); // 单次巡逻时长 单位：分钟
		FastDuration = Integer.parseInt(element.getAttribute("FastDuration") == null || element.getAttribute("FastDuration").length() == 0 ? "0"
			: element.getAttribute("FastDuration")); // 快速巡逻时长 单位：分钟
		TimeLimit = Integer.parseInt(element.getAttribute("TimeLimit") == null || element.getAttribute("TimeLimit").length() == 0 ? "0"
			: element.getAttribute("TimeLimit")); // 巡逻时间上限 单位：分钟
		FastTriesLimit = Integer.parseInt(element.getAttribute("FastTriesLimit") == null || element.getAttribute("FastTriesLimit").length() == 0 ? "0"
			: element.getAttribute("FastTriesLimit")); // 快速巡逻次数
		CardsNumber = Integer.parseInt(element.getAttribute("CardsNumber") == null || element.getAttribute("CardsNumber").length() == 0 ? "0"
			: element.getAttribute("CardsNumber")); // 月卡附加次数 999=无限
		FastConsumeID = Integer.parseInt(element.getAttribute("FastConsumeID") == null || element.getAttribute("FastConsumeID").length() == 0 ? "0"
			: element.getAttribute("FastConsumeID")); // 快速巡逻消耗ID
		AdvertisementTriesLimit = Integer.parseInt(element.getAttribute("AdvertisementTriesLimit") == null || element.getAttribute("AdvertisementTriesLimit").length() == 0 ? "0"
			: element.getAttribute("AdvertisementTriesLimit")); // 广告巡逻次数
	}
	

}
