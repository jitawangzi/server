package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 升级
 * 
 * 工具生成的，不要手动修改
 */
 public class FundPassUpgradeConfig extends ExpConfig {

	/** 等级 */
	public final int ID;		
	/** 玩家升级所需经验类型 */
	public final int ExpType;		
	/** 玩家升级所需经验值 */
	public final int experience;		

	public FundPassUpgradeConfig (Element element) throws Exception {
	
		super(element);
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 等级
		ExpType = Integer.parseInt(element.getAttribute("ExpType") == null || element.getAttribute("ExpType").length() == 0 ? "0"
			: element.getAttribute("ExpType")); // 玩家升级所需经验类型
		experience = Integer.parseInt(element.getAttribute("experience") == null || element.getAttribute("experience").length() == 0 ? "0"
			: element.getAttribute("experience")); // 玩家升级所需经验值
	}
	

}
