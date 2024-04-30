package cn.game.protocol.generated.enume;

/**
 * 活动类型
 * 
 * 工具生成的，不要手动修改
 */
public enum ActivityTypeEnum{

	/** 首冲 */
	FirstCharge(1,"FirstCharge","首冲"),
	/** 单充 */
	SingleCharge(2,"SingleCharge","单充"),
    ;
	/** 活动类型 */
	public final int ID ; 
	/** 物品英文名 */
	public final String Name ; 
	/** 物品名称 */
	public final String Desc ; 

	private ActivityTypeEnum(int ID, String Name, String Desc) {
		this.ID = ID; 
		this.Name = Name; 
		this.Desc = Desc; 
	}
	
	public static ActivityTypeEnum get(int id) {
		ActivityTypeEnum[] values = ActivityTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ActivityTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static ActivityTypeEnum getNullable(int id) {
		ActivityTypeEnum[] values = ActivityTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		return null;
	}

}
