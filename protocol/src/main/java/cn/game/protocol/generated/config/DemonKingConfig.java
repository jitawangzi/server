package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 妖王别跑
 * 
 * 工具生成的，不要手动修改
 */
 public class DemonKingConfig {

	/** ID */
	public final int ID;		
	/** 战役ID */
	public final int BattleID;		
	/** 修为限制 */
	public final int Condition;		
	/** 仙友加成文本 */
	public final String XYText;		
	/** 仙友加成属性 */
	public final int[] XYAttribute;		
	/** 普通奖励 */
	public final int[] Reward;		
	/** 首通奖励 */
	public final int[] FirstPassReward;		
	/** BOSS形象资源 */
	public final String BOSSRes;		

	public DemonKingConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		BattleID = Integer.parseInt(element.getAttribute("BattleID") == null || element.getAttribute("BattleID").length() == 0 ? "0"
			: element.getAttribute("BattleID")); // 战役ID
		Condition = Integer.parseInt(element.getAttribute("Condition") == null || element.getAttribute("Condition").length() == 0 ? "0"
			: element.getAttribute("Condition")); // 修为限制
		XYText = element.getAttribute("XYText"); // 仙友加成文本
		String XYAttributeString = element.getAttribute("XYAttribute"); // 仙友加成属性
		if (XYAttributeString != null && XYAttributeString.length() > 0) {
			String[] XYAttributeStrings = XYAttributeString.split(";"); 
			int[] XYAttributeTemp = new int[XYAttributeStrings.length] ; 
			for (int i = 0; i < XYAttributeStrings.length; i++) {
				int temp = Integer.parseInt(XYAttributeStrings[i]);	
				XYAttributeTemp[i] = temp;
			}
			XYAttribute = XYAttributeTemp ;			
		} else {
			XYAttribute = new int[] {};
		}
		String RewardString = element.getAttribute("Reward"); // 普通奖励
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
		BOSSRes = element.getAttribute("BOSSRes"); // BOSS形象资源
	}
	

}
