package cn.game.protocol.generated.enume;

/**
 * 探索对象枚举
 * 
 * 工具生成的，不要手动修改
 */
public enum ExploreObjectEnum{

	/** 地面 */
	GROUND(1,"GROUND","地面","地面",0,true,false,false,false,false,true,false,false,false,false,false,false,false,false,-1,false),
	/** 墙 */
	WALL(2,"WALL","墙","带遮挡关系的装饰物",0,false,false,false,false,false,false,false,false,false,false,false,false,false,false,-1,false),
	/** 区域入口 */
	ENTRANCE(5,"ENTRANCE","区域入口","区域入口",0,true,false,false,false,true,true,false,false,true,false,false,false,false,false,-1,true),
	/** 返回点 */
	EXIT(6,"EXIT","返回点","返回点",0,true,true,false,false,false,true,true,false,false,false,false,false,false,false,-1,true),
	/** 门 */
	DOOR(7,"DOOR","门","门",0,true,true,false,false,false,true,true,false,false,false,false,false,false,false,-1,true),
	/** 草丛 */
	GRASS(13,"GRASS","草丛","草丛",0,true,false,false,false,false,true,false,false,false,true,false,false,false,true,0,false),
	/** 菌落 */
	BACTERIAL_COLONY(14,"BACTERIAL_COLONY","菌落","菌落",0,true,false,false,false,false,true,false,false,false,true,false,false,false,true,0,false),
	/** 高台 */
	HIGH_WALL(54,"HIGH_WALL","高台","高台",0,false,false,false,false,false,false,false,false,false,false,false,false,false,false,-1,false),
	/** 表-玩家 */
	PLAYER_OUT(30,"PLAYER_OUT","表-玩家","表-玩家",1,false,true,false,false,false,false,false,false,false,false,true,false,true,false,-1,false),
	/** 里-玩家 */
	PLAYER_IN(31,"PLAYER_IN","里-玩家","里-玩家",1,false,true,false,false,false,false,false,false,false,false,true,true,true,false,-1,false),
	/** 空间楔 */
	SPACE_WEDGE(33,"SPACE_WEDGE","空间楔","空间楔",0,true,false,false,true,false,false,true,false,false,false,false,true,false,false,0,false),
	/** 平民建筑 */
	CIVILIAN_BUILDING(40,"CIVILIAN_BUILDING","平民建筑","平民建筑",0,false,false,false,false,false,false,false,false,false,false,false,false,false,false,-1,false),
	/** 怪物死坟墓 */
	GRAVE(68,"GRAVE","怪物死坟墓","坟墓",0,true,false,false,false,false,true,false,false,false,false,false,false,false,false,-1,true),
	/** 希格斯结晶 */
	HIGGS_CRYSTAL(101,"HIGGS_CRYSTAL","希格斯结晶","希格斯结晶",0,true,false,false,false,true,false,true,true,false,false,false,true,false,false,-1,false),
	/** 障碍 */
	OBSTACLE(3,"OBSTACLE","障碍","可破坏障碍物",0,false,true,true,false,false,false,false,false,false,false,false,false,false,false,-1,false),
	/** 陷阱 */
	SUSPEND(11,"SUSPEND","陷阱","陷阱",0,true,false,false,false,false,true,false,false,false,true,false,false,false,false,-1,false),
	/** 宝箱 */
	BOX(12,"BOX","宝箱","残骸",0,false,true,true,false,false,false,false,false,false,true,false,false,false,false,0,true),
	/** 材料堆 */
	MATERIAL_PILE(15,"MATERIAL_PILE","材料堆","材料堆",0,false,false,false,false,false,false,true,true,false,true,false,false,false,false,0,true),
	/** 遗迹 */
	RELIC(37,"RELIC","遗迹","遗迹",0,false,true,false,false,false,false,false,false,false,false,false,false,false,false,-1,true),
	/** 杂物 */
	REMAINS(38,"REMAINS","杂物","遗骸",0,false,true,true,false,false,false,false,false,false,false,false,false,false,false,-1,true),
	/** 补给箱 */
	SUPPLY_BOX(39,"SUPPLY_BOX","补给箱","补给箱",0,false,true,true,false,false,false,false,false,false,false,false,false,false,false,0,true),
	/** 时空传输 */
	SPACE_TRANSMITTER(53,"SPACE_TRANSMITTER","时空传输","时空传输器",4,false,true,false,false,false,false,true,false,false,false,false,false,false,false,0,false),
	/** 灰道 */
	GREY_TRACK(56,"GREY_TRACK","灰道","灰道",0,true,true,false,false,false,true,true,false,false,false,false,false,false,true,0,true),
	/** 尘埃 */
	ASTRAL_DUST(57,"ASTRAL_DUST","尘埃","星体尘埃",0,true,false,false,false,false,true,true,true,false,false,false,false,false,false,0,false),
	/** 一次性宝箱 */
	DISPOSABLE_BOX(67,"DISPOSABLE_BOX","一次性宝箱","一次性宝箱",0,false,true,true,false,false,false,false,false,false,true,false,false,false,false,0,false),
	/** 道具 */
	ITEM(63,"ITEM","道具","道具",0,false,true,true,false,false,false,false,false,false,false,false,false,false,false,-1,true),
	/** 道具-里 */
	ITEMIn(64,"ITEMIn","道具-里","道具-里",0,false,false,false,false,false,false,true,true,false,false,false,false,false,false,-1,true),
	/** 净化器 */
	BARRIER_GENERATOR(8,"BARRIER_GENERATOR","净化器","屏障发生器",0,false,true,false,false,false,false,false,false,false,false,false,false,false,true,0,true),
	/** 扰动区 */
	RAND_SEND(10,"RAND_SEND","扰动区","扰动",0,true,false,false,false,false,true,true,false,true,true,false,true,false,true,0,true),
	/** 辐射区 */
	RADIATION_AREA(27,"RADIATION_AREA","辐射区","辐射区",0,true,false,false,false,false,true,false,false,false,true,false,false,false,true,-1,false),
	/** 流沙区 */
	QUICKSAND_AREA(28,"QUICKSAND_AREA","流沙区","流沙区",0,true,false,false,false,false,true,false,false,false,true,false,false,false,true,-1,false),
	/** 信标 */
	BEACON(34,"BEACON","信标","信标",0,false,true,true,false,false,false,true,true,false,false,false,false,false,true,0,false),
	/** 破裂管道 */
	RUPTURED_PIPELINE(43,"RUPTURED_PIPELINE","破裂管道","破裂管道",4,false,false,false,false,false,false,false,false,false,false,false,false,false,true,1,true),
	/** 喷射口 */
	JET_POINT(44,"JET_POINT","喷射口","蒸汽喷口",4,true,true,false,false,false,true,false,false,false,false,false,false,false,true,1,false),
	/** 压力板 */
	PRESSURE_PLATE(45,"PRESSURE_PLATE","压力板","蒸汽喷口控制器",4,true,true,false,false,false,true,false,false,false,false,false,false,false,true,0,false),
	/** 机械门 */
	MECHANICAL_DOOR(46,"MECHANICAL_DOOR","机械门","机械门",4,false,true,false,true,false,true,false,false,false,false,false,false,false,true,0,false),
	/** 排污口 */
	SEWAGE_OUTLET(47,"SEWAGE_OUTLET","排污口","排污口",4,false,false,false,false,false,false,false,false,false,false,false,false,false,true,0,false),
	/** 汽油桶 */
	GASOLINE_CAN(48,"GASOLINE_CAN","汽油桶","汽油桶",4,false,true,false,true,false,false,false,false,false,false,false,false,false,true,0,false),
	/** 排水管 */
	DRAIN_LINE(49,"DRAIN_LINE","排水管","排水管",4,false,true,false,true,false,false,false,false,false,false,false,false,false,true,0,false),
	/** 火焰 */
	FIRE(50,"FIRE","火焰","火焰",4,true,false,false,false,false,true,false,false,false,false,false,false,false,true,0,false),
	/** 污水 */
	SEWAGE(51,"SEWAGE","污水","污水",4,true,false,false,false,false,true,false,false,false,false,false,false,false,true,0,false),
	/** 污染土壤 */
	CONTAMINATED_GROUND(58,"CONTAMINATED_GROUND","污染土壤","污染土壤",4,true,false,false,false,true,true,true,false,true,false,false,false,false,true,1,false),
	/** 漏电地面 */
	ELECTRICITY_GROUND(59,"ELECTRICITY_GROUND","漏电地面","漏电地面",4,true,true,false,false,false,true,true,false,true,false,false,false,false,true,1,true),
	/** 医疗箱 */
	MEDICALBOX(61,"MEDICALBOX","医疗箱","医疗箱",0,false,true,true,true,true,true,false,false,false,false,false,false,false,true,0,false),
	/** 补给分配机 */
	DISTRIBUTOR(71,"DISTRIBUTOR","补给分配机","补给分配机",0,false,true,true,true,true,true,false,false,false,false,false,false,false,true,0,false),
	/** 平民 */
	CIVILIAN(18,"CIVILIAN","平民","平民",0,false,true,false,false,false,false,false,false,false,false,true,false,true,false,0,false),
	/** 商人 */
	BUSINESSMAN(19,"BUSINESSMAN","商人","商人",0,false,true,false,false,false,false,false,false,false,false,true,false,true,false,0,false),
	/** 拾荒者 */
	SCAVENGER(20,"SCAVENGER","拾荒者","拾荒者",0,false,true,false,false,false,false,false,false,false,false,true,false,true,false,0,false),
	/** 预备队员 */
	RESERVES(70,"RESERVES","预备队员","预备队员",0,false,true,true,true,false,false,true,true,true,false,false,false,false,false,0,false),
	/** 事件NPC */
	EVENT_NPC(60,"EVENT_NPC","事件NPC","事件NPC",0,false,true,false,false,false,false,true,false,false,false,false,false,false,false,0,true),
	/** 无碰撞事件NPC */
	EVENT_NoNPC(80,"EVENT_NoNPC","无碰撞事件NPC","无碰撞事件NPC",0,true,false,false,false,false,true,false,false,false,false,false,false,false,false,0,true),
	/** 怪物 */
	MONSTER_SMALL(22,"MONSTER_SMALL","怪物","小怪",3,false,true,true,false,false,true,false,true,false,true,true,false,true,false,0,true),
	/** 怪物 */
	MONSTER_HIGHT(62,"MONSTER_HIGHT","怪物","精英怪",3,false,true,true,false,false,true,false,true,false,true,true,false,true,false,0,true),
	/** 恶灵 */
	GHOST(32,"GHOST","恶灵","恶灵",3,true,false,false,false,false,false,true,true,false,false,true,true,true,false,0,false),
	/** 特殊恶灵 */
	SPECIALGHOST(69,"SPECIALGHOST","特殊恶灵","特殊恶灵",3,true,false,false,false,false,false,true,true,false,false,true,true,true,false,0,false),
	/** BOSS */
	BOSS(41,"BOSS","BOSS","邪神",3,false,true,true,false,false,false,true,true,false,false,true,false,false,false,0,false),
	/** 追猎者 */
	STALKER(65,"STALKER","追猎者","追猎者",3,false,true,true,false,false,false,true,true,false,false,true,false,false,false,0,false),
	/** 游荡者 */
	WANDERER(66,"WANDERER","游荡者","游荡者",3,true,false,false,false,false,false,true,true,false,false,true,true,true,false,0,true),
	/** 毁灭者 */
	Destroyer(501,"Destroyer","毁灭者","毁灭者",3,false,true,true,false,false,false,false,true,false,false,true,false,true,false,0,false),
	/** 精神体 */
	SPRITBODY(502,"SPRITBODY","精神体","精神体",3,true,false,false,false,false,false,true,true,false,false,true,true,false,false,0,true),
	/** 医疗箱 */
	MEDICAL_BOX(78,"MEDICAL_BOX","医疗箱","医疗箱",0,false,true,true,false,false,false,false,false,false,false,false,false,false,false,0,false),
	/** 自动补给分配机 */
	SUPPLY_MACHINE(79,"SUPPLY_MACHINE","自动补给分配机","自动补给分配机",0,false,true,true,false,false,false,false,false,false,false,false,false,false,false,0,false),
	/** 地雷 */
	LANDMINE(72,"LANDMINE","地雷","地雷",4,true,true,true,false,false,false,false,false,false,false,false,false,false,false,0,true),
	/** 激光炮台 */
	LASER_TURRET(73,"LASER_TURRET","激光炮台","激光炮台",4,false,false,false,false,false,false,false,false,false,false,false,false,false,false,0,true),
	/** 防卫控制台 */
	LASER_TURRET_CONTROLLER(74,"LASER_TURRET_CONTROLLER","防卫控制台","防卫控制台",0,false,true,false,false,false,false,false,false,false,false,false,false,false,false,0,false),
	/** 液化气体罐 */
	GAS_TANK(75,"GAS_TANK","液化气体罐","液化气体罐",4,false,true,true,false,false,false,false,false,false,false,false,false,false,false,0,false),
	/** 空液化气体罐 */
	EMPTY_GAS_TANK(76,"EMPTY_GAS_TANK","空液化气体罐","空液化气体罐",4,false,true,true,false,false,false,false,false,false,false,false,false,false,false,0,false),
	/** 爆炸 */
	EXPLODE(77,"EXPLODE","爆炸","爆炸",4,false,true,true,false,false,false,false,false,false,false,false,false,false,false,0,true),
	/** 屏障 */
	BARRIER(4,"BARRIER","屏障","可破坏屏障",0,false,false,false,false,false,false,false,false,false,false,false,false,false,true,-1,false),
	/** 能量晶体 */
	ENERGY_CRYSTAL(9,"ENERGY_CRYSTAL","能量晶体","能量晶体",0,false,false,false,false,false,false,true,false,false,false,false,false,false,true,-1,false),
	/** 能量罐 */
	ENERGY_TANK(16,"ENERGY_TANK","能量罐","能量罐",0,false,true,true,false,false,false,true,true,false,false,false,false,false,true,-1,false),
	/** 动物 */
	ANIMAL(21,"ANIMAL","动物","动物",0,false,true,false,false,false,false,false,false,false,false,true,false,true,true,-1,false),
	/** 恐惧主教 */
	FEAR_BISHOP(23,"FEAR_BISHOP","恐惧主教","恐惧主教",3,true,false,false,false,false,false,true,true,false,false,true,true,false,true,-1,true),
	/** 人形傀儡 */
	HUMAN_PUPPET(24,"HUMAN_PUPPET","人形傀儡","傀儡怪",3,false,true,true,false,false,false,true,false,false,false,true,false,true,true,-1,true),
	/** 梯子 */
	LADDER(25,"LADDER","梯子","梯子",0,true,false,false,false,true,true,false,false,true,false,false,false,false,true,-1,false),
	/** 核弹 */
	BOMB(26,"BOMB","核弹","核弹",0,false,true,false,false,false,false,false,false,false,true,false,false,false,true,-1,false),
	/** 变异动物 */
	MUTANT_ANIMAL(29,"MUTANT_ANIMAL","变异动物","变异动物",0,false,true,true,false,false,false,false,false,false,true,true,false,false,true,-1,false),
	/** 藤壶 */
	BARNACLE(35,"BARNACLE","藤壶","藤壶",3,false,true,true,false,false,false,true,true,false,false,false,false,false,true,-1,false),
	/** 晶核 */
	CRYSTAL_NUCLEUS(36,"CRYSTAL_NUCLEUS","晶核","晶核",3,false,true,true,false,false,false,false,false,false,false,false,false,false,true,-1,false),
	/** 诡秘法师 */
	SECRETIVE(42,"SECRETIVE","诡秘法师","诡秘法师",3,true,true,true,false,false,false,true,true,false,false,true,true,false,true,-1,false),
	/** 占位 */
	PLACE_HOLDER(52,"PLACE_HOLDER","占位","占位",4,true,false,false,false,false,true,false,false,false,false,false,false,false,true,-1,false),
	/** 解谜类 */
	PUZZLE_CLASS(17,"PUZZLE_CLASS","解谜类","解迷",0,false,true,true,false,false,false,true,true,false,false,false,false,false,true,-1,false),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 描述 */
	private String desc ; 
	/** 地图名称 */
	private String mapName ; 
	/** 初始阵营 */
	private int initialCamp ; 
	/** 是否可移动到该位置 */
	private boolean movable_out ; 
	/** 是否可交互 */
	private boolean interactive_out ; 
	/** 交互后是否销毁 */
	private boolean destructible_out ; 
	/** 交互后是否移除碰撞 */
	private boolean removableCollision_out ; 
	/** 交互后玩家是否移动到该位置 */
	private boolean interactiveMovable_out ; 
	/** 是否可移动到该位置 */
	private boolean movable_inside ; 
	/** 是否可交互 */
	private boolean interactive_inside ; 
	/** 交互后是否销毁 */
	private boolean destructible_inside ; 
	/** 交互后玩家是否移动到该位置 */
	private boolean interactiveMovable_inside ; 
	/** 是否刷新 */
	private boolean isRefresh ; 
	/** 是否移动 */
	private boolean isMove ; 
	/** 是否是灵体 */
	private boolean isGhost ; 
	/** 是否受到陷阱即死判断 */
	private boolean isDie ; 
	/** 是否可覆盖 */
	private boolean isCover ; 
	/** 区域类型 */
	private int landforms ; 
	/** 产生索引 */
	private boolean isTypeIndex ; 

