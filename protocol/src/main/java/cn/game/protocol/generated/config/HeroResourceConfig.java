package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 武将资源
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroResourceConfig {

	/** 资源id  被以下表调用： Hero表 Monster表 */
	public final int ID;		
	/** 资源类型 1-英雄 2-怪物 3-小头目（在小怪中随机出现） 4-大BOSS（头上N个血条，进入会有boss来袭） */
	public final int Type;		
	/** 资源路径 */
	public final String prefab;		
	/** 头像Icon 文件名  出现在图鉴中 */
	public final String Icon;		
	/** 出生 动作 */
	public final String StartAction;		
	/** 休闲待机 动作 */
	public final String StandbyAction;		
	/** 技能攻击 动作1 */
	public final String AttackAction1;		

	public HeroResourceConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 资源id  被以下表调用： Hero表 Monster表
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 资源类型 1-英雄 2-怪物 3-小头目（在小怪中随机出现） 4-大BOSS（头上N个血条，进入会有boss来袭）
		prefab = element.getAttribute("prefab"); // 资源路径
		Icon = element.getAttribute("Icon"); // 头像Icon 文件名  出现在图鉴中
		StartAction = element.getAttribute("StartAction"); // 出生 动作
		StandbyAction = element.getAttribute("StandbyAction"); // 休闲待机 动作
		AttackAction1 = element.getAttribute("AttackAction1"); // 技能攻击 动作1
	}
	

}
