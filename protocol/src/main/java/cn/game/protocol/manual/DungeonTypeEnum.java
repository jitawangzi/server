package cn.game.protocol.manual;

/**
 * 副本枚举
 * 
 * 工具生成的，不要手动修改
 */
public enum DungeonTypeEnum{

	/** 列传 */
	BattleChapter(1,"BattleChapter","列传"),
	/** 道心磨砺战斗 */
	DaoHeart(2, "DaoHeart", "道心磨砺战斗"),
	/** 突发事件 */
	BattleEvent(3,"BattleEvent","突发事件"),
	/** 探索战斗 */
	ExploreBattle(4,"ExploreBattle","探索战斗"),
	/** 主线战斗 */
	MainlineBattle(5,"MainlineBattle","主线战斗"),

	DayChallenge(12, "DayChallenge", "每日挑战"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String desc ; 

	private DungeonTypeEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static DungeonTypeEnum get(int id) {
		DungeonTypeEnum[] values = DungeonTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【DungeonTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static DungeonTypeEnum getNullable(int id) {
		DungeonTypeEnum[] values = DungeonTypeEnum.values();
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
