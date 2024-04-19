package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 武将资源
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroResourceConfig {

	/** 资源id */
	public final int ID;		
	/** 资源类型 1=英雄 */
	public final int Type;		
	/** 资源路径 */
	public final String prefab;		
	/** 头像Icon */
	public final String Icon;		
	/** 出生 动作 */
	public final String StartAction;		
	/** 休闲待机 动作 */
	public final String StandbyAction;		
	/** 技能攻击 动作1 */
	public final String AttackAction1;		
	/** 技能攻击 动作2 */
	public final String AttackAction2;		
	/** 受击 动作 */
	public final String AffecteAction;		
	/** 死亡 动作 */
	public final String DeathAction;		
	/** 出生 特效 */
	public final String StartEffect;		
	/** 休闲待机 特效 */
	public final String StandbyEffect;		
	/** 受击 特效 */
	public final String AffecteEffect;		
	/** 死亡 特效 */
	public final String DeathEffect;		
	/** 气泡 配置 */
	public final String Bubble;		

	public HeroResourceConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 资源id
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 资源类型 1=英雄
		prefab = element.getAttribute("prefab"); // 资源路径
		Icon = element.getAttribute("Icon"); // 头像Icon
		StartAction = element.getAttribute("StartAction"); // 出生 动作
		StandbyAction = element.getAttribute("StandbyAction"); // 休闲待机 动作
		AttackAction1 = element.getAttribute("AttackAction1"); // 技能攻击 动作1
		AttackAction2 = element.getAttribute("AttackAction2"); // 技能攻击 动作2
		AffecteAction = element.getAttribute("AffecteAction"); // 受击 动作
		DeathAction = element.getAttribute("DeathAction"); // 死亡 动作
		StartEffect = element.getAttribute("StartEffect"); // 出生 特效
		StandbyEffect = element.getAttribute("StandbyEffect"); // 休闲待机 特效
		AffecteEffect = element.getAttribute("AffecteEffect"); // 受击 特效
		DeathEffect = element.getAttribute("DeathEffect"); // 死亡 特效
		Bubble = element.getAttribute("Bubble"); // 气泡 配置
	}
	

}