	private ExploreObjectEnum(int id, String name, String desc, String mapName, int initialCamp, boolean movable_out, boolean interactive_out, boolean destructible_out, boolean removableCollision_out, boolean interactiveMovable_out, boolean movable_inside, boolean interactive_inside, boolean destructible_inside, boolean interactiveMovable_inside, boolean isRefresh, boolean isMove, boolean isGhost, boolean isDie, boolean isCover, int landforms, boolean isTypeIndex) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
		this.mapName = mapName; 
		this.initialCamp = initialCamp; 
		this.movable_out = movable_out; 
		this.interactive_out = interactive_out; 
		this.destructible_out = destructible_out; 
		this.removableCollision_out = removableCollision_out; 
		this.interactiveMovable_out = interactiveMovable_out; 
		this.movable_inside = movable_inside; 
		this.interactive_inside = interactive_inside; 
		this.destructible_inside = destructible_inside; 
		this.interactiveMovable_inside = interactiveMovable_inside; 
		this.isRefresh = isRefresh; 
		this.isMove = isMove; 
		this.isGhost = isGhost; 
		this.isDie = isDie; 
		this.isCover = isCover; 
		this.landforms = landforms; 
		this.isTypeIndex = isTypeIndex; 
	}
	
	public static ExploreObjectEnum get(int id) {
		ExploreObjectEnum[] values = ExploreObjectEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ExploreObjectEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static ExploreObjectEnum getNullable(int id) {
		ExploreObjectEnum[] values = ExploreObjectEnum.values();
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
	public String getMapName(){
		return this.mapName;
	}
	public int getInitialCamp(){
		return this.initialCamp;
	}
	public boolean getMovable_out(){
		return this.movable_out;
	}
	public boolean getInteractive_out(){
		return this.interactive_out;
	}
	public boolean getDestructible_out(){
		return this.destructible_out;
	}
	public boolean getRemovableCollision_out(){
		return this.removableCollision_out;
	}
	public boolean getInteractiveMovable_out(){
		return this.interactiveMovable_out;
	}
	public boolean getMovable_inside(){
		return this.movable_inside;
	}
	public boolean getInteractive_inside(){
		return this.interactive_inside;
	}
	public boolean getDestructible_inside(){
		return this.destructible_inside;
	}
	public boolean getInteractiveMovable_inside(){
		return this.interactiveMovable_inside;
	}
	public boolean getIsRefresh(){
		return this.isRefresh;
	}
	public boolean getIsMove(){
		return this.isMove;
	}
	public boolean getIsGhost(){
		return this.isGhost;
	}
	public boolean getIsDie(){
		return this.isDie;
	}
	public boolean getIsCover(){
		return this.isCover;
	}
	public int getLandforms(){
		return this.landforms;
	}
	public boolean getIsTypeIndex(){
		return this.isTypeIndex;
	}
}
