package cn.game.protocol.manual;


/**   
 * @Description 工会建筑类型
 * @date Aug 11, 2015 10:28:27 AM
 * @author SYQ
 */
public enum UnionBuildingType {
	
	/** 工会大厅 */
	 UNION(1),
	 /** 建筑大厅 */
	 BUILDING(2),
	/** 公会战 */
	 BATTLE(3),
	/** 副本 */
	 INSTANCE(4),
	 /** 工会商店 */
	 SHOP(5),
	 /** 技能大厅 */
	 SKILL(6),
	 /**  洞穴风暴 */
	 CAVE(7),
	 ;
	 
	 private int type ; 
	 
	 private UnionBuildingType(int type){
		 this.type = type ; 
	 }
	
	public final int getType() {
	
		return type;
	}
	
	public static UnionBuildingType valueOf(int type){
		
		for (UnionBuildingType unionBuildingType : values())
		{
			if (unionBuildingType.getType()==type)
			{
				return unionBuildingType ; 
			}
		}
		return null ; 
	}
	 
	
}
