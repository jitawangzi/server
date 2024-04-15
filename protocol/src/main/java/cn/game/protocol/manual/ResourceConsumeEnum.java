package cn.game.protocol.manual;

/**
 * @Description 资源消耗类型
 * @date 2020年10月9日 上午10:46:02
 * @author SYQ
 */
public enum ResourceConsumeEnum{

	/** 源质解锁 */
	OriginUnlock(1, "OriginUnlock", "源质解锁"),
	/** 源质升级 */
	OriginUpgrade(2, "OriginUpgrade", "源质升级"),
	/** 兑换角色皮肤 */
	RoleSkin(3, "RoleSkin", "兑换角色皮肤"),
	/** 意识核心 */
	CoreUnit(4, "CoreUnit", "意识核心"),
	/** 芯片 */
	Chip(5, "Chip", "芯片"),
	/** 购买商品 */
	BuyGoods(6, "BuyGoods", "购买商品"),
	/** Battlepass购买等级 */
	BattlePass(7, "BattlePass", "battlepass"),
	/** 购买体力道具 */
	BuyPower(7, "BuyPowerItem", "购买体力道具"),
	/** 打副本消耗 */
	BattleLevel(8, "BattleLevel", "打副本消耗"),
	/** 灵武 */
	SoulWeapon(9, "SoulWeapon", "灵武"),
	/** 奇点 */
	Singularity(10, "Singularity", "奇点"),
	/** 抽卡 */
	Draw(11, "Draw", "抽卡"),
	/** 主线npc */
	MAINLINENPC(12, "MAINLINENPC", "旅店休息"),
	/** GM操作 */
	GM(12, "GM", "GM操作"),
	/** 主城建造 */
	BuildingBuild(13, "BuildingBuild", "主城建造"),
	/** 主城升级 */
	BuildingLevelUp(14, "BuildingLevelUp", "主城升级"),
	/** 主城天赋解锁 */
	OccupationTalentUnlock(15, "OccupationTalentUnlock", "主城天赋解锁"),
	/** 主城添加buff */
	MainCityAddBuff(16, "MainCityAddBuff", "主城添加buff"),
	/** 效果产生的 */
	Effect(16, "Effect", "效果产生的"),
	/** 选择事件 */
	EventOptin(17, "EventOptin", "选择事件"),
	/** 局间阶段-洗牌 */
	ShuffleStrategyCards(18, "ShuffleStrategyCards", "局间阶段-洗牌"),
	/** 装备强化 */
	EquipmentStrengthen(19, "EquipmentStrengthen", "装备强化"),
	/** 交互消耗 */
	Interactive(20, "Interactive", "交互消耗"),

	/** 英雄升级 */
	HeroLevelUp(50, "HeroLevelUp", "英雄升级"),
	HeroConflate(51, "HeroConflate", "英雄合成"),

	None(999, "None", "未定义"),

    ;
	private int id ; 
	private String name ; 
	private String desc ; 

	private ResourceConsumeEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static ResourceConsumeEnum get(int id) {
		ResourceConsumeEnum[] values = ResourceConsumeEnum.values();
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
