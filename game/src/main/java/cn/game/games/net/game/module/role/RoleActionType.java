package cn.game.games.net.game.module.role;


public enum RoleActionType {
	/** 普通行为 */
	Common(1),
	/** 战斗行为 */
	Battle(2),
	;
	
	private int id ; 

	private RoleActionType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
