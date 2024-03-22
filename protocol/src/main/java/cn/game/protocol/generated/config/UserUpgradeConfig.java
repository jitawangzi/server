package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 升级
 * 
 * 工具生成的，不要手动修改
 */
 public class UserUpgradeConfig extends ExpConfig {

	/** 体力上限 */
	public final int StaminaMax;		
	/** 升级奖励ID */
	public final int LvRewardID;		

	public UserUpgradeConfig (Element element) throws Exception {
	
		super(element);
		StaminaMax = Integer.parseInt(element.getAttribute("StaminaMax") == null || element.getAttribute("StaminaMax").length() == 0 ? "0"
			: element.getAttribute("StaminaMax")); // 体力上限
		LvRewardID = Integer.parseInt(element.getAttribute("LvRewardID") == null || element.getAttribute("LvRewardID").length() == 0 ? "0"
			: element.getAttribute("LvRewardID")); // 升级奖励ID
	}
	

}
