package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 失落真经
 * 
 * 工具生成的，不要手动修改
 */
 public class LostScripturesConfig {

	/** ID */
	public final int ID;		
	/** 关卡名称 */
	public final String Name;		
	/** 战役ID */
	public final int BattleID;		
	/** 前置ID */
	public final int PreBattle;		
	/** 推荐战力 */
	public final int AtkValue;		
	/** 类型 1=普通 2=精英 */
	public final int Type;		
	/** 头像框 */
	public final String AvatarFrame;		
	/** 头像资源 */
	public final String Avatar;		
	/** BUFF类型 1=增益 2=减益 */
	public final int BuffType;		
	/** BUFF属性 */
	public final int[][] Attribute;		
	/** BUFF文本 */
	public final int BUFFText;		
	/** BUFF图标 */
	public final String BUFFRes;		
	/** 首通奖励 */
	public final int[] FirstPassReward;		
	/** 每日奖励 */
	public final int[] Reward;		

	public LostScripturesConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Name = element.getAttribute("Name"); // 关卡名称
		BattleID = Integer.parseInt(element.getAttribute("BattleID") == null || element.getAttribute("BattleID").length() == 0 ? "0"
			: element.getAttribute("BattleID")); // 战役ID
		PreBattle = Integer.parseInt(element.getAttribute("PreBattle") == null || element.getAttribute("PreBattle").length() == 0 ? "0"
			: element.getAttribute("PreBattle")); // 前置ID
		AtkValue = Integer.parseInt(element.getAttribute("AtkValue") == null || element.getAttribute("AtkValue").length() == 0 ? "0"
			: element.getAttribute("AtkValue")); // 推荐战力
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 类型 1=普通 2=精英
		AvatarFrame = element.getAttribute("AvatarFrame"); // 头像框
		Avatar = element.getAttribute("Avatar"); // 头像资源
		BuffType = Integer.parseInt(element.getAttribute("BuffType") == null || element.getAttribute("BuffType").length() == 0 ? "0"
			: element.getAttribute("BuffType")); // BUFF类型 1=增益 2=减益
		String AttributeString = element.getAttribute("Attribute"); // BUFF属性
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
		BUFFText = Integer.parseInt(element.getAttribute("BUFFText") == null || element.getAttribute("BUFFText").length() == 0 ? "0"
			: element.getAttribute("BUFFText")); // BUFF文本
		BUFFRes = element.getAttribute("BUFFRes"); // BUFF图标
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
		String RewardString = element.getAttribute("Reward"); // 每日奖励
		if (RewardString != null && RewardString.length() > 0) {
			String[] RewardStrings = RewardString.split(";"); 
			int[] RewardTemp = new int[RewardStrings.length] ; 
			for (int i = 0; i < RewardStrings.length; i++) {
				int temp = Integer.parseInt(RewardStrings[i]);	
				RewardTemp[i] = temp;
			}
			Reward = RewardTemp ;			
		} else {
			Reward = new int[] {};
		}
	}
	

}
