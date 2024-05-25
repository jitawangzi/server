package cn.game.protocol.generated.enume;

/**
 * 任务类型
 * 
 * 工具生成的，不要手动修改
 */
public enum QuestTypeEnum{

	/** 日常任务 */
	Daily(1,"Daily","日常任务"),
	/** 周常任务 */
	Weekly(2,"Weekly","周常任务"),
	/** 成就任务 */
	Achievement(3,"Achievement","成就任务"),
	/** 七日狂欢任务 */
	SevenDaysCarniva(4,"SevenDaysCarniva","七日狂欢任务"),
	/** 其他 */
	Other(10,"Other","其他"),
	/** 主线任务 */
	MainLine(55,"MainLine","主线任务"),
	/** 支线任务 */
	BranchLine(66,"BranchLine","支线任务"),
	/** 每日挑战积分 */
	DailyChallengePoint(70,"DailyChallengePoint","每日挑战积分"),
    ;
	/** id */
	public final int ID ; 
	/** 英文名称 */
	public final String name ; 
	/** 说明 */
	public final String desc ; 

	private QuestTypeEnum(int ID, String name, String desc) {
		this.ID = ID; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static QuestTypeEnum get(int id) {
		QuestTypeEnum[] values = QuestTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【QuestTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static QuestTypeEnum getNullable(int id) {
		QuestTypeEnum[] values = QuestTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		return null;
	}

}
