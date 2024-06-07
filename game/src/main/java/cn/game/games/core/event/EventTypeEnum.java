package cn.game.games.core.event;

/**
 * 事件类型
 */
public enum EventTypeEnum {
	/** 创建新玩家 */
	PLAYER_CREATE(100, "创建新玩家"),
	/** 开始登陆 */
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

	/** 升级,注意是所有等级，包含玩家等级。  参数： 经验，升到的等级*/
	LevelUp(2, "升级"),
	/** 通关关卡，包含剧情普通关卡，探索关卡等 */
	Level(3, "通关关卡"), // 参数： 关卡id，回合数，剩余人数
	/** 章节胜利  参数: 章节id*/
	ChapterWin(4, "章节胜利"),
	/** 主线章节第一次通关 ，参数： 章节id */
	HCChapterFirstWin(5, "主线章节第一次通关"),
	/** 英雄升级 参数：英雄*/
	HeroLevelUp(6, "英雄升级"),
	/** 英雄突破 */
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
	/** 获得英雄 */
	Hero(15, "获得英雄"),
	/** 一场战斗结束，参数战役id，关卡id，输赢，杀怪数量 ,boss数量*/
	BattleEnd(16, "一场战斗结束"),

	// 任务相关
	/** 完成意识空间 */
	// 参数，id，value
	GetItem(19, "获取资源"),
	/** 消耗资源： 参数 id，数量 */
	CostItem(20, "消耗资源"),

	QuestFinish(21, "完成任务"),

	ReceiveStamina(22, "领取体力"),

	Draw(23, "抽卡"),
	/** 巡逻、挂机  参数：是否是快速巡逻 */
	Patrol(24, "巡逻、挂机"),

	/** 合成章节胜利  参数: 章节id*/
	HCChapterWin(25, "合成章节胜利"),

	// 参数 : 商店id，商品id，数量
	BuyItems(28, "商店购买商品"),

	// 探索事件 
	/** 探索中经过一回合 */
	ExploreRound(30, "探索中经过一回合"),
	/** 探索中人经过一回合 */
	ExplorePlayerRound(31, "探索中人经过一回合"),
	/** 探索中怪或者中立单位经过一回合 */
	ExploreNpcRound(32, "探索中怪或者中立单位经过一回合"),
	/** 进入某探索地图 */
	ExploreMapEnter(35, "进入某探索地图"),
	
	/** 出探索地图 */
	ExploreMapEnd(47, "出探索地图"),
	/** 跨探索区域 */
	ExploreLevelEnd(36, "跨探索区域"),
	/** 探索结束 */
	ExploreEnd(37, "探索结束"),
	/** 探索开始 */
	ExploreStart(34, "探索开始"),
	
	/** 通关某探索章 */
	ExploreClearance(38, "通关某探索章"),
	/** 探索角色复活参数，探索角色uid */
	ExploreRoleResurrection(39, "探索角色复活"),
	
	/** 9-战斗中损失属性值 参数1:角色uid 参数2:属性id 参数3:损失值 */
	ExploreWoundedInBattle(41, "战斗中损失属性值"),
	/** 10-战斗胜利 */
	ExploreBattleWin(42, "战斗胜利"),
	/** 11-探索地图购买道具(不包括局间) */
	ExploreExploreStoreBuy(43, "探索地图购买道具"),
	/** 12-获取晶矿结晶 */
	ExploreMaterialReward(44, "获取晶矿结晶"),
	/** 13-宝箱、遗骸获得物品 */
	ExploreBoxAndRemainsReward(45, "宝箱、遗骸获得物品"),
	/** 14-获得金币 */
	ExploreGetCoin(45, "获得金币"),
	/** 补给值小于某值参数： 补给值 */
	SupplyLessThanOneValue(46, "补给值小于某值"),
	/** 忽略啥也不干,只是占位,为了配置数据正确 */
	Ignore(50, "忽略"),

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
	/** 固定装备升级 */
	FixedEquipmentLevel(71, "固定装备升级"),
	/** buff改变 */
	BuffChange(75, "buff改变"),

	/** 某游戏事件完成 参数： 事件id */
	GameEventFinish(76, "某游戏事件完成"),

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
