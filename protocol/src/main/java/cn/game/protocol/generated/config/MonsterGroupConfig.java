package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 怪物组
 * 
 * 工具生成的，不要手动修改
 */
 public class MonsterGroupConfig {

	/** 序列号 */
	public final int ID;		
	/** 怪物组ID */
	public final int GroupID;		
	/** 怪物资源ID */
	public final int EnemyID;		
	/** 怪物属性ID */
	public final int EnemyAttID;		
	/** 权重 */
	public final int Weight;		

	public MonsterGroupConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 序列号
		GroupID = Integer.parseInt(element.getAttribute("GroupID") == null || element.getAttribute("GroupID").length() == 0 ? "0"
			: element.getAttribute("GroupID")); // 怪物组ID
		EnemyID = Integer.parseInt(element.getAttribute("EnemyID") == null || element.getAttribute("EnemyID").length() == 0 ? "0"
			: element.getAttribute("EnemyID")); // 怪物资源ID
		EnemyAttID = Integer.parseInt(element.getAttribute("EnemyAttID") == null || element.getAttribute("EnemyAttID").length() == 0 ? "0"
			: element.getAttribute("EnemyAttID")); // 怪物属性ID
		Weight = Integer.parseInt(element.getAttribute("Weight") == null || element.getAttribute("Weight").length() == 0 ? "0"
			: element.getAttribute("Weight")); // 权重
	}
	

}
