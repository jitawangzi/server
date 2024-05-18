package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 基金通行证奖励
 * 
 * 工具生成的，不要手动修改
 */
 public class FundPassRewardsConfig {

	/** ID */
	public final int ID;		
	/** 索引 FundPass#基金通行证的ID */
	public final int Index;		
	/** 等级 */
	public final int Lv;		
	/** 解锁条件 条件表 */
	public final int Condition;		
	/** 奖励 */
	public final int[] Reward;		

	public FundPassRewardsConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Index = Integer.parseInt(element.getAttribute("Index") == null || element.getAttribute("Index").length() == 0 ? "0"
			: element.getAttribute("Index")); // 索引 FundPass#基金通行证的ID
		Lv = Integer.parseInt(element.getAttribute("Lv") == null || element.getAttribute("Lv").length() == 0 ? "0"
			: element.getAttribute("Lv")); // 等级
		Condition = Integer.parseInt(element.getAttribute("Condition") == null || element.getAttribute("Condition").length() == 0 ? "0"
			: element.getAttribute("Condition")); // 解锁条件 条件表
		String RewardString = element.getAttribute("Reward"); // 奖励
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
