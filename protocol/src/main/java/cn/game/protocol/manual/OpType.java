package cn.game.protocol.manual;

/**
 * @Description 操作类型
 * @date 2020年10月9日 上午10:46:02
 * @author SYQ
 */
public enum OpType{

	/** 购买商品 */
	BuyGoods(6, "BuyGoods", "购买商品"),
	/** 抽卡 */
	Draw(11, "Draw", "抽卡"),
	/** GM操作 */
	GM(12, "GM", "GM操作"),
	/** 效果产生的 */
	Effect(16, "Effect", "效果产生的"),
	/** 选择事件 */
	EventOptin(17, "EventOptin", "选择事件"),
	/** 装备强化 */
	EquipmentStrengthen(19, "EquipmentStrengthen", "装备强化"),

	/** 英雄升级 */
	HeroLevelUp(50, "HeroLevelUp", "英雄升级"),
	HeroConflate(51, "HeroConflate", "英雄合成"),

	// 用来增加奖励的。
	Init(100, "Init", "初始化增加的"),
	Quest(101, "Quest", "任务奖励"),
	ShopTrade(102, "ShopTrade", "商店购买"),
	Patrol(103, "Patrol", "巡逻，挂机"),
	BattleEnd(104, "BattleEnd", "战役结束"),
	HeroLvReset(105, "HeroLvReset", "英雄等级重置"),
	HeroQualityReset(106, "HeroQualityReset", "英雄品质重置"),
	MonthCardBuy(107, "MonthCardBuy", "月卡购买"),
	MonthCardDay(108, "MonthCardDay", "月卡每日奖励"),
	ChapterGift(109, "ChapterGift", "章节礼包"),

	Mail(110, "Mail", "邮件"),
	Test(111, "Test", "测试添加"),

	TimerRecovery(112, "TimerRecovery", "定时恢复"),
	NewHeroReward(113, "NewHeroReward", "新英雄奖励"),

	PlayerLevelUp(114, "PlayerLevelUp", "升级奖励"),

	CloudBox(115, "CloudBox", "小云宝箱"),
	FirstCharge(116, "FirstCharge", "首冲"),

	None(999, "None", "未定义"),

    ;
	private int id ; 
	private String name ; 
	private String desc ; 

	private OpType(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static OpType get(int id) {
		OpType[] values = OpType.values();
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
