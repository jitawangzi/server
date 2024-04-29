package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 资源总表
 * 
 * 工具生成的，不要手动修改
 */
 public class ArtResourceConfig {

	/** 300-唯一   310-永恒 320-彩      330-红 340-金      350-紫 360-蓝      370-绿 380-白      390-没有品质（怪专用） */
	public final int ID;		
	/** 资源类型 1-英雄 2-怪物 */
	public final int Type;		
	/** spine预制体 */
	public final String Prefab;		
	/** 头像Icon 调用：west\src\First_party\art\xiyou UI\卡牌_card \west\src\First_party\art\xiyou UI\head_头像 */
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
	/** 移动动作（怪） */
	public final String MoveAct;		
	/** 休闲待机 特效 */
	public final String LeisureEffect;		
	/** 技能攻击 特效 */
	public final String AttackEffect;		
	/** 受击 特效 */
	public final String AffecteEffect;		
	/** 死亡 动作 */
	public final String DeathEffect;		
	/** 移动特效（怪） */
	public final String MoveEffect;		
	/** 出生特效（怪） */
	public final String BornEffect;		
	/** 气泡 配置 */
	public final String Bubble;		

	public ArtResourceConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 300-唯一   310-永恒 320-彩      330-红 340-金      350-紫 360-蓝      370-绿 380-白      390-没有品质（怪专用）
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 资源类型 1-英雄 2-怪物
		Prefab = element.getAttribute("Prefab"); // spine预制体
		Icon = element.getAttribute("Icon"); // 头像Icon 调用：west\src\First_party\art\xiyou UI\卡牌_card \west\src\First_party\art\xiyou UI\head_头像
		StartAction = element.getAttribute("StartAction"); // 出生 动作
		StandbyAction = element.getAttribute("StandbyAction"); // 休闲待机 动作
		AttackAction = element.getAttribute("AttackAction"); // 技能攻击 动作
		AffecteAction = element.getAttribute("AffecteAction"); // 受击 动作
		DeathAction = element.getAttribute("DeathAction"); // 死亡 动作
		MoveAct = element.getAttribute("MoveAct"); // 移动动作（怪）
		LeisureEffect = element.getAttribute("LeisureEffect"); // 休闲待机 特效
		AttackEffect = element.getAttribute("AttackEffect"); // 技能攻击 特效
		AffecteEffect = element.getAttribute("AffecteEffect"); // 受击 特效
		DeathEffect = element.getAttribute("DeathEffect"); // 死亡 动作
		MoveEffect = element.getAttribute("MoveEffect"); // 移动特效（怪）
		BornEffect = element.getAttribute("BornEffect"); // 出生特效（怪）
		Bubble = element.getAttribute("Bubble"); // 气泡 配置
	}
	

}
