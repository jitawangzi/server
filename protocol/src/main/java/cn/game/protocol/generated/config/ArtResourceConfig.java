package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 资源总表
 * 
 * 工具生成的，不要手动修改
 */
 public class ArtResourceConfig {

	/** 资源id  被以下表调用： Hero表 Monster表 */
	private final int ID;		
	/** 资源类型 1-英雄 2-怪物 3-小头目（在小怪中随机出现） 4-大BOSS（头上N个血条，进入会有boss来袭） */
	private final int Type;		
	/** 头像Icon 文件名  出现在图鉴中 */
	private final String Icon;		
	/** 出生 动作 */
	private final String StartAction;		
	/** 休闲待机 动作 */
	private final String StandbyAction;		
	/** 技能攻击 动作 */
	private final String AttackAction;		
	/** 受击 动作 */
	private final String AffecteAction;		
	/** 死亡 动作 */
	private final String DeathAction;		
	/** 休闲待机 特效 */
	private final String LeisureEffect;		
	/** 技能攻击 特效 */
	private final String AttackEffect;		
	/** 受击 特效 */
	private final String AffecteEffect;		
	/** 死亡 动作 */
	private final String DeathEffect;		
	/** 气泡 配置 */
	private final String Bubble;		

	public ArtResourceConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 资源id  被以下表调用： Hero表 Monster表
		this.Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 资源类型 1-英雄 2-怪物 3-小头目（在小怪中随机出现） 4-大BOSS（头上N个血条，进入会有boss来袭）
		this.Icon = element.getAttribute("Icon"); // 头像Icon 文件名  出现在图鉴中
		this.StartAction = element.getAttribute("StartAction"); // 出生 动作
		this.StandbyAction = element.getAttribute("StandbyAction"); // 休闲待机 动作
		this.AttackAction = element.getAttribute("AttackAction"); // 技能攻击 动作
		this.AffecteAction = element.getAttribute("AffecteAction"); // 受击 动作
		this.DeathAction = element.getAttribute("DeathAction"); // 死亡 动作
		this.LeisureEffect = element.getAttribute("LeisureEffect"); // 休闲待机 特效
		this.AttackEffect = element.getAttribute("AttackEffect"); // 技能攻击 特效
		this.AffecteEffect = element.getAttribute("AffecteEffect"); // 受击 特效
		this.DeathEffect = element.getAttribute("DeathEffect"); // 死亡 动作
		this.Bubble = element.getAttribute("Bubble"); // 气泡 配置
	}
	
	public int getID() {
		return ID;
	}
	
	public int getType() {
		return Type;
	}
	
	public String getIcon() {
		return Icon;
	}
	
	public String getStartAction() {
		return StartAction;
	}
	
	public String getStandbyAction() {
		return StandbyAction;
	}
	
	public String getAttackAction() {
		return AttackAction;
	}
	
	public String getAffecteAction() {
		return AffecteAction;
	}
	
	public String getDeathAction() {
		return DeathAction;
	}
	
	public String getLeisureEffect() {
		return LeisureEffect;
	}
	
	public String getAttackEffect() {
		return AttackEffect;
	}
	
	public String getAffecteEffect() {
		return AffecteEffect;
	}
	
	public String getDeathEffect() {
		return DeathEffect;
	}
	
	public String getBubble() {
		return Bubble;
	}
	
}
