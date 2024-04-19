package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 任务
 * 
 * 工具生成的，不要手动修改
 */
 public class QuestConfig {

	/** 任务ID */
	public final int ID;		
	/** 任务描述 */
	public final String Description;		
	/** 任务完成条件 */
	public final int Condition;		
	/** 任务奖励ID */
	public final int[] Reward;		
	/** 前往引导ID */
	public final int[] GuideID;		

	public QuestConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 任务ID
		Description = element.getAttribute("Description"); // 任务描述
		Condition = Integer.parseInt(element.getAttribute("Condition") == null || element.getAttribute("Condition").length() == 0 ? "0"
			: element.getAttribute("Condition")); // 任务完成条件
		String RewardString = element.getAttribute("Reward"); // 任务奖励ID
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
		String GuideIDString = element.getAttribute("GuideID"); // 前往引导ID
		if (GuideIDString != null && GuideIDString.length() > 0) {
			String[] GuideIDStrings = GuideIDString.split(";"); 
			int[] GuideIDTemp = new int[GuideIDStrings.length] ; 
			for (int i = 0; i < GuideIDStrings.length; i++) {
				int temp = Integer.parseInt(GuideIDStrings[i]);	
				GuideIDTemp[i] = temp;
			}
			GuideID = GuideIDTemp ;			
		} else {
			GuideID = new int[] {};
		}
	}
	

}
