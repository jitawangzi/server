package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 升级
 * 
 * 工具生成的，不要手动修改
 */
 public class UserUpgradeConfig extends ExpConfig {

	/** 升级奖励ID */
	public final int LvRewardID;		

	public UserUpgradeConfig (Element element) throws Exception {
	
		super(element);
		LvRewardID = Integer.parseInt(element.getAttribute("LvRewardID") == null || element.getAttribute("LvRewardID").length() == 0 ? "0"
			: element.getAttribute("LvRewardID")); // 升级奖励ID
	}
	

}
