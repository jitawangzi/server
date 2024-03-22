package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 属性Id表
 * 
 * 工具生成的，不要手动修改
 */
 public class AttrEffectConfigConfig {

	/** ID */
	private final int ID;		
	/** 游戏属性名称 */
	private final String AttributeName;		
	/** 1-数值 2-百分比 */
	private final int AttributeType;		
	/** 战力系数 */
	private final int CombatEffectiveness;		

	public AttrEffectConfigConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		this.AttributeName = element.getAttribute("AttributeName"); // 游戏属性名称
		this.AttributeType = Integer.parseInt(element.getAttribute("AttributeType") == null || element.getAttribute("AttributeType").length() == 0 ? "0"
			: element.getAttribute("AttributeType")); // 1-数值 2-百分比
		this.CombatEffectiveness = Integer.parseInt(element.getAttribute("CombatEffectiveness") == null || element.getAttribute("CombatEffectiveness").length() == 0 ? "0"
			: element.getAttribute("CombatEffectiveness")); // 战力系数
	}
	
	public int getID() {
		return ID;
	}
	
	public String getAttributeName() {
		return AttributeName;
	}
	
	public int getAttributeType() {
		return AttributeType;
	}
	
	public int getCombatEffectiveness() {
		return CombatEffectiveness;
	}
	
}
