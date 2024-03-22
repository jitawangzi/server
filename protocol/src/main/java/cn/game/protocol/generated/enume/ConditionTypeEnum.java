package cn.game.protocol.generated.enume;

/**
 * 条件类型
 * 
 * 工具生成的，不要手动修改
 */
public enum ConditionTypeEnum{

	/** 用户等级 */
	PlayerLevel(1,"PlayerLevel","用户等级"),
	/** 战队战力 */
	PlayerCombat(2,"PlayerCombat","战队战力"),
	/** 累计充值 */
	AccumulatedRecharge(3,"AccumulatedRecharge","累计充值"),
	/** 通关章节 */
	ChapterFinish(4,"ChapterFinish","通关章节"),
	/** 持有月卡 */
	MonthCard(5,"MonthCard","持有月卡"),
    ;
	/** id */
	public final int ID ; 
	/** 英文名称 */
	public final String name ; 
	/** 说明 */
	public final String desc ; 

	private ConditionTypeEnum(int ID, String name, String desc) {
		this.ID = ID; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static ConditionTypeEnum get(int id) {
		ConditionTypeEnum[] values = ConditionTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ConditionTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static ConditionTypeEnum getNullable(int id) {
		ConditionTypeEnum[] values = ConditionTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		return null;
	}

}
