package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 龙技能
 * 
 * 工具生成的，不要手动修改
 */
 public class DragonSkillConfig {

	/** 龙id */
	public final int ID;		
	/** 龙技能名称 */
	public final String Name;		
	/** 龙技能tips */
	public final String Tips;		
	/** 图标Icon 文件名  同技能图标 */
	public final String Icon;		

	public DragonSkillConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 龙id
		Name = element.getAttribute("Name"); // 龙技能名称
		Tips = element.getAttribute("Tips"); // 龙技能tips
		Icon = element.getAttribute("Icon"); // 图标Icon 文件名  同技能图标
	}
	

}
