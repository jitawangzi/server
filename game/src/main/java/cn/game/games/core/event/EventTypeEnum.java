package cn.game.games.core.event;

/**
 * 玩家的事件类型
 */
public enum EventTypeEnum {
	/** 创建新玩家，只会有一次这个事件 */
	PLAYER_CREATE(100, "创建新玩家"),
	/** 准备开始登陆（不太常用，特殊情况下使用）,发生在新玩家首次进入，和老玩家从数据库载入数据登陆时 */
	LoginStart(101, "开始登陆"),
	/** 登陆完成(一般根据这个事件来做登陆逻辑),Login 事件之后，发生在新玩家首次进入，和老玩家从数据库载入数据登陆，
	 * 强调内存中新初始化一个玩家，需要对数据进行初始化 */
	LoginFinish(102, "登陆完成"),
	/** 客户端重新登陆了，这个时候玩家内存数据还在，不需要进行数据初始化， 
	 * 通常不需要对这个事件做处理,但是可能需要考虑某些玩法，在重新登陆后的一些特殊处理  */
	Relogin(103, "重新登陆"),
	/** 重连，客户端没有退出，只是网络状态发生变化时，通常不需要对这个事件做处理,这个时候玩家内存数据还在。
	 * 特别注意，如果客户端长时间切后台，导致缓存被清理，客户端再切换回来时，是登陆（LoginFinish） 而不是重连 */
	Reconnect(104, "重连"),
	/** 不管是LoginFinish、Relogin任何一个事件产生时，也就是发生了登陆（分为有缓存，但是客户端启动游戏进行登录，
	 * 或者没有缓存了发生了数据重新加载，不是重连） 
	 * 都会产生这个事件，注意这个事件不要和上面2个事件同时处理，会导致重复。
	 * 不推荐使用，具体看情况，最好是单独的针对某个具体的事件进行处理。 
	 *  */
	LoginSuccess(105, "只要是登录就会刷新的该事件"),
	/** 早5点跨天 */
	NewDay5(108, "早5点跨天"),
	/** 过晚上12点，跨天 */
	NewDay(109, "过晚上12点，跨天"),
	/** 跨周 */
	NewWeek(110, "跨周"),
	/** 跨月 */
	NewMonth(111, "跨月"),
	/** 充值  // 参数 ： 充值数量(rmb)*/
	Charge(112, "充值"),

	/** 功能开启 */
	FuncOpen(113, "功能开启 "),

	/** 新添加了某种资源 */
	ResourceAdd(120, "新添加了某种资源"),
	/** 某种资源被移除 */
	ResourceRemove(120, "某种资源被移除"),
	
	WatchAds(130, "看了一次广告"),

	CultivatesImmortals(133, "提升天道修为等级"),

	/** 升级,注意是所有等级，包含玩家等级。  参数： 经验，升到的等级*/
	LevelUp(2, "升级"),
	/** 通关关卡，包含剧情普通关卡，探索关卡等 */
	Level(3, "通关关卡"), // 参数： 关卡id，回合数，剩余人数
	/** 章节胜利  参数: 章节id */
	ChapterWin(4, "章节胜利"),
	/** 主线章节第一次通关 ，参数： 章节id */
	HCChapterFirstWin(5, "主线章节第一次通关"),
	/** 英雄升级 参数：英雄*/
	HeroLevelUp(6, "英雄升级"),
	/** 英雄突破 ，参数，星级、品质*/
	HeroBreak(7, "英雄突破"),
	/** 伙伴升星 */
	RoleStarUp(8, "伙伴升星"),
	/** 技能升级 */
	SkillUp(9, "技能升级"),
	/** 获得技能 */
	Skill(10, "获得技能"),
	/** 获得装备 */
	Equip(11, "获得装备"),
	/** 装备强化 */
	EquipUp(12, "装备强化"),
	/** 英雄上阵 参数，hero */
	HeroBattle(14, "英雄上阵"),
	HeroBattleDismiss(15, "英雄下阵"),
	/** 获得英雄  参数： heroId*/
	Hero(16, "获得英雄"),
	/** 一场战斗结束，参数战役id，关卡id，输赢，杀怪数量 ,boss数量*/
	BattleEnd(17, "一场战斗结束"),

	HCHero(18, "获得合成英雄"),
	// 参数，id，value
	GetItem(19, "获取资源"),
	/** 消耗资源： 参数 id，数量(int类型) */
	CostItem(20, "消耗资源"),
	/** 消耗带有uid的物品： 参数 uid，configId */
	CostUidItem(21, "消耗带有uid的物品"),

	ReceiveStamina(22, "领取体力"),

	Draw(23, "抽卡"),
	/** 巡逻、挂机  参数：是否是快速巡逻 */
	Patrol(24, "巡逻、挂机"),

	/** 合成章节胜利  参数: 章节id*/
	HCChapterWin(25, "合成章节胜利"),
	QuestReward(26, "完成任务"),

	// 参数 : 商店id，商品id，数量
	BuyItems(28, "商店购买商品"),

	QianLi(29, "潜力修炼"),
	/** 参数： hero */
	HeroQuality(30, "英雄品质提升"),
	/** 第一次通关主线  参数: 章节id ，battle表id*/
	ChapterFirstWin(33, "第一次通关主线 "),
	//成就相关
	/** 开启战斗 :  Battle战役id，关卡id*/
	BattleStart(54, "开启战斗"),
	/** 开启战斗 : HCBattle id，关卡id*/
	HCBattleStart(55, "开启战斗"),
	ParticipatePVPStart(56, "开始大道争锋PVP战斗"),
	/** 角色更换技能 */
	SwitchSkills(61, "角色更换技能"),
	/** 探索后获得资源 */
	ExploreGetResources(62, "探索后获得资源"),
	/** 使用道具 */
	ItemUse(68, "使用道具"),
	/** 收集图鉴 */
	CollectAtlas(70, "收集图鉴"),
	/** buff改变 */
	BuffChange(75, "buff改变"),

	/** 某游戏事件完成 参数： 事件id */
	GameEventFinish(76, "某游戏事件完成"),
	vipExpChange(77, "VIP 经验变动"),
	vipLevelChange(78, "VIP 等级变动"),
	QuestFinish(79,"任务完成"),
	QiangYuan(85, "强援修炼"),
	FairyFriendsTravel(86, "仙友寻缘"),
	FairyFriendsGift(87, "仙友赠礼"),

	HeroRecruit(90, "英雄招募"),

	/** 宗门砍价 */
	ZongMenBargain(303, "宗门砍价"),
	/** 加入某个宗门，参数 ：1 宗门id，2宗门名字， 3  是否是第一次加入宗门  */
	ZongMenJoin(304, "加入某个宗门"),
	ZongMenDonate(305, "宗门捐献"),
	;
	private int id;
	private String desc;

	private EventTypeEnum(int id, String desc) {
		this.id = id;
		this.desc = desc;
	}

	public int getId() {
		return this.id;
	}

	public String getDesc() {
		return this.desc;
	}
}
