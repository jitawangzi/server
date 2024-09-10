package cn.game.protocol.manual;

/**
 * 副本枚举
 */
public enum DungeonTypeEnum{

	/** 主线章节 */
	BattleChapter(1, "主线章节"),
	/** 道心磨砺战斗 */
	DaoHeart(2, "道心磨砺战斗"),

	/** 心魔试炼战斗 */
	XinMo(3, "心魔试炼战斗"),

	/** 妖王别跑战斗 */
	YaoWang(4, "妖王别跑战斗"),

	/** 世界boss */
	WorldBoss(21, "世界boss"),

	/** 梦魇秘境战斗 */
	MengYanMiJing(9, "梦魇秘境战斗"),
	/** 灵魄之战 */
	LingPo(10, "灵魄之战"),

	/** 失落真经 */
	ShiLuoZhenJing(20, "失落真经"),

	/** 突发事件 */
	BattleEvent(33, "突发事件"),
	/** 探索战斗 */
	ExploreBattle(44, "探索战斗"),

	// 合成游戏的战斗
	HCBattleChapter(11, "合成游戏主线章节"),
	DayChallenge(12, "合成每日挑战"),
	CHAPTER_TYPE_DA_DAO(50, "大道争锋玩法"),
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
