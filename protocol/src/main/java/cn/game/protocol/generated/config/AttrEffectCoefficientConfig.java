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
	/** 初始暴击率 英雄和怪物一致 需要/10000用 */
	public final int InitialCriticalStrike;		
	/** 暴击率上线阈值Max 需要/10000用 */
	public final int CriticalHitRate;		
	/** 初始暴击倍数 需要/10000用 */
	public final int InitialCriticalStrikeDamage;		
	/** 暴击倍数上限 需要/10000用 */
	public final int InitialCriticalDamageLimit;		
	/** 主角&龙 初始攻击速度 （毫秒） */
	public final int InitialAttackSpeed;		
	/** 怪物 初始攻击速度 （毫秒） */
	public final int MonsterAttackSpeed;		
	/** 怪物移动速度 （毫秒） */
	public final int InitialMovementSpeed;		
	/** 初始治疗数值 */
	public final int InitialTreatment;		
	/** 攻击治疗转换系数 需要/10000用 */
	public final int TherapyConversion;		
	/** 统一小怪、头目 击退位移（像素） */
	public final int Defeat;		
	/** 城池生命初始值 */
	public final int CityWallHp;		
	/** 龙继承主角伤害百分比 需要/10000用 */
	public final int DragonInheritance;		
	/** 额外爆炸伤害初始值 */
	public final int ExtraExplosion;		
	/** 额外精神伤害初始值 */
	public final int ExtraSpirit;		
	/** 额外中毒伤害初始值 */
	public final int AdditionalPoisoning;		

	public AttrEffectCoefficientConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		TotalCombat = Integer.parseInt(element.getAttribute("TotalCombat") == null || element.getAttribute("TotalCombat").length() == 0 ? "0"
			: element.getAttribute("TotalCombat")); // 总战力系数 需要/10000用
		InitialCriticalStrike = Integer.parseInt(element.getAttribute("InitialCriticalStrike") == null || element.getAttribute("InitialCriticalStrike").length() == 0 ? "0"
			: element.getAttribute("InitialCriticalStrike")); // 初始暴击率 英雄和怪物一致 需要/10000用
		CriticalHitRate = Integer.parseInt(element.getAttribute("CriticalHitRate") == null || element.getAttribute("CriticalHitRate").length() == 0 ? "0"
			: element.getAttribute("CriticalHitRate")); // 暴击率上线阈值Max 需要/10000用
		InitialCriticalStrikeDamage = Integer.parseInt(element.getAttribute("InitialCriticalStrikeDamage") == null || element.getAttribute("InitialCriticalStrikeDamage").length() == 0 ? "0"
			: element.getAttribute("InitialCriticalStrikeDamage")); // 初始暴击倍数 需要/10000用
		InitialCriticalDamageLimit = Integer.parseInt(element.getAttribute("InitialCriticalDamageLimit") == null || element.getAttribute("InitialCriticalDamageLimit").length() == 0 ? "0"
			: element.getAttribute("InitialCriticalDamageLimit")); // 暴击倍数上限 需要/10000用
		InitialAttackSpeed = Integer.parseInt(element.getAttribute("InitialAttackSpeed") == null || element.getAttribute("InitialAttackSpeed").length() == 0 ? "0"
			: element.getAttribute("InitialAttackSpeed")); // 主角&龙 初始攻击速度 （毫秒）
		MonsterAttackSpeed = Integer.parseInt(element.getAttribute("MonsterAttackSpeed") == null || element.getAttribute("MonsterAttackSpeed").length() == 0 ? "0"
			: element.getAttribute("MonsterAttackSpeed")); // 怪物 初始攻击速度 （毫秒）
		InitialMovementSpeed = Integer.parseInt(element.getAttribute("InitialMovementSpeed") == null || element.getAttribute("InitialMovementSpeed").length() == 0 ? "0"
			: element.getAttribute("InitialMovementSpeed")); // 怪物移动速度 （毫秒）
		InitialTreatment = Integer.parseInt(element.getAttribute("InitialTreatment") == null || element.getAttribute("InitialTreatment").length() == 0 ? "0"
			: element.getAttribute("InitialTreatment")); // 初始治疗数值
		TherapyConversion = Integer.parseInt(element.getAttribute("TherapyConversion") == null || element.getAttribute("TherapyConversion").length() == 0 ? "0"
			: element.getAttribute("TherapyConversion")); // 攻击治疗转换系数 需要/10000用
		Defeat = Integer.parseInt(element.getAttribute("Defeat") == null || element.getAttribute("Defeat").length() == 0 ? "0"
			: element.getAttribute("Defeat")); // 统一小怪、头目 击退位移（像素）
		CityWallHp = Integer.parseInt(element.getAttribute("CityWallHp") == null || element.getAttribute("CityWallHp").length() == 0 ? "0"
			: element.getAttribute("CityWallHp")); // 城池生命初始值
		DragonInheritance = Integer.parseInt(element.getAttribute("DragonInheritance") == null || element.getAttribute("DragonInheritance").length() == 0 ? "0"
			: element.getAttribute("DragonInheritance")); // 龙继承主角伤害百分比 需要/10000用
		ExtraExplosion = Integer.parseInt(element.getAttribute("ExtraExplosion") == null || element.getAttribute("ExtraExplosion").length() == 0 ? "0"
			: element.getAttribute("ExtraExplosion")); // 额外爆炸伤害初始值
		ExtraSpirit = Integer.parseInt(element.getAttribute("ExtraSpirit") == null || element.getAttribute("ExtraSpirit").length() == 0 ? "0"
			: element.getAttribute("ExtraSpirit")); // 额外精神伤害初始值
		AdditionalPoisoning = Integer.parseInt(element.getAttribute("AdditionalPoisoning") == null || element.getAttribute("AdditionalPoisoning").length() == 0 ? "0"
			: element.getAttribute("AdditionalPoisoning")); // 额外中毒伤害初始值
	}
	

}
