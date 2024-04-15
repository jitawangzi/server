package cn.game.games.core.event;

/**
 * 事件类型
 */
public enum EventTypeEnum {
	/** 创建新玩家 */
	PLAYER_CREATE(100, "PLAYER_CREATE", "创建新玩家"),
	/** 开始登陆 */
	Login(101, "Login", "开始登陆"),
	/** 登陆完成 */
	LoginFinish(102, "LoginFinish", "登陆完成"),
	/** 重连 */
	Reconnect(103, "Reconnect", "重连"),
	/** 早5点跨天 */
	NewDay5(108, "Day5Refresh", "早5点跨天"),
	/** 过晚上12点，跨天 */
	NewDay(109, "NewDay", "过晚上12点，跨天"),
	/** 跨周 */
	NewWeek(110, "NewWeek", "跨周"),
	/** 跨月 */
	NewMonth(111, "NewMonth", "跨月"),

	/** 新添加了某种资源 */
	ResourceAdd(120, "ResourceAdd", "新添加了某种资源"),
	/** 某种资源被移除 */
	ResourceRemove(120, "ResourceRemove", "某种资源被移除"),
	
	WatchAds(130, "WatchAds", "看了一次广告"),

	/** 升级 */
	LevelUp(2, "LevelUp", "升级"),
	/** 通关关卡，包含剧情普通关卡，探索关卡等 */
	Level(3, "Level", "通关关卡"), // 参数： 关卡id，回合数，剩余人数
	/** 通关章节 */
	Chapter(4, "Chapter", "通关章节"),
	/** 主角升星 */
	StarUp(5, "StarUp", "主角升星"),
	/** 伙伴升级 */
	RoleLevelUp(6, "RoleLevelUp", "伙伴升级"),
	/** 伙伴突破 */
	Break(7, "Break", "伙伴突破"),
	/** 伙伴升星 */
	RoleStarUp(8, "RoleStarUp", "伙伴升星"),
	/** 技能升级 */
	SkillUp(9, "SkillUp", "技能升级"),
	/** 获得技能 */
	Skill(10, "Skill", "获得技能"),
	/** 获得装备 */
	Equip(11, "Equip", "获得装备"),
	/** 装备强化 */
	EquipUp(12, "EquipUp", "装备强化"),
	/** 喂经验素材 */
	RoleExp(13, "RoleExp", "喂经验素材"),
	/** 伙伴培养 */
	RoleTrain(14, "RoleTrain", "伙伴培养"),
	/** 获得伙伴 */
	Role(15, "Role", "获得伙伴"),
	/** 一场战斗结束，参数,战役id，关卡id，输赢， 阵容id */
	BattleEnd(16, "BattleEnd", "一场战斗结束"),

	// 任务相关
	/** 完成探索关卡,可能和完成关卡合并 */
//	ExploreLevel(16, "ExploreLevel", "完成探索关卡"),
	/** 完成意识空间 */
//	AwarenessSpace(17, "AwarenessSpace", "完成意识空间"),
	CardUpGrade(18, "CardUpGrade", "升级卡牌行为，不是真正升级"),
	// 参数，id，value
	GetItem(19, "GetItem", "获取资源"),
	CostItem(20, "CostItem", "消耗资源"),

	QuestFinish(21, "QuestFinish", "完成任务"),
	WeeklyScore(22, "WeeklyScore", "获取周积分"),
	// 参数 : 商品id，数量 
	BuyItems(28, "BuyItems", "商店购买商品"),

	// 探索事件 
	/** 探索中经过一回合 */
	ExploreRound(30, "ExploreRound","探索中经过一回合"),
	/** 探索中人经过一回合 */
	ExplorePlayerRound(31, "ExplorePlayerRound", "探索中人经过一回合"),
	/** 探索中怪或者中立单位经过一回合 */
	ExploreNpcRound(32, "ExploreNpcRound", "探索中怪或者中立单位经过一回合"),
	/** 进入某探索地图 */
	ExploreMapEnter(35, "ExploreMapEnter", "进入某探索地图"),
	
	/** 出探索地图 */
	ExploreMapEnd(47, "ExploreMapEnd", "出探索地图"),
	/** 跨探索区域 */
	ExploreLevelEnd(36, "ExploreLevel", "跨探索区域"),
	/** 探索结束 */
	ExploreEnd(37, "ExploreEnd", "探索结束"),
	/** 探索开始 */
	ExploreStart(34, "ExploreStart", "探索开始"),
	
	/** 通关某探索章 */
	ExploreClearance(38, "ExploreClearance", "通关某探索章"),
	/** 探索角色复活,参数，探索角色uid */
	ExploreRoleResurrection(39, "ExploreRoleResurrection", "探索角色复活"),
	
	/** 9-战斗中损失属性值 参数1:角色uid 参数2:属性id 参数3:损失值 */
	ExploreWoundedInBattle(41, "WoundedInBattle", "战斗中损失属性值"),
	/** 10-战斗胜利 */
	ExploreBattleWin(42, "BattleWin", "战斗胜利"),
	/** 11-探索地图购买道具(不包括局间) */
	ExploreExploreStoreBuy(43, "ExploreStoreBuy", "探索地图购买道具"),
	/** 12-获取晶矿结晶 */
	ExploreMaterialReward(44, "ExploreMaterialReward", "获取晶矿结晶"),
	/** 13-宝箱、遗骸获得物品 */
	ExploreBoxAndRemainsReward(45, "ExploreBoxAndRemainsReward", "宝箱、遗骸获得物品"),
	/** 14-获得金币 */
	ExploreGetCoin(45, "ExploreGetCoin", "获得金币"),
	/** 补给值小于某值,参数： 补给值 */
	SupplyLessThanOneValue(46, "SupplyLessThanOneValue", "补给值小于某值"),
	/** 忽略,啥也不干,只是占位,为了配置数据正确 */
	Ignore(50, "Ignore", "忽略"),

	//成就相关
	/** 开启战斗 : 战役id，关卡id*/
	BattleStart(54, "BattleStart", "开启战斗"),
	/** 角色更换技能 */
	SwitchSkills(61, "SwitchSkills", "角色更换技能"),
	/** 探索后获得资源 */
	ExploreGetResources(62, "ExploreGetResources", "探索后获得资源"),
	/** 使用道具 */
	ItemUse(68, "ItemUse", "使用道具"),
	/** 收集图鉴 */
	CollectAtlas(70, "CollectAtlas", "收集图鉴"),
	/** 固定装备升级 */
	FixedEquipmentLevel(71, "FixedEquipmentLevel", "固定装备升级"),
	/** buff改变 */
	BuffChange(75, "BuffChange", "buff改变"),

	/** 某游戏事件完成 ,参数： 事件id */
	GameEventFinish(76, "GameEventFinish", "某游戏事件完成"),

	;
	private int id;
	private String name;
	private String desc;

	private EventTypeEnum(int id, String name, String desc) {
		this.id = id;
		this.name = name;
		this.desc = desc;
	}

	public static EventTypeEnum get(int id) {
		EventTypeEnum[] values = EventTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		return null;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getDesc() {
		return this.desc;
	}
}
