package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 升级
 * 
 * 工具生成的，不要手动修改
 */
 public class UserUpgradeConfig extends ExpConfig {

	/** 等级 */
	public final int ID;		
	/** 玩家升级所需经验 */
	public final int experience;		
	/** 升级奖励ID */
	public final int LvRewardID;		

	public UserUpgradeConfig (Element element) throws Exception {
	
		super(element);
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 等级
		experience = Integer.parseInt(element.getAttribute("experience") == null || element.getAttribute("experience").length() == 0 ? "0"
			: element.getAttribute("experience")); // 玩家升级所需经验
		LvRewardID = Integer.parseInt(element.getAttribute("LvRewardID") == null || element.getAttribute("LvRewardID").length() == 0 ? "0"
			: element.getAttribute("LvRewardID")); // 升级奖励ID
	}
	

}
