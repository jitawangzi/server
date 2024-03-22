package cn.game.protocol.generated.enume;

/**
 * 探索玩法物件枚举
 * 
 * 工具生成的，不要手动修改
 */
public enum ExploreTypeEnum{

	/** 空地 */
	NULL(0,"NULL","空地",true,false,false,true),
	/** 障碍 */
	OBSTACLE(1,"OBSTACLE","障碍",false,false,false,false),
	/** 活板门 */
	DOOR(2,"DOOR","活板门",true,true,false,true),
	/** 入口 */
	ENTRANCE(3,"ENTRANCE","入口",true,true,false,true),
	/** 出口 */
	EXIT(4,"EXIT","出口",true,true,false,true),
	/** 药草 */
	HERBS(5,"HERBS","药草",false,true,true,false),
	/** 中继器 */
	REPEATER(6,"REPEATER","中继器",false,true,false,false),
	/** 传送陷阱 */
	SEND(7,"SEND","传送陷阱",true,false,true,true),
	/** 定身陷阱 */
	SUSPEND(8,"SUSPEND","定身陷阱",true,false,true,true),
	/** 宝箱/残骸 */
	BOX(9,"BOX","宝箱/残骸",false,true,true,false),
	/** 草地 */
	GRASS(10,"GRASS","草地",true,false,false,true),
	/** 怪物 */
	MONSTER(11,"MONSTER","怪物",false,true,true,false),
	/** 队员 */
	ROLE(12,"ROLE","队员",false,true,true,false),
	/** 梯子 */
	LADDER(13,"LADDER","梯子",true,false,false,true),
	/** 坟冢 */
	GRAVE(14,"GRAVE","坟冢",true,true,true,true),
	/** 迷失者 */
	LOST(15,"LOST","迷失者",false,true,true,false),
	/** 完成副本 */
	REPLICA_CLEARANCE(16,"REPLICA_CLEARANCE","完成副本",false,true,true,false),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 描述 */
	private String desc ; 
	/** 是否可移动到该位置 */
	private boolean movable ; 
	/** 是否可交互 */
	private boolean interactive ; 
	/** 交互后是否销毁 */
	private boolean destructible ; 
	/** 交互后玩家是否移动到该位置 */
	private boolean interactiveMovable ; 

	private ExploreTypeEnum(int id, String name, String desc, boolean movable, boolean interactive, boolean destructible, boolean interactiveMovable) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
		this.movable = movable; 
		this.interactive = interactive; 
		this.destructible = destructible; 
		this.interactiveMovable = interactiveMovable; 
	}
	
	public static ExploreTypeEnum get(int id) {
		ExploreTypeEnum[] values = ExploreTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ExploreTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static ExploreTypeEnum getNullable(int id) {
		ExploreTypeEnum[] values = ExploreTypeEnum.values();
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
	public boolean getMovable(){
		return this.movable;
	}
	public boolean getInteractive(){
		return this.interactive;
	}
	public boolean getDestructible(){
		return this.destructible;
	}
	public boolean getInteractiveMovable(){
		return this.interactiveMovable;
	}
}
