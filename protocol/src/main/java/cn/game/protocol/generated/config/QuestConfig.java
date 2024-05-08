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
	/** 任务类型：QuestTypeEnum表id */
	public final int Type;		
	/** 任务完成条件：Condition表ID */
	public final int Condition;		
	/** 任务奖励ID */
	public final int Reward;		
	/** 分组，暂时保留 */
	public final int Group;		
	/** 当任务完成时 开启的新任务id */
	public final int[] OpenQuests;		
	/** 当任务完成时 是否删除这个任务，不再显示 */
	public final boolean IsDeleteOnFinish;		
	/** 前往引导ID */
	public final int[] GuideID;		

	public QuestConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 任务ID
		Description = element.getAttribute("Description"); // 任务描述
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 任务类型：QuestTypeEnum表id
		Condition = Integer.parseInt(element.getAttribute("Condition") == null || element.getAttribute("Condition").length() == 0 ? "0"
			: element.getAttribute("Condition")); // 任务完成条件：Condition表ID
		Reward = Integer.parseInt(element.getAttribute("Reward") == null || element.getAttribute("Reward").length() == 0 ? "0"
			: element.getAttribute("Reward")); // 任务奖励ID
		Group = Integer.parseInt(element.getAttribute("Group") == null || element.getAttribute("Group").length() == 0 ? "0"
			: element.getAttribute("Group")); // 分组，暂时保留
		String OpenQuestsString = element.getAttribute("OpenQuests"); // 当任务完成时 开启的新任务id
		if (OpenQuestsString != null && OpenQuestsString.length() > 0) {
			String[] OpenQuestsStrings = OpenQuestsString.split(";"); 
			int[] OpenQuestsTemp = new int[OpenQuestsStrings.length] ; 
			for (int i = 0; i < OpenQuestsStrings.length; i++) {
				int temp = Integer.parseInt(OpenQuestsStrings[i]);	
				OpenQuestsTemp[i] = temp;
			}
			OpenQuests = OpenQuestsTemp ;			
		} else {
			OpenQuests = new int[] {};
		}
		IsDeleteOnFinish = Boolean.parseBoolean(element.getAttribute("IsDeleteOnFinish") == null || element.getAttribute("IsDeleteOnFinish").length() == 0 ? "false"
			: element.getAttribute("IsDeleteOnFinish")); // 当任务完成时 是否删除这个任务，不再显示
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
