package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 七日狂欢
 * 
 * 工具生成的，不要手动修改
 */
 public class SevenDaysCarnivalConfig {

	/** ID */
	public final int ID;		
	/** 类型 */
	public final int Type;		
	/** 天数 */
	public final int Day;		
	/** 任务ID */
	public final int[] TaskID;		

	public SevenDaysCarnivalConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 类型
		Day = Integer.parseInt(element.getAttribute("Day") == null || element.getAttribute("Day").length() == 0 ? "0"
			: element.getAttribute("Day")); // 天数
		String TaskIDString = element.getAttribute("TaskID"); // 任务ID
		if (TaskIDString != null && TaskIDString.length() > 0) {
			String[] TaskIDStrings = TaskIDString.split(";"); 
			int[] TaskIDTemp = new int[TaskIDStrings.length] ; 
			for (int i = 0; i < TaskIDStrings.length; i++) {
				int temp = Integer.parseInt(TaskIDStrings[i]);	
				TaskIDTemp[i] = temp;
			}
			TaskID = TaskIDTemp ;			
		} else {
			TaskID = new int[] {};
		}
	}
	

}
