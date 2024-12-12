package cn.game.games.core.event;

/**
 * 事件类型
 */
public enum EventTypeEnum {
	/** 创建新玩家 */
	PLAYER_CREATE(100, "创建新玩家"),
	/** 开始登陆,似乎用处不大，可以用LoginFinish代替 */
	Login(101, "开始登陆"),
	/** 登陆完成 */
	LoginFinish(102, "登陆完成"),
	/** 重连 */
	Reconnect(103, "重连"),
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

	Practice(29, "修炼"),
	/** 参数： hero */
	HeroQuality(30, "英雄品质提升"),
	/** 第一次通关主线  参数: 章节id ，battle表id*/
	ChapterFirstWin(33, "第一次通关主线 "),
	//成就相关
	/** 开启战斗 :  Battle战役id，关卡id*/
	BattleStart(54, "开启战斗"),
	/** 开启战斗 : HCBattle id，关卡id*/
	HCBattleStart(54, "开启战斗"),
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
	LoginSuccess(80, "只要是登录就会刷新的该事件"),
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
