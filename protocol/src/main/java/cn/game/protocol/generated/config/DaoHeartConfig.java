package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 道心磨砺
 * 
 * 工具生成的，不要手动修改
 */
 public class DaoHeartConfig {

	/** 专属活动ID */
	public final int ID;		
	/** 类型  1-道心历练 2-心魔试炼 */
	public final int Type;		
	/** 战役ID  调Battle#战役表中id */
	public final int BattleID;		
	/** 前置ID  填本表中第1列 */
	public final int PreBattle;		
	/** 推荐战力  >1万，显示XX.Y万 >1亿，显示XX.Y亿 */
	public final int AtkValue;		
	/** BUFF条数  1-增益 2-减益 读取HeroSkill——GamePlayRandomBuff#玩法随机buff表中，不同玩法对应的buff进行随机 */
	public final int[][] Cnt;		
	/** 首通奖励  调用：Random掉落ID 固定必给奖励 */
	public final int FirstPassReward;		
	/** 扫荡奖励  调用：Random掉落ID 目前配置扫荡奖励=通关奖励 */
	public final int SweepReward;		
	/** 通关奖励宝箱  调用：Random掉落ID 额外固定奖励，有UI */
	public final int ClearGameReward;		

	public DaoHeartConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 专属活动ID
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 类型  1-道心历练 2-心魔试炼
		BattleID = Integer.parseInt(element.getAttribute("BattleID") == null || element.getAttribute("BattleID").length() == 0 ? "0"
			: element.getAttribute("BattleID")); // 战役ID  调Battle#战役表中id
		PreBattle = Integer.parseInt(element.getAttribute("PreBattle") == null || element.getAttribute("PreBattle").length() == 0 ? "0"
			: element.getAttribute("PreBattle")); // 前置ID  填本表中第1列
		AtkValue = Integer.parseInt(element.getAttribute("AtkValue") == null || element.getAttribute("AtkValue").length() == 0 ? "0"
			: element.getAttribute("AtkValue")); // 推荐战力  >1万，显示XX.Y万 >1亿，显示XX.Y亿
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
	}
	

}
