package cn.game.protocol.generated.enume;

/**
 * 效果大目标类型
 * 
 * 工具生成的，不要手动修改
 */
public enum EffectTargetTypeEnum{

	/** 玩家 */
	Player(1,"Player","玩家"),
	/** 角色 */
	Role(2,"Role","角色"),
	/** 建筑 */
	Building(3,"Building","建筑"),
	/** 商店 */
	Shop(4,"Shop","商店"),
	/** 队伍 */
	Team(5,"Team","队伍"),
	/** 探索 */
	Explore(6,"Explore","探索"),
	/** 探索中的npc（如：怪物/中立单位） */
	ExploreNpc(7,"ExploreNpc","探索中的npc（如：怪物/中立单位）"),
	/** 探索角色 */
	ExploreRole(8,"ExploreRole","探索角色"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 描述 */
	private String desc ; 

	private EffectTargetTypeEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static EffectTargetTypeEnum get(int id) {
		EffectTargetTypeEnum[] values = EffectTargetTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【EffectTargetTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static EffectTargetTypeEnum getNullable(int id) {
		EffectTargetTypeEnum[] values = EffectTargetTypeEnum.values();
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
