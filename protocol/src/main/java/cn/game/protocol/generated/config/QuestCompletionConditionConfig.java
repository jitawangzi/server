package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 任务完成条件
 * 
 * 工具生成的，不要手动修改
 */
 public class QuestCompletionConditionConfig {

	/** ID */
	public final int ID;		
	/** 条件类型 */
	public final String Description;		
	/** 条件参数 */
	public final String Condition;		
	/** 说明 */
	public final String Parameter;		

	public QuestCompletionConditionConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Description = element.getAttribute("Description"); // 条件类型
		Condition = element.getAttribute("Condition"); // 条件参数
		Parameter = element.getAttribute("Parameter"); // 说明
	}
	

}
