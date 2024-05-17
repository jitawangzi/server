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
	/** 类型 1=普通 2=精英 */
	public final int Type;		
	/** 战役ID */
	public final int BattleID;		
	/** 前置ID */
	public final int PreBattle;		
	/** 推荐战力 */
	public final int AtkValue;		
	/** 资源图 */
	public final String Res;		
	/** BUFF类型 1=增益 2=减益 */
	public final int BuffType;		
	/** 高难预警属性 */
	public final int[][] Attribute;		
	/** 高难预警文本 */
	public final String BUFFText;		
	/** 高难预警图标 */
	public final String BUFFRes;		
	/** 首通奖励 */
	public final int[] FirstPassReward;		
	/** 扫荡奖励 */
	public final int[] SweepReward;		
	/** 通关奖励 */
	public final int[] ClearGameReward;		

	public DaoHeartConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 类型 1=普通 2=精英
		BattleID = Integer.parseInt(element.getAttribute("BattleID") == null || element.getAttribute("BattleID").length() == 0 ? "0"
			: element.getAttribute("BattleID")); // 战役ID
		PreBattle = Integer.parseInt(element.getAttribute("PreBattle") == null || element.getAttribute("PreBattle").length() == 0 ? "0"
			: element.getAttribute("PreBattle")); // 前置ID
		AtkValue = Integer.parseInt(element.getAttribute("AtkValue") == null || element.getAttribute("AtkValue").length() == 0 ? "0"
			: element.getAttribute("AtkValue")); // 推荐战力
		Res = element.getAttribute("Res"); // 资源图
		BuffType = Integer.parseInt(element.getAttribute("BuffType") == null || element.getAttribute("BuffType").length() == 0 ? "0"
			: element.getAttribute("BuffType")); // BUFF类型 1=增益 2=减益
		String AttributeString = element.getAttribute("Attribute"); // 高难预警属性
		if (AttributeString != null && AttributeString.length() > 0) {
			String[] AttributeStrings = AttributeString.split("\\|"); 
			int[][] AttributeTemp = new int[AttributeStrings.length][] ; 
			for (int i = 0; i < AttributeStrings.length; i++) {
				String[] AttributeStrings2 = AttributeStrings[i].split(";"); 
				int[] array = new int[AttributeStrings2.length];
				for (int j = 0; j < AttributeStrings2.length; j++) {
					int temp = Integer.parseInt(AttributeStrings2[j]);	
					array[j] = temp;
				}
				AttributeTemp[i] = array;
			}
			Attribute = AttributeTemp ;			
		} else {
			Attribute = new int[][] {};
		}
		BUFFText = element.getAttribute("BUFFText"); // 高难预警文本
		BUFFRes = element.getAttribute("BUFFRes"); // 高难预警图标
		String FirstPassRewardString = element.getAttribute("FirstPassReward"); // 首通奖励
		if (FirstPassRewardString != null && FirstPassRewardString.length() > 0) {
			String[] FirstPassRewardStrings = FirstPassRewardString.split(";"); 
			int[] FirstPassRewardTemp = new int[FirstPassRewardStrings.length] ; 
			for (int i = 0; i < FirstPassRewardStrings.length; i++) {
				int temp = Integer.parseInt(FirstPassRewardStrings[i]);	
				FirstPassRewardTemp[i] = temp;
			}
			FirstPassReward = FirstPassRewardTemp ;			
		} else {
			FirstPassReward = new int[] {};
		}
		String SweepRewardString = element.getAttribute("SweepReward"); // 扫荡奖励
		if (SweepRewardString != null && SweepRewardString.length() > 0) {
			String[] SweepRewardStrings = SweepRewardString.split(";"); 
			int[] SweepRewardTemp = new int[SweepRewardStrings.length] ; 
			for (int i = 0; i < SweepRewardStrings.length; i++) {
				int temp = Integer.parseInt(SweepRewardStrings[i]);	
				SweepRewardTemp[i] = temp;
			}
			SweepReward = SweepRewardTemp ;			
		} else {
			SweepReward = new int[] {};
		}
		String ClearGameRewardString = element.getAttribute("ClearGameReward"); // 通关奖励
		if (ClearGameRewardString != null && ClearGameRewardString.length() > 0) {
			String[] ClearGameRewardStrings = ClearGameRewardString.split(";"); 
			int[] ClearGameRewardTemp = new int[ClearGameRewardStrings.length] ; 
			for (int i = 0; i < ClearGameRewardStrings.length; i++) {
				int temp = Integer.parseInt(ClearGameRewardStrings[i]);	
				ClearGameRewardTemp[i] = temp;
			}
			ClearGameReward = ClearGameRewardTemp ;			
		} else {
			ClearGameReward = new int[] {};
		}
	}
	

}
