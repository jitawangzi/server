package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 抽象升级表，不用配置数据
 * 
 * 工具生成的，不要手动修改
 */
 public class ExpConfig {

	/** 等级 */
	public final int ID;		
	/** 升级所需经验 */
	public final int experience;		

	public ExpConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 等级
		experience = Integer.parseInt(element.getAttribute("experience") == null || element.getAttribute("experience").length() == 0 ? "0"
			: element.getAttribute("experience")); // 升级所需经验
	}
	

}
