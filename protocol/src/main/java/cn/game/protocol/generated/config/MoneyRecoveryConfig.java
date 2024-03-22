package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 资源按时间恢复
 * 
 * 工具生成的，不要手动修改
 */
 public class MoneyRecoveryConfig {

	/** 物品ID */
	public final int ID;		
	/** 多少分钟恢复1点 */
	public final int interval;		
	/** 自动恢复的上限 */
	public final int max;		
	/** 额外增加的上限类型: 1月卡 */
	public final int maxType;		

	public MoneyRecoveryConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 物品ID
		interval = Integer.parseInt(element.getAttribute("interval") == null || element.getAttribute("interval").length() == 0 ? "0"
			: element.getAttribute("interval")); // 多少分钟恢复1点
		max = Integer.parseInt(element.getAttribute("max") == null || element.getAttribute("max").length() == 0 ? "0"
			: element.getAttribute("max")); // 自动恢复的上限
		maxType = Integer.parseInt(element.getAttribute("maxType") == null || element.getAttribute("maxType").length() == 0 ? "0"
			: element.getAttribute("maxType")); // 额外增加的上限类型: 1月卡
	}
	

}
