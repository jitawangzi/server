package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 怪物属性
 * 
 * 工具生成的，不要手动修改
 */
 public class MonsterConfig {

	/** 怪物ID */
	public final int ID;		
	/** 移动速度 */
	public final int MoveSpeed;		
	/** 攻击速度 */
	public final int AtkSpeed;		
	/** 规模 */
	public final int scale;		
	/** 生命 */
	public final int HP;		
	/** 攻击 */
	public final int Attack;		
	/** 怪物资源 */
	public final String EnemyResPath;		
	/** 技能ID */
	public final int SkillID;		
	/** 出生动作 */
	public final String BornAct;		
	/** 移动动作 */
	public final String MoveAct;		
	/** 攻击动作 */
	public final String AtkAct;		
	/** 受击动作 */
	public final String HurtedAct;		
	/** 死亡动作 */
	public final String DieAct;		
	/** 移动特效 */
	public final String MoveEffect;		
	/** 出生特效 */
	public final String BornEffect;		
	/** 出生音效 */
	public final String BornAudio;		

	public MonsterConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 怪物ID
		MoveSpeed = Integer.parseInt(element.getAttribute("MoveSpeed") == null || element.getAttribute("MoveSpeed").length() == 0 ? "0"
			: element.getAttribute("MoveSpeed")); // 移动速度
		AtkSpeed = Integer.parseInt(element.getAttribute("AtkSpeed") == null || element.getAttribute("AtkSpeed").length() == 0 ? "0"
			: element.getAttribute("AtkSpeed")); // 攻击速度
		scale = Integer.parseInt(element.getAttribute("scale") == null || element.getAttribute("scale").length() == 0 ? "0"
			: element.getAttribute("scale")); // 规模
		HP = Integer.parseInt(element.getAttribute("HP") == null || element.getAttribute("HP").length() == 0 ? "0"
			: element.getAttribute("HP")); // 生命
		Attack = Integer.parseInt(element.getAttribute("Attack") == null || element.getAttribute("Attack").length() == 0 ? "0"
			: element.getAttribute("Attack")); // 攻击
		EnemyResPath = element.getAttribute("EnemyResPath"); // 怪物资源
		SkillID = Integer.parseInt(element.getAttribute("SkillID") == null || element.getAttribute("SkillID").length() == 0 ? "0"
			: element.getAttribute("SkillID")); // 技能ID
		BornAct = element.getAttribute("BornAct"); // 出生动作
		MoveAct = element.getAttribute("MoveAct"); // 移动动作
		AtkAct = element.getAttribute("AtkAct"); // 攻击动作
		HurtedAct = element.getAttribute("HurtedAct"); // 受击动作
		DieAct = element.getAttribute("DieAct"); // 死亡动作
		MoveEffect = element.getAttribute("MoveEffect"); // 移动特效
		BornEffect = element.getAttribute("BornEffect"); // 出生特效
		BornAudio = element.getAttribute("BornAudio"); // 出生音效
	}
	

}
