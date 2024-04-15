package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 成就
 * 
 * 工具生成的，不要手动修改
 */
 public class AccomplishmentConfig {

	/** ID */
	public final int ID;		
	/** 成就组 */
	public final int AchievementGroup;		
	/** 出现顺序 */
	public final int Order;		
	/** 任务ID */
	public final int TaskID;		

	public AccomplishmentConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		AchievementGroup = Integer.parseInt(element.getAttribute("AchievementGroup") == null || element.getAttribute("AchievementGroup").length() == 0 ? "0"
			: element.getAttribute("AchievementGroup")); // 成就组
		Order = Integer.parseInt(element.getAttribute("Order") == null || element.getAttribute("Order").length() == 0 ? "0"
			: element.getAttribute("Order")); // 出现顺序
		TaskID = Integer.parseInt(element.getAttribute("TaskID") == null || element.getAttribute("TaskID").length() == 0 ? "0"
			: element.getAttribute("TaskID")); // 任务ID
	}
	

}
