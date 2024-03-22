package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 属性系数表
 * 
 * 工具生成的，不要手动修改
 */
 public class AttrEffectCoefficientConfig {

	/** ID */
	private final int ID;		
	/** 总战力系数 需要/10000用 */
	private final int TotalCombat;		
	/** 暴击率上线阈值Max 需要/10000用 */
	private final int CriticalHitRate;		
	/** 暴击伤害加成上限阀值max 需要/10000用 */
	private final int CriticalDamage;		
	/** 初始治疗数值 */
	private final int InitialTreatment;		
	/** 攻击治疗转换系数 需要/10000用 */
	private final int TherapyConversion;		

	public AttrEffectCoefficientConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		this.TotalCombat = Integer.parseInt(element.getAttribute("TotalCombat") == null || element.getAttribute("TotalCombat").length() == 0 ? "0"
			: element.getAttribute("TotalCombat")); // 总战力系数 需要/10000用
		this.CriticalHitRate = Integer.parseInt(element.getAttribute("CriticalHitRate") == null || element.getAttribute("CriticalHitRate").length() == 0 ? "0"
			: element.getAttribute("CriticalHitRate")); // 暴击率上线阈值Max 需要/10000用
		this.CriticalDamage = Integer.parseInt(element.getAttribute("CriticalDamage") == null || element.getAttribute("CriticalDamage").length() == 0 ? "0"
			: element.getAttribute("CriticalDamage")); // 暴击伤害加成上限阀值max 需要/10000用
		this.InitialTreatment = Integer.parseInt(element.getAttribute("InitialTreatment") == null || element.getAttribute("InitialTreatment").length() == 0 ? "0"
			: element.getAttribute("InitialTreatment")); // 初始治疗数值
		this.TherapyConversion = Integer.parseInt(element.getAttribute("TherapyConversion") == null || element.getAttribute("TherapyConversion").length() == 0 ? "0"
			: element.getAttribute("TherapyConversion")); // 攻击治疗转换系数 需要/10000用
	}
	
	public int getID() {
		return ID;
	}
	
	public int getTotalCombat() {
		return TotalCombat;
	}
	
	public int getCriticalHitRate() {
		return CriticalHitRate;
	}
	
	public int getCriticalDamage() {
		return CriticalDamage;
	}
	
	public int getInitialTreatment() {
		return InitialTreatment;
	}
	
	public int getTherapyConversion() {
		return TherapyConversion;
	}
	
}
