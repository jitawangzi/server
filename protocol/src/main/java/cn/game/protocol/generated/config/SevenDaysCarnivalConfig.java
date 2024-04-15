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
	/** 前置条件 1：活动开启第*天 */
	public final int[] Preconditions;		
	/** 任务ID */
	public final int[] TaskID;		

	public SevenDaysCarnivalConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		String PreconditionsString = element.getAttribute("Preconditions"); // 前置条件 1：活动开启第*天
		if (PreconditionsString != null && PreconditionsString.length() > 0) {
			String[] PreconditionsStrings = PreconditionsString.split(";"); 
			int[] PreconditionsTemp = new int[PreconditionsStrings.length] ; 
			for (int i = 0; i < PreconditionsStrings.length; i++) {
				int temp = Integer.parseInt(PreconditionsStrings[i]);	
				PreconditionsTemp[i] = temp;
			}
			Preconditions = PreconditionsTemp ;			
		} else {
			Preconditions = new int[] {};
		}
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
