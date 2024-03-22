package cn.game.protocol.generated.enume;

/**
 * 活动类型表
 * 
 * 工具生成的，不要手动修改
 */
public enum ActivityTypeEnum{

	/** 爬塔 */
	ClimbingTower(1,"ClimbingTower","爬塔"),
	Test1(2,"ClimbingTower","爬塔"),
	Test2(3,"ClimbingTower","爬塔"),
	Test3(4,"ClimbingTower","爬塔"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String desc ; 

	private ActivityTypeEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static ActivityTypeEnum get(int id) {
		ActivityTypeEnum[] values = ActivityTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ActivityTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static ActivityTypeEnum getNullable(int id) {
		ActivityTypeEnum[] values = ActivityTypeEnum.values();
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
