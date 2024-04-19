package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 资源总表
 * 
 * 工具生成的，不要手动修改
 */
 public class ArtResourceConfig {

	/** 资源id #Hero表 */
	public final int ID;		
	/** 资源类型 1-英雄 */
	public final int Type;		
	/** 头像Icon */
	public final String Icon;		
	/** 出生 动作 */
	public final String StartAction;		
	/** 休闲待机 动作 */
	public final String StandbyAction;		
	/** 技能攻击 动作 */
	public final String AttackAction;		
	/** 受击 动作 */
	public final String AffecteAction;		
	/** 死亡 动作 */
	public final String DeathAction;		
	/** 休闲待机 特效 */
	public final String LeisureEffect;		
	/** 技能攻击 特效 */
	public final String AttackEffect;		
	/** 受击 特效 */
	public final String AffecteEffect;		
	/** 死亡 动作 */
	public final String DeathEffect;		
	/** 气泡 配置 */
	public final String Bubble;		

	public ArtResourceConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 资源id #Hero表
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 资源类型 1-英雄
		Icon = element.getAttribute("Icon"); // 头像Icon
		StartAction = element.getAttribute("StartAction"); // 出生 动作
		StandbyAction = element.getAttribute("StandbyAction"); // 休闲待机 动作
		AttackAction = element.getAttribute("AttackAction"); // 技能攻击 动作
		AffecteAction = element.getAttribute("AffecteAction"); // 受击 动作
		DeathAction = element.getAttribute("DeathAction"); // 死亡 动作
		LeisureEffect = element.getAttribute("LeisureEffect"); // 休闲待机 特效
		AttackEffect = element.getAttribute("AttackEffect"); // 技能攻击 特效
		AffecteEffect = element.getAttribute("AffecteEffect"); // 受击 特效
		DeathEffect = element.getAttribute("DeathEffect"); // 死亡 动作
		Bubble = element.getAttribute("Bubble"); // 气泡 配置
	}
	

}
