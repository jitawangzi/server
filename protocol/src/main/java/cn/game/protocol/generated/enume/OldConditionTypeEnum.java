package cn.game.protocol.generated.enume;

/**
 * 条件枚举
 * 
 * 工具生成的，不要手动修改
 */
public enum OldConditionTypeEnum{

	/** 完成{0}事件 */
	EventComplete(1,"EventComplete","完成{0}事件",true),
	/** 完成{0}关卡 */
	GivenLevelComplete(2,"GivenLevelComplete","完成{0}关卡",true),
	/** 关卡通关 */
	LevelComplete(3,"LevelComplete","关卡通关",false),
	/** 阵亡人数 */
	DeathToll(4,"DeathToll","阵亡人数",false),
	/** 剩余血量百分比 */
	RemainHp(5,"RemainHp","剩余血量百分比",false),
	/** 亲密度等级 */
	FriendlyLevel(6,"FriendlyLevel","亲密度等级",true),
	/** 限定卡牌 */
	RoleLimit(7,"RoleLimit","限定卡牌",true),
	/** 限定职业 */
	ProfessionLimit(8,"ProfessionLimit","限定职业",true),
	/** 限定阵营 */
	CampLimit(9,"CampLimit","限定阵营",true),
	/** 完成指定探索章 */
	GivenExploreChapterComplete(10,"GivenExploreChapterComplete","完成指定探索章",true),
	/** 完成{0}关卡 */
	GivenExploreLevelComplete(11,"GivenExploreLevelComplete","完成{0}关卡",true),
	/** 完成探索关卡{1}次 */
	ExploreLevelTimes(12,"ExploreLevelTimes","完成探索关卡{1}次",false),
	/** 完成意识空间{1}次 */
	AwarenessSpaceTimes(13,"AwarenessSpaceTimes","完成意识空间{1}次",false),
	/** 卡牌升级{1}次 */
	CardUpGradeTimes(14,"CardUpGradeTimes","卡牌升级{1}次",false),
	/** 获得{0}{1} */
	GetItemNumbers(15,"GetItemNumbers","获得{0}{1}",false),
	/** 消耗{0}{1} */
	CostItemNumbers(16,"CostItemNumbers","消耗{0}{1}",false),
	/** 完成每日任务{1}条 */
	DailyTasksNumbers(17,"DailyTasksNumbers","完成每日任务{1}条",false),
	/** 日常任务中获得周积分{1} */
	WeeklyScore(18,"WeeklyScore","日常任务中获得周积分{1}",false),
	/** 完成意识空间{0}关卡 */
	GivenAwarenessSpaceComplete(19,"GivenAwarenessSpaceComplete","完成意识空间{0}关卡",false),
	/** 主线关卡内回合胜利 */
	RoundsWin(20,"RoundsWin","主线关卡内回合胜利",false),
	/** 主线关卡死亡小于人数 */
	RoundsDeadNumber(21,"RoundsDeadNumber","主线关卡死亡小于人数",false),
	/** 完成任务{0} */
	Mission(22,"Mission","完成任务{0}",false),
	/** {2}击杀{3}{0}{1}个 */
	HitMainlineMonster(101,"HitMainlineMonster","{2}击杀{3}{0}{1}个",true),
	/** {2}与{0}进行{3}{1}次 */
	TalkNPC(102,"TalkNPC","{2}与{0}进行{3}{1}次",false),
	/** 与{2}{0}交互{1}次 */
	UserObject(103,"UserObject","与{2}{0}交互{1}次",false),
	/** 到达{0}的{2}坐标 */
	ReachTheTarget(104,"ReachTheTarget","到达{0}的{2}坐标",false),
	/** 解锁{0} */
	UnlockingArea(105,"UnlockingArea","解锁{0}",false),
	/** 持有{0}{1}件 */
	HoldItems(106,"HoldItems","持有{0}{1}件",true),
	/** 获得{0}{1}件 */
	GetItems(107,"GetItems","获得{0}{1}件",true),
	/** {2}{3}下购买{0}{1}件 */
	BuyItems(108,"BuyItems","{2}{3}下购买{0}{1}件",false),
	/** 解锁大事件关联的线索 */
	BigEventClue(109,"BigEventClue","解锁大事件关联的线索",false),
	/** 限制当前等级 */
	LimitLevel(501,"LimitLevel","限制当前等级",false),
	/** 获取{0}解锁 */
	LimitRole(502,"LimitRole","获取{0}解锁",false),
	/** 获得任意{0}阵营下任意一名角色 */
	GetCampRole(1001,"GetCampRole","获得任意{0}阵营下任意一名角色",false),
	/** 击杀当前地图{0}{1}个 */
	HitExploreMonster(601,"HitExploreMonster","击杀当前地图{0}{1}个",true),
	/** 击杀任意怪物组{1}个 */
	HitExploreAnyMonsterGroup(602,"HitExploreAnyMonsterGroup","击杀任意怪物组{1}个",true),
	/** 累计完成{1}个可统计的具体事件 */
	TotalFinishGameEvent(604,"TotalFinishGameEvent","累计完成{1}个可统计的具体事件",true),
	/** 情报{0}完成 */
	IntelligenceFinish(605,"IntelligenceFinish","情报{0}完成",true),
	/** 某事件成功完成(没有脱离) */
	GameEventFinishSuccessfully(701,"GameEventFinishSuccessfully","某事件成功完成(没有脱离)",false),
	/** 仅消灭哪些boss */
	EliminateOnly(702,"EliminateOnly","仅消灭哪些boss",false),
	/** 完成任意一条线索 */
	GetAnyClues(703,"GetAnyClues","完成任意一条线索",false),
	/** 意志点数大于{1} */
	WillPoints(704,"WillPoints","意志点数大于{1}",false),
	/** 当前处于哪个章节 */
	CurrentChapter(705,"CurrentChapter","当前处于哪个章节",false),
	/** 军械库等级大于0 */
	ArmoryGrade(706,"ArmoryGrade","军械库等级大于0",false),
	/** 某事件进行中 */
	GameEventConduct(707,"GameEventConduct","某事件进行中",false),
	/** 一次探索中仅完成那些关卡 */
	ExploreLevelFinishOnly(708,"ExploreLevelFinishOnly","一次探索中仅完成那些关卡",false),
	/** 完成关卡 */
	ExploreLevelFinish(709,"ExploreLevelFinish","完成关卡",false),
	/** 探索支线事件线成功完成 */
	ExploreEventBranchFinishSuccessfully(801,"ExploreEventBranchFinishSuccessfully","探索支线事件线成功完成",false),
	/** 探索支线事件线被放弃(脱离) */
	ExploreEventBranchGiveup(802,"ExploreEventBranchGiveup","探索支线事件线被放弃(脱离)",false),
	/** 当前地图探索度达到100% */
	FullView(2001,"FullView","当前地图探索度达到100%",true),
	/** 角色拥有某类型buff */
	RoleGetBuff(2002,"RoleGetBuff","角色拥有某类型buff",true),
	/** 在表世界 */
	TableWorld(2003,"TableWorld","在表世界",false),
	/** 在里世界 */
	InnerWorld(2004,"InnerWorld","在里世界",false),
	/** 是否是毁灭者（Monster表中类型为3的） */
	IsDestroyer(2005,"IsDestroyer","是否是毁灭者（Monster表中类型为3的）",false),
	/** 获得新角色时 */
	GetRole(7001,"GetRole","获得新角色时",false),
	/** 任意角色累计造成伤害 */
	CumulativeDamage(7002,"CumulativeDamage","任意角色累计造成伤害",true),
	/** 任意角色累计击杀怪物(最后一击){1}只 */
	CumulativeKillMonsters(7003,"CumulativeKillMonsters","任意角色累计击杀怪物(最后一击){1}只",true),
	/** 任意角色累计对{2}怪物造成伤害{1} */
	CumulativeToMonstersDamage(7004,"CumulativeToMonstersDamage","任意角色累计对{2}怪物造成伤害{1}",true),
	/** 体型超过{2}范围的怪物造成伤害 */
	CumulativeToShapeMonstersDamage(7005,"CumulativeToShapeMonstersDamage","体型超过{2}范围的怪物造成伤害",true),
	/** 任意角色累计{1}场战斗中,每场战斗进行攻击的次数大于{2} */
	CumulativeAttackTimes(7006,"CumulativeAttackTimes","任意角色累计{1}场战斗中,每场战斗进行攻击的次数大于{2}",true),
	/** 任意角色累计{1}场战斗中,每场战斗成为我方第一个进行攻击的 */
	CumulativeFirstAttackTimes(7007,"CumulativeFirstAttackTimes","任意角色累计{1}场战斗中,每场战斗成为我方第一个进行攻击的",true),
	/** 任意角色累计{1}场战斗中，每场战斗受到伤害的次数大于{2}次 */
	CumulativeInjuredTimes(7008,"CumulativeInjuredTimes","任意角色累计{1}场战斗中，每场战斗受到伤害的次数大于{2}次",true),
	/** 任意角色累计{1}场战斗中，每场战斗成为我方第一个受到攻击的 */
	CumulativeFirstInjuredTimes(7009,"CumulativeFirstInjuredTimes","任意角色累计{1}场战斗中，每场战斗成为我方第一个受到攻击的",true),
	/** 任意角色累计{1}场战斗中，每场战斗第一次行动时，完成一次击杀（最后一击） */
	CumulativeFirstAttackDie(7010,"CumulativeFirstAttackDie","任意角色累计{1}场战斗中，每场战斗第一次行动时，完成一次击杀（最后一击）",true),
	/** 任意角色累计{1}场中，每场战斗开始时血量>{2}%,战斗结束时血量<={3}% */
	CumulativeHpRange(7011,"CumulativeHpRange","任意角色累计{1}场中，每场战斗开始时血量>{2}%,战斗结束时血量<={3}%",true),
	/** 任意角色累计{1}场战斗中，每场战斗开始时血量<={3}% */
	CumulativeHpUpperlimit(7012,"CumulativeHpUpperlimit","任意角色累计{1}场战斗中，每场战斗开始时血量<={3}%",true),
	/** 任意角色累计{1}场战斗中，每场战斗受到{0}buff类型的次数>{2}次 */
	CumulativeBuff(7013,"CumulativeBuff","任意角色累计{1}场战斗中，每场战斗受到{0}buff类型的次数>{2}次",true),
	/** 任意角色累计濒死状态{1}次 */
	CumulativeDieTimes(7014,"CumulativeDieTimes","任意角色累计濒死状态{1}次",true),
	/** 任意角色累计{1}次，受到单次伤害>自身最大生命值{2}% */
	CumulativeInjuredLowerLimit(7015,"CumulativeInjuredLowerLimit","任意角色累计{1}次，受到单次伤害>自身最大生命值{2}%",true),
	/** 任意角色累计{1}次，使用恢复类道具治疗 */
	CumulativeUseItem(7016,"CumulativeUseItem","任意角色累计{1}次，使用恢复类道具治疗",true),
	/** 任意角色{1}次SAN值小于等于{2} */
	SANValueRange(7017,"SANValueRange","任意角色{1}次SAN值小于等于{2}",true),
	/** 任意角色累计{1}场战斗中，每场战斗击杀超过{2}只怪物（最后一击） */
	CumulativeTimesKillMonsters(7018,"CumulativeTimesKillMonsters","任意角色累计{1}场战斗中，每场战斗击杀超过{2}只怪物（最后一击）",true),
	/** 任意角色累计{1}场战斗中，每场战斗对同一个目标造成伤害次数>{2}次 */
	CumulativeKillTimes(7019,"CumulativeKillTimes","任意角色累计{1}场战斗中，每场战斗对同一个目标造成伤害次数>{2}次",true),
	/** 任意角色累计{1}次，被敌方击退 */
	CumulativeKnockback(7020,"CumulativeKnockback","任意角色累计{1}次，被敌方击退",true),
	/** 任意角色累计{1}次，在自身有{2}状态的情况下攻击目标 */
	CumulativeHitTarget(7021,"CumulativeHitTarget","任意角色累计{1}次，在自身有{2}状态的情况下攻击目标",true),
	/** 任意角色累计{1}次，攻击被敌方护卫 */
	CumulativeBeEscorted(7022,"CumulativeBeEscorted","任意角色累计{1}次，攻击被敌方护卫",true),
	/** 任意角色累计{1}次，攻击被敌方闪避 */
	CumulativeBeDodged(7023,"CumulativeBeDodged","任意角色累计{1}次，攻击被敌方闪避",true),
	/** 任意角色累计{1}次，攻击触发暴击 */
	CumulativeCritical(7024,"CumulativeCritical","任意角色累计{1}次，攻击触发暴击",true),
	/** 任意角色累计{1}次，被敌方{2} */
	CumulativeDeBuff(7025,"CumulativeDeBuff","任意角色累计{1}次，被敌方{2}",true),
	/** 首次进入XX区域 */
	FirstEntryArea(8001,"FirstEntryArea","首次进入XX区域",false),
	/** 首次进入地牢 */
	FirstEntryDungeon(8002,"FirstEntryDungeon","首次进入地牢",false),
	/** 首次到达局间 */
	FirstEntryInterexchange(8003,"FirstEntryInterexchange","首次到达局间",false),
	/** 初次到达主城 */
	FirstEntryMainCity(8004,"FirstEntryMainCity","初次到达主城",false),
	/** 初次遭遇{0}指定怪物组ID */
	FirstEncounterMonster(8005,"FirstEncounterMonster","初次遭遇{0}指定怪物组ID",false),
	/** 交互物{0}交互{1}次 */
	CumulativeInteractionTimes(8007,"CumulativeInteractionTimes","交互物{0}交互{1}次",false),
	/** 累积局间回复血量共计{1} */
	InterexchangeBloodReturning(8008,"InterexchangeBloodReturning","累积局间回复血量共计{1}",false),
	/** 礼物卡传递次数共计{1}次 */
	GiftCardTransmitTimes(8009,"GiftCardTransmitTimes","礼物卡传递次数共计{1}次",false),
	/** 探索中购买道具花费{0}{1} */
	ExploreBuyItemCost(8010,"ExploreBuyItemCost","探索中购买道具花费{0}{1}",false),
	/** 探索中购买装备{1}次(包括探索中商人和局间商人) */
	ExploreBuyEquipmentTimes(8011,"ExploreBuyEquipmentTimes","探索中购买装备{1}次(包括探索中商人和局间商人)",false),
	/** 使用折扣后的价格在主城购买商品 */
	DiscountBuyCommodity(8012,"DiscountBuyCommodity","使用折扣后的价格在主城购买商品",false),
	/** 首次在主城建造设施 */
	FirstMainCityBuilding(8013,"FirstMainCityBuilding","首次在主城建造设施",false),
	/** 完成所有{0}线索 */
	CompleteIndustrialAreaEvent(8014,"CompleteIndustrialAreaEvent","完成所有{0}线索",false),
	/** 一次探索中走过{1}回合 */
	ExploreNumberofRounds(8015,"ExploreNumberofRounds","一次探索中走过{1}回合",false),
	/** 任意角色切换所有技能 */
	SwitchAllCharacterSkills(8016,"SwitchAllCharacterSkills","任意角色切换所有技能",false),
	/** 一次探索中获得{0}{1}(包括评价探索) */
	ExploreGetResources(8017,"ExploreGetResources","一次探索中获得{0}{1}(包括评价探索)",false),
	/** 在一张地图中累积超过{1}回合 */
	ExploreMapNumberofRounds(8018,"ExploreMapNumberofRounds","在一张地图中累积超过{1}回合",false),
	/** 首次破坏所有掩射器 */
	DestroyAllSpaceWedge(8019,"DestroyAllSpaceWedge","首次破坏所有掩射器",false),
	/** 主动放弃一次探索 */
	GiveUpanExploration(8020,"GiveUpanExploration","主动放弃一次探索",false),
	/** 连续2个区域不被怪物先手 */
	MultipleAreasNotToBeFirst(8022,"MultipleAreasNotToBeFirst","连续2个区域不被怪物先手",false),
	/** 所有队员SAN值涨满且战斗胜利{1}次 */
	SanValueInflationVictory(8023,"SanValueInflationVictory","所有队员SAN值涨满且战斗胜利{1}次",false),
	/** 满血击杀任意怪物组{0}次 */
	FullofBloodMonster(8024,"FullofBloodMonster","满血击杀任意怪物组{0}次",false),
	/** 在战斗中被任意怪物团灭{1}次 */
	BattleBeMonstersDisappear(8025,"BattleBeMonstersDisappear","在战斗中被任意怪物团灭{1}次",false),
	/** 击杀{1}个不同区域的{0}BOSS */
	KillDifferentAreasMonsters(8026,"KillDifferentAreasMonsters","击杀{1}个不同区域的{0}BOSS",false),
	/** 击杀{1}个{0} */
	KillMonstersNumbers(8027,"KillMonstersNumbers","击杀{1}个{0}",false),
	/** 任意一名英雄击杀{1}只怪物 */
	CumulativeKillNumbers(8028,"CumulativeKillNumbers","任意一名英雄击杀{1}只怪物",false),
	/** 战斗后只剩下{0}位英雄 */
	BattleSurplusNumbers(8029,"BattleSurplusNumbers","战斗后只剩下{0}位英雄",false),
	/** 任意一位英雄血量小于百分比{2}的情况下连续参与{1}场战斗而不死 */
	FightUndead(8030,"FightUndead","任意一位英雄血量小于百分比{2}的情况下连续参与{1}场战斗而不死",false),
	/** 单场战斗中一名英雄对敌人连续造成{1}次致命一击 */
	DeathBlow(8031,"DeathBlow","单场战斗中一名英雄对敌人连续造成{1}次致命一击",false),
	/** 在地牢赢得{1}场战斗 */
	DungeonBattleNumbers(8032,"DungeonBattleNumbers","在地牢赢得{1}场战斗",false),
	/** 所有队员一次探索中参与连续超过{1}场(不能更换或死亡) */
	ContinuityPartakeBattle(8033,"ContinuityPartakeBattle","所有队员一次探索中参与连续超过{1}场(不能更换或死亡)",false),
	/** 触发伏击{1}次 */
	AmbushNumbers(8034,"AmbushNumbers","触发伏击{1}次",false),
	/** 任意一名英雄一回合内击杀{1}只怪物 */
	RoundKillNumbers(8035,"RoundKillNumbers","任意一名英雄一回合内击杀{1}只怪物",false),
	/** 一击被秒(战斗开始后任意英雄被怪物秒杀)，同时还要胜利 */
	KillWithOneBlow(8036,"KillWithOneBlow","一击被秒(战斗开始后任意英雄被怪物秒杀)，同时还要胜利",false),
	/** 成功使用道具效果次数 */
	ItemRemovalBuff(8037,"ItemRemovalBuff","成功使用道具效果次数",false),
	/** 击败{0}怪物（主要是大事件怪物也可以是boss） */
	DefeatMonsterId(8038,"DefeatMonsterId","击败{0}怪物（主要是大事件怪物也可以是boss）",false),
	/** 收集图鉴{1}张 */
	CollectAtlas(8039,"CollectAtlas","收集图鉴{1}张",false),
	/** 背包空间提升{1}格 */
	BackpackSpaceImproved(8040,"BackpackSpaceImproved","背包空间提升{1}格",false),
	/** 任意一件固定装备达到{0}级 */
	FixedEquipmentLevel(8041,"FixedEquipmentLevel","任意一件固定装备达到{0}级",false),
	/** 任意一位英雄解锁所有扎营卡片 */
	GiftCardUnlocking(8042,"GiftCardUnlocking","任意一位英雄解锁所有扎营卡片",false),
	/** 激活{0}次意志点数 */
	ActivationWillPoints(8043,"ActivationWillPoints","激活{0}次意志点数",false),
	/** 所有建筑升至满级 */
	AllBuildingFull(8044,"AllBuildingFull","所有建筑升至满级",false),
	/** 阻止小怪原地复活{1}次 */
	PreventResurrection(8045,"PreventResurrection","阻止小怪原地复活{1}次",false),
	/** 通关任意地貌下的{2}模式{1} */
	ClearanceDungeon(8046,"ClearanceDungeon","通关任意地貌下的{2}模式{1}",false),
	/** 所有饰品达到{0}色稀有度 */
	JewelryGrade(8047,"JewelryGrade","所有饰品达到{0}色稀有度",false),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 描述 */
	private String desc ; 
	/** 枚举类型 */
	private boolean addUp ; 

	private OldConditionTypeEnum(int id, String name, String desc, boolean addUp) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
		this.addUp = addUp; 
	}
	
	public static OldConditionTypeEnum get(int id) {
		OldConditionTypeEnum[] values = OldConditionTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ConditionTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static OldConditionTypeEnum getNullable(int id) {
		OldConditionTypeEnum[] values = OldConditionTypeEnum.values();
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
	public boolean getAddUp(){
		return this.addUp;
	}
}
