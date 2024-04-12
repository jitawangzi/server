package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 怪物资源
 * 
 * 工具生成的，不要手动修改
 */
 public class ResourceMonsterConfig {

	/** 怪物资源ID */
	public final int ID;		
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

	public ResourceMonsterConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 怪物资源ID
		EnemyResPath = element.getAttribute("EnemyResPath"); // 怪物资源
		SkillID = Integer.parseInt(element.getAttribute("SkillID") == null || element.getAttribute("SkillID").length() == 0 ? "0"
			: element.getAttribute("SkillID")); // 技能ID
		BornAct = element.getAttribute("BornAct"); // 出生动作
		MoveAct = element.getAttribute("MoveAct"); // 移动动作
		AtkAct = element.getAttribute("AtkAct"); // 攻击动作
		HurtedAct = element.getAttribute("HurtedAct"); // 受击动作
		DieAct = element.getAttribute("DieAct"); // 死亡动作
	}
	

}
