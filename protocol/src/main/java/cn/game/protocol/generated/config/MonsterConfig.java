package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 怪物属性
 * 
 * 工具生成的，不要手动修改
 */
 public class MonsterConfig {

	/** 怪物ID 怪物id必须1打头 10NN-表示章节 第5位是小关 第6位流水号 */
	public final int ID;		
	/** 1-英雄 2-小怪 3-头目 4-boss */
	public final int Type;		
	/** 移动速度  正常填40 快一些填60 */
	public final int MoveSpeed;		
	/** 攻击速度 填2=2秒1次攻击 */
	public final int AtkSpeed;		
	/** 怪物大小 填100=100% */
	public final int scale;		
	/** 生命 小怪100% 头目200% BOSS12000% */
	public final int HP;		
	/** 攻击 100% */
	public final int Attack;		
	/** 技能ID  调用HeroSkillGroup#技能组ID */
	public final int SkillID;		
	/** 美术资源id 调用ArtResource#资源总表id */
	public final int ArtResourceId;		

	public MonsterConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 怪物ID 怪物id必须1打头 10NN-表示章节 第5位是小关 第6位流水号
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 1-英雄 2-小怪 3-头目 4-boss
		MoveSpeed = Integer.parseInt(element.getAttribute("MoveSpeed") == null || element.getAttribute("MoveSpeed").length() == 0 ? "0"
			: element.getAttribute("MoveSpeed")); // 移动速度  正常填40 快一些填60
		AtkSpeed = Integer.parseInt(element.getAttribute("AtkSpeed") == null || element.getAttribute("AtkSpeed").length() == 0 ? "0"
			: element.getAttribute("AtkSpeed")); // 攻击速度 填2=2秒1次攻击
		scale = Integer.parseInt(element.getAttribute("scale") == null || element.getAttribute("scale").length() == 0 ? "0"
			: element.getAttribute("scale")); // 怪物大小 填100=100%
		HP = Integer.parseInt(element.getAttribute("HP") == null || element.getAttribute("HP").length() == 0 ? "0"
			: element.getAttribute("HP")); // 生命 小怪100% 头目200% BOSS12000%
		Attack = Integer.parseInt(element.getAttribute("Attack") == null || element.getAttribute("Attack").length() == 0 ? "0"
			: element.getAttribute("Attack")); // 攻击 100%
		SkillID = Integer.parseInt(element.getAttribute("SkillID") == null || element.getAttribute("SkillID").length() == 0 ? "0"
			: element.getAttribute("SkillID")); // 技能ID  调用HeroSkillGroup#技能组ID
		ArtResourceId = Integer.parseInt(element.getAttribute("ArtResourceId") == null || element.getAttribute("ArtResourceId").length() == 0 ? "0"
			: element.getAttribute("ArtResourceId")); // 美术资源id 调用ArtResource#资源总表id
	}
	

}
