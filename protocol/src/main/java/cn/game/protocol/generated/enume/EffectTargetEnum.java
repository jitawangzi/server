package cn.game.protocol.generated.enume;

/**
 * 效果小目标类型
 * 
 * 工具生成的，不要手动修改
 */
public enum EffectTargetEnum{

	/** 无 */
	Null(-1,"Null","无",1,0),
	/** 玩家资产类 */
	PlayerResouce(101,"PlayerResouce","玩家资产类",1,0),
	/** 探索中指定角色 */
	ExploreRole(203,"ExploreRole","探索中指定角色",8,1),
	/** 探索中所有角色 */
	ExploreAllRole(205,"ExploreAllRole","探索中所有角色",8,1),
	/** 通用的角色 */
	Role(206,"Role","通用的角色",2,0),
	/** 探索中当前队伍随机一名角色 */
	ExploreCurTeamRandomRole(207,"ExploreCurTeamRandomRole","探索中当前队伍随机一名角色",8,1),
	/** 所有通用的角色 */
	AllRole(208,"AllRole","所有通用的角色",2,1),
	/** 单个高级建筑 */
	SingleBuilding(301,"SingleBuilding","单个高级建筑",3,0),
	/** 所有高级建筑 */
	AllBuilding(302,"AllBuilding","所有高级建筑",3,0),
	/** 单个商店 */
	SingleStore(401,"SingleStore","单个商店",4,0),
	/** 探索中当前队伍 */
	ExploreTeam(501,"ExploreTeam","探索中当前队伍",5,1),
	/** 探索属性 */
	ExploreAttribute(601,"ExploreAttribute","探索属性",6,1),
	/** 遗迹 */
	Relic(602,"Relic","遗迹",6,1),
	/** 整个探索 */
	Explore(603,"Explore","整个探索",6,1),
	/** 接触的怪物 */
	AllExploreMonster(701,"AllExploreMonster","接触的怪物",7,1),
	/** 接触的所有中立单位 */
	AllExploreNeutral(702,"AllExploreNeutral","接触的所有中立单位",7,1),
	/** 遗迹参与的所有角色 */
	RelicAllRole(201,"RelicAllRole","遗迹参与的所有角色",8,1),
	/** 遗迹参与的角色中随机一个角色 */
	RelicRandomRole(202,"RelicRandomRole","遗迹参与的角色中随机一个角色",8,1),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 描述 */
	private String desc ; 
	/** type */
	private int type ; 
	/** 非正常情况下销毁类型 */
	private int destroyType ; 

	private EffectTargetEnum(int id, String name, String desc, int type, int destroyType) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
		this.type = type; 
		this.destroyType = destroyType; 
	}
	
	public static EffectTargetEnum get(int id) {
		EffectTargetEnum[] values = EffectTargetEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【EffectTargetEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static EffectTargetEnum getNullable(int id) {
		EffectTargetEnum[] values = EffectTargetEnum.values();
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
	public int getType(){
		return this.type;
	}
	public int getDestroyType(){
		return this.destroyType;
	}
}
