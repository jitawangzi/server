package cn.game.protocol.generated.enume;

/**
 * 探索房间枚举
 * 
 * 工具生成的，不要手动修改
 */
public enum ExploreRoomTypeEnum{

	/** 商店 */
	STORE(1,"STORE","商店"),
	/** 黑店 */
	BLACK_STORE(2,"BLACK_STORE","黑店"),
	/** 流浪者 */
	WANDERER(3,"WANDERER","流浪者"),
	/** 血站 */
	BLOOD(4,"BLOOD","血站"),
	/** 抵抗者据点 */
	RESISTANCE_STRONGHOLD(5,"RESISTANCE_STRONGHOLD","抵抗者据点"),
	/** 深水研究所 */
	DEEPWATER_RESEARCH_INSTITUTE(6,"DEEPWATER_RESEARCH_INSTITUTE","深水研究所"),
	/** 安全屋 */
	SAFE_ROOM(7,"SAFE_ROOM","安全屋"),
	/** 任务房间 */
	MISSION(8,"MISSION","任务房间"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 描述 */
	private String desc ; 

	private ExploreRoomTypeEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static ExploreRoomTypeEnum get(int id) {
		ExploreRoomTypeEnum[] values = ExploreRoomTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ExploreRoomTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static ExploreRoomTypeEnum getNullable(int id) {
		ExploreRoomTypeEnum[] values = ExploreRoomTypeEnum.values();
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
