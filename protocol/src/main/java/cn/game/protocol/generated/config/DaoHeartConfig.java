package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 道心磨砺
 * 
 * 工具生成的，不要手动修改
 */
 public class DaoHeartConfig {

	/** ID */
	public final int ID;		
	/** 类型 1=道心历练 2=心魔试炼 */
	public final int Type;		
	/** 战役ID */
	public final int BattleID;		
	/** 前置ID */
	public final int PreBattle;		
	/** 推荐战力 */
	public final int AtkValue;		
	/** 资源图 */
	public final String Res;		
	/** BUFF条数 1=增益 2=减益 */
	public final int[][] Cnt;		
	/** 首通奖励 掉落ID */
	public final int FirstPassReward;		
	/** 扫荡奖励 */
	public final int SweepReward;		
	/** 通关奖励 */
	public final int ClearGameReward;		

	public DaoHeartConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 类型 1=道心历练 2=心魔试炼
		BattleID = Integer.parseInt(element.getAttribute("BattleID") == null || element.getAttribute("BattleID").length() == 0 ? "0"
			: element.getAttribute("BattleID")); // 战役ID
		PreBattle = Integer.parseInt(element.getAttribute("PreBattle") == null || element.getAttribute("PreBattle").length() == 0 ? "0"
			: element.getAttribute("PreBattle")); // 前置ID
		AtkValue = Integer.parseInt(element.getAttribute("AtkValue") == null || element.getAttribute("AtkValue").length() == 0 ? "0"
			: element.getAttribute("AtkValue")); // 推荐战力
		Res = element.getAttribute("Res"); // 资源图
		String CntString = element.getAttribute("Cnt"); // BUFF条数 1=增益 2=减益
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
			: element.getAttribute("FirstPassReward")); // 首通奖励 掉落ID
		SweepReward = Integer.parseInt(element.getAttribute("SweepReward") == null || element.getAttribute("SweepReward").length() == 0 ? "0"
			: element.getAttribute("SweepReward")); // 扫荡奖励
		ClearGameReward = Integer.parseInt(element.getAttribute("ClearGameReward") == null || element.getAttribute("ClearGameReward").length() == 0 ? "0"
			: element.getAttribute("ClearGameReward")); // 通关奖励
	}
	

}
