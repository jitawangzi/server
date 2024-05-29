package cn.game.protocol.manual;

/**
 * 副本枚举
 */
public enum DungeonTypeEnum{

	/** 主线章节 */
	BattleChapter(1, "主线章节"),
	/** 道心磨砺战斗 */
	DaoHeart(2, "道心磨砺战斗"),
	/** 突发事件 */
	BattleEvent(3, "突发事件"),
	/** 探索战斗 */
	ExploreBattle(4, "探索战斗"),

	// 合成游戏的战斗
	HCBattleChapter(11, "合成游戏主线章节"),
	DayChallenge(12, "合成每日挑战"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String desc ; 

	private DungeonTypeEnum(int id, String desc) {
		this.id = id; 
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
	public String getDesc(){
		return this.desc;
	}
}
