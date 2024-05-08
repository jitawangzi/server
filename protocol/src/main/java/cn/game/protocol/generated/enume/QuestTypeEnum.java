package cn.game.protocol.generated.enume;

/**
 * 任务类型枚举表
 * 
 * 工具生成的，不要手动修改
 */
public enum QuestTypeEnum{

	/** 日常任务 */
	Daily(1,"Daily","日常任务"),
	/** 周常任务 */
	Weekly(2,"Weekly","周常任务"),
	/** 主线任务 */
	MainLine(6,"MainLine","主线任务"),
	/** 支线任务 */
	BranchLine(7,"BranchLine","支线任务"),
	/** 成就任务 */
	Achievement(8,"Achievement","成就任务"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String desc ; 

	private QuestTypeEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static QuestTypeEnum get(int id) {
		QuestTypeEnum[] values = QuestTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【MissionTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static QuestTypeEnum getNullable(int id) {
		QuestTypeEnum[] values = QuestTypeEnum.values();
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
