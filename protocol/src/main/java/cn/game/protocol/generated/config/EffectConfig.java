package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 效果
 * 
 * 工具生成的，不要手动修改
 */
 public class EffectConfig {

	/** ID */
	private final int ID;		
	/** 触发条件 1、攻击次数 配置：1;7 */
	private final int[] TriggerCondition;		
	/** 效果持续时间  毫秒 */
	private final int Duration;		

	public EffectConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		String TriggerConditionString = element.getAttribute("TriggerCondition"); // 触发条件 1、攻击次数 配置：1;7
		if (TriggerConditionString != null && TriggerConditionString.length() > 0) {
			String[] TriggerConditionStrings = TriggerConditionString.split(";"); 
			int[] TriggerCondition = new int[TriggerConditionStrings.length] ; 
			for (int i = 0; i < TriggerConditionStrings.length; i++) {
				int temp = Integer.parseInt(TriggerConditionStrings[i]);
				TriggerCondition[i] = temp;
			}
			this.TriggerCondition = TriggerCondition ;			
		} else {
			this.TriggerCondition = new int[] {};
		}
		this.Duration = Integer.parseInt(element.getAttribute("Duration") == null || element.getAttribute("Duration").length() == 0 ? "0"
			: element.getAttribute("Duration")); // 效果持续时间  毫秒
	}
	
	public int getID() {
		return ID;
	}
	
	public int[] getTriggerCondition() {
		return TriggerCondition;
	}
	
	public int getDuration() {
		return Duration;
	}
	
}
