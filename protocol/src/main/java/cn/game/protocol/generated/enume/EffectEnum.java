package cn.game.protocol.generated.enume;

/**
 * Buff枚举表
 * 
 * 工具生成的，不要手动修改
 */
public enum EffectEnum{

	/** 后端不推送，前端也不显示 */
	Null(-1,"Null","后端不推送，前端也不显示",0),
	/** 后端推送，相关描述/图标前端会作显示 */
	OnlyView(-2,"OnlyView","后端推送，相关描述/图标前端会作显示",0),
	/** 获得资源或者道具 */
	AddOrDelGoods(1,"AddOrDelGoods","获得资源或者道具",1),
	/** 商店打折 */
	ShopDiscount(3,"ShopDiscount","商店打折",2),
	/** 商店库存数量改变 */
	ChangeShopNumbers(5,"ChangeShopNumbers","商店库存数量改变",2),
	/** 高级建筑效能减半 */
	BuildingEfficency(7,"BuildingEfficency","高级建筑效能减半",2),
	/** 随机获得资源或者道具 */
	GetRandomGoods(119,"GetRandomGoods","随机获得资源或者道具",1),
	/** 改变角色基础属性 */
	ChangeRoleAttribute(101,"ChangeRoleAttribute","改变角色基础属性",1),
	/** 状态恢复时额外改变角色的当前属性值 */
	ExtraRecoveryAttributeInStateRecovery(124,"ExtraRecoveryAttributeInStateRecovery","状态恢复时额外改变角色的当前属性值",2),
	/** 当属性恢复时，恢复量降低 */
	RecoveryStopInStateRecovery(126,"RecoveryStopInStateRecovery","当属性恢复时，恢复量降低",2),
	/** 探索中不消耗san */
	SanConsumeStopInMap(128,"SanConsumeStopInMap","探索中不消耗san",2),
	/** 恢复（K*角色当前等级）的属性值 */
	RestoreLife(1001,"RestoreLife","恢复（K*角色当前等级）的属性值",1),
	/** 在战斗中损失一定属性值时，增加一个buff */
	AddBuffWoundedInBattle(1002,"AddBuffWoundedInBattle","在战斗中损失一定属性值时，增加一个buff",2),
	/** 改变某类型buff的效果（让某类buff更强或更弱） */
	ChangeBuffEffect(1003,"ChangeBuffEffect","改变某类型buff的效果（让某类buff更强或更弱）",0),
	/** 随机改变角色基础属性 */
	ChangeRandomRoleAttribute(1004,"ChangeRandomRoleAttribute","随机改变角色基础属性",0),
	/** 增加角色等级 */
	AddRoleLevel(103,"AddRoleLevel","增加角色等级",1),
	/** 移除重伤 */
	RemoveSeriouslyInjured(117,"RemoveSeriouslyInjured","移除重伤",1),
	/** 污染值不会增长 */
	ContaminationStop(123,"ContaminationStop","污染值不会增长",2),
	/** 按类型移除buff */
	RemoveBuffByType(118,"RemoveBuffByType","按类型移除buff",1),
	/** 加buff */
	AddBuff(120,"AddBuff","加buff",1),
	/** 免疫buff状态 */
	ImmuneBuffByType(3001,"ImmuneBuffByType","免疫buff状态",2),
	/** 移除buff */
	RemoveBuff(3002,"RemoveBuff","移除buff",0),
	/** 恢复战斗中损失的属性当前值并扣除其他当前属性 */
	RecoveryAttributeLossInBattle(121,"RecoveryAttributeLossInBattle","恢复战斗中损失的属性当前值并扣除其他当前属性",1),
	/** 战斗中获得的经验值增加 */
	AddExpInBattle(132,"AddExpInBattle","战斗中获得的经验值增加",2),
	/** 角色不会死亡或重伤,留1点血 */
	NotDeadOrSeriouslyInjured(142,"NotDeadOrSeriouslyInjured","角色不会死亡或重伤,留1点血",2),
	/** 改变队伍污染值 */
	ChangeTeamContamination(108,"ChangeTeamContamination","改变队伍污染值",1),
	/** 单位立即死亡 */
	ImmediatelyDie(114,"ImmediatelyDie","单位立即死亡",1),
	/** 停止移动（禁止一切操作,只能跳过回合） */
	StopMove(115,"StopMove","停止移动（禁止一切操作,只能跳过回合）",1),
	/** 状态恢复时补给消耗翻倍 */
	ExtraSupplyConsumeInStateRecovery(125,"ExtraSupplyConsumeInStateRecovery","状态恢复时补给消耗翻倍",2),
	/** 地图商店打折(不含局间商店) */
	MapStoreDiscount(131,"MapStoreDiscount","地图商店打折(不含局间商店)",2),
	/** 探索精神:从宝箱、遗骸获得物品时，有概率使数量翻倍 */
	ExplorationSpirit(134,"ExplorationSpirit","探索精神:从宝箱、遗骸获得物品时，有概率使数量翻倍",2),
	/** 暴力开采：从晶矿获取结晶时，有概率数量翻倍，增加buff */
	ViolenceMining(135,"ViolenceMining","暴力开采：从晶矿获取结晶时，有概率数量翻倍，增加buff",2),
	/** 显示当前地图所有宝箱位置 */
	ShowBox(136,"ShowBox","显示当前地图所有宝箱位置",1),
	/** 显示当前地图所有晶矿 */
	ShowMaterial(137,"ShowMaterial","显示当前地图所有晶矿",1),
	/** 显示当前地图所有补给箱 */
	ShowSupplyBox(138,"ShowSupplyBox","显示当前地图所有补给箱",1),
	/** 显示当前地图所有怪物 */
	ShowMonster(139,"ShowMonster","显示当前地图所有怪物",1),
	/** 改变视野范围 */
	ChangeExploreView(140,"ChangeExploreView","改变视野范围",2),
	/** 奖励翻倍 */
	RewardDouble(141,"RewardDouble","奖励翻倍",2),
	/** 增加一场探索战斗 */
	AddBattleLevel(144,"AddBattleLevel","增加一场探索战斗",1),
	/** 显示当前地图所有信息 */
	MapInformation(4001,"MapInformation","显示当前地图所有信息",2),
	/** 扣除资源或者道具 */
	DelGoods(2,"DelGoods","扣除资源或者道具",1),
	/** 角色临时增加属性值（当增加不可被消耗） */
	AddRoleTemporaryAttribute(102,"AddRoleTemporaryAttribute","角色临时增加属性值（当增加不可被消耗）",1),
	/** 角色减少属性(当前) */
	DelRoleAttribute(106,"DelRoleAttribute","角色减少属性(当前)",1),
	/** 改变角色属性(上限) */
	ChangeRoleAttributeToplimit(109,"ChangeRoleAttributeToplimit","改变角色属性(上限)",2),
	/** 角色全属性增加(上限-不包括当前值属性) */
	AddRoleAllAttributeToplimit(110,"AddRoleAllAttributeToplimit","角色全属性增加(上限-不包括当前值属性)",0),
	/** 角色减少属性(上限) */
	DelRoleAttributeToplimit(111,"DelRoleAttributeToplimit","角色减少属性(上限)",2),
	/** 角色全属性减少(上限-不包括当前值属性) */
	DelRoleAllAttributeToplimit(112,"DelRoleAllAttributeToplimit","角色全属性减少(上限-不包括当前值属性)",0),
	/** 战斗中不消耗san */
	SanConsumeStopInBattle(129,"SanConsumeStopInBattle","战斗中不消耗san",2),
	/** 在战斗中损失生命值，按比例恢复EP值 */
	RecoveryEpWoundedInBattleByPercent(127,"RecoveryEpWoundedInBattleByPercent","在战斗中损失生命值，按比例恢复EP值",2),
	/** 角色全属性下降 */
	DecrAllAttribute(130,"DecrAllAttribute","角色全属性下降",2),
	/** 禁止一切操作,只能跳过回合 */
	OnlySkip(133,"OnlySkip","禁止一切操作,只能跳过回合",2),
	/** 降低视野范围 */
	DelView(143,"DelView","降低视野范围",2),
	/** 增加遗迹消耗的回合数 */
	AddRelicRound(104,"AddRelicRound","增加遗迹消耗的回合数",1),
	/** 遗迹奖励减少 */
	DelRelicReward(105,"DelRelicReward","遗迹奖励减少",1),
	/** 遗迹中增加资源或者道具 */
	RelicGetGoods(113,"RelicGetGoods","遗迹中增加资源或者道具",1),
	/** 道具店可出售都得道具库数量减少 */
	DelItemShopNumbers(6,"DelItemShopNumbers","道具店可出售都得道具库数量减少",2),
	/** 武器店在下次出发前打折 */
	ArmoryDiscount(4,"ArmoryDiscount","武器店在下次出发前打折",2),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 描述 */
	private String desc ; 
	/** 状态类型 */
	private int type ; 

	private EffectEnum(int id, String name, String desc, int type) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
		this.type = type; 
	}
	
	public static EffectEnum get(int id) {
		EffectEnum[] values = EffectEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【EffectEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static EffectEnum getNullable(int id) {
		EffectEnum[] values = EffectEnum.values();
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
}
