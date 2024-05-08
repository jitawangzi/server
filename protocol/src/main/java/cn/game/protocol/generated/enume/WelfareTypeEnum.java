package cn.game.protocol.generated.enume;

/**
 * 福利类型
 * 
 * 工具生成的，不要手动修改
 */
public enum WelfareTypeEnum{

	/** 免广告 */
	Advertise(1,"Advertise","免广告"),
	/** 增加巡逻收益 */
	PatrolIncome(2,"PatrolIncome","增加巡逻收益"),
	/** 增加快速巡逻次数 */
	QuicPatrolCnt(3,"QuicPatrolCnt","增加快速巡逻次数"),
	/** 增加体力上限 */
	PlayerEnergy(4,"PlayerEnergy","增加体力上限"),
    ;
	/** id */
	public final int ID ; 
	/** 英文名称 */
	public final String name ; 
	/** 说明 */
	public final String desc ; 

	private WelfareTypeEnum(int ID, String name, String desc) {
		this.ID = ID; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static WelfareTypeEnum get(int id) {
		WelfareTypeEnum[] values = WelfareTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【WelfareTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static WelfareTypeEnum getNullable(int id) {
		WelfareTypeEnum[] values = WelfareTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		return null;
	}

}
