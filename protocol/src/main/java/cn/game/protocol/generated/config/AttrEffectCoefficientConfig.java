package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 属性系数表
 * 
 * 工具生成的，不要手动修改
 */
 public class AttrEffectCoefficientConfig {

	/** ID */
	public final int ID;		
	/** 总战力系数 需要/10000用 */
	public final int TotalCombat;		
	/** 暴击率上限 需要/10000用 */
	public final int CriticalHitRate;		
	/** 暴击倍数上限 需要/10000用 */
	public final int InitialCriticalDamageLimit;		
	/** 伙伴继承卡牌伤害百分比 需要/10000用 */
	public final int DragonInheritance;		

	public AttrEffectCoefficientConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		TotalCombat = Integer.parseInt(element.getAttribute("TotalCombat") == null || element.getAttribute("TotalCombat").length() == 0 ? "0"
			: element.getAttribute("TotalCombat")); // 总战力系数 需要/10000用
		CriticalHitRate = Integer.parseInt(element.getAttribute("CriticalHitRate") == null || element.getAttribute("CriticalHitRate").length() == 0 ? "0"
			: element.getAttribute("CriticalHitRate")); // 暴击率上限 需要/10000用
		InitialCriticalDamageLimit = Integer.parseInt(element.getAttribute("InitialCriticalDamageLimit") == null || element.getAttribute("InitialCriticalDamageLimit").length() == 0 ? "0"
			: element.getAttribute("InitialCriticalDamageLimit")); // 暴击倍数上限 需要/10000用
		DragonInheritance = Integer.parseInt(element.getAttribute("DragonInheritance") == null || element.getAttribute("DragonInheritance").length() == 0 ? "0"
			: element.getAttribute("DragonInheritance")); // 伙伴继承卡牌伤害百分比 需要/10000用
	}
	

}
