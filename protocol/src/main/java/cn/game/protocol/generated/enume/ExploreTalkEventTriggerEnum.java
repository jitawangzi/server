package cn.game.protocol.generated.enume;

/**
 * 探索对话事件触发枚举表
 * 
 * 工具生成的，不要手动修改
 */
public enum ExploreTalkEventTriggerEnum{

	/** 当视野内出现怪物时（从无到有） */
	MonsterAppears(1,"MonsterAppears","当视野内出现怪物时（从无到有）"),
	/** 当视野内的怪物达到X个时 */
	MonsterAppearsNumbers(2,"MonsterAppearsNumbers","当视野内的怪物达到X个时"),
	/** 当视野内出现（类型怪）时 */
	MonsterAppearsType(3,"MonsterAppearsType","当视野内出现（类型怪）时"),
	/** 污染值超过百分之X时 */
	PollutionValue(4,"PollutionValue","污染值超过百分之X时"),
	/** 当非自己的角色重伤时 */
	RoleSeriousInjury(5,"RoleSeriousInjury","当非自己的角色重伤时"),
	/** 当自己重伤时 */
	OwnSeriousInjury(6,"OwnSeriousInjury","当自己重伤时"),
	/** 当角色死亡时 */
	RoleDeath(7,"RoleDeath","当角色死亡时"),
	/** 队伍存活人数降低至X时（指两个世界都死亡） */
	SurvivalNumbers(8,"SurvivalNumbers","队伍存活人数降低至X时（指两个世界都死亡）"),
	/** 仅剩自己存活时 */
	OwnSerious(9,"OwnSerious","仅剩自己存活时"),
	/** 进入隐蔽时 */
	Concealment(10,"Concealment","进入隐蔽时"),
	/** 角色进入X状态（buff表）时 */
	RoleState(11,"RoleState","角色进入X状态（buff表）时"),
	/** 到达新地图时 */
	EnterNewMap(12,"EnterNewMap","到达新地图时"),
	/** 开始新区域时 */
	EnterNewArea(13,"EnterNewArea","开始新区域时"),
	/** 进入角色总览界面时 */
	EnterRoleOverviewUI(14,"EnterRoleOverviewUI","进入角色总览界面时"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String desc ; 

	private ExploreTalkEventTriggerEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static ExploreTalkEventTriggerEnum get(int id) {
		ExploreTalkEventTriggerEnum[] values = ExploreTalkEventTriggerEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ExploreTalkEventTriggerEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static ExploreTalkEventTriggerEnum getNullable(int id) {
		ExploreTalkEventTriggerEnum[] values = ExploreTalkEventTriggerEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		return null;
	}

	public int getId(){
		return this.id;
	}
	public String getName(){
		return this.name;
	}
	public String getDesc(){
		return this.desc;
	}
}
