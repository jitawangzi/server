package cn.game.protocol.generated.enume;

/**
 * 任务类型枚举表
 * 
 * 工具生成的，不要手动修改
 */
public enum MissionTypeEnum{

	/** 日常任务 */
	Daily(1,"Daily","日常任务"),
	/** 周常任务 */
	Weekly(2,"Weekly","周常任务"),
	/** 挑战任务 */
	Challenge(3,"Challenge","挑战任务"),
	/** 空 */
	Null(4,"Null","空"),
	/** 通行证 */
	BattlePass(5,"BattlePass","通行证"),
	/** 主线任务 */
	MainLine(6,"MainLine","主线任务"),
	/** 支线任务 */
	BranchLine(7,"BranchLine","支线任务"),
	/** 成就任务 */
	Achievement(8,"Achievement","成就任务"),
	/** 探索中的主线任务 */
	ExploreMainLine(9,"ExploreMainLine","探索中的主线任务"),
	/** 探索事件任务 */
	Explore(10,"Explore","探索事件任务"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String desc ; 

	private MissionTypeEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static MissionTypeEnum get(int id) {
		MissionTypeEnum[] values = MissionTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【MissionTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static MissionTypeEnum getNullable(int id) {
		MissionTypeEnum[] values = MissionTypeEnum.values();
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
