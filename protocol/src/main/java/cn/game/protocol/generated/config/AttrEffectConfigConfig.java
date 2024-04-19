package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 属性Id表
 * 
 * 工具生成的，不要手动修改
 */
 public class AttrEffectConfigConfig {

	/** 属性ID */
	public final int ID;		
	/** 属性名称 */
	public final String AttributeName;		
	/** 1-数值 2-百分比 */
	public final int AttributeType;		
	/** 战力系数 需要/10000用 */
	public final int CombatEffectiveness;		
	/** 策划属性名称 */
	public final String Name;		

	public AttrEffectConfigConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 属性ID
		AttributeName = element.getAttribute("AttributeName"); // 属性名称
		AttributeType = Integer.parseInt(element.getAttribute("AttributeType") == null || element.getAttribute("AttributeType").length() == 0 ? "0"
			: element.getAttribute("AttributeType")); // 1-数值 2-百分比
		CombatEffectiveness = Integer.parseInt(element.getAttribute("CombatEffectiveness") == null || element.getAttribute("CombatEffectiveness").length() == 0 ? "0"
			: element.getAttribute("CombatEffectiveness")); // 战力系数 需要/10000用
		Name = element.getAttribute("Name"); // 策划属性名称
	}
	

}
