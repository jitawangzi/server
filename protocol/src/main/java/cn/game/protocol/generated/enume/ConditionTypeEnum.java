package cn.game.protocol.generated.enume;

/**
 * 条件类型
 * 
 * 工具生成的，不要手动修改
 */
public enum ConditionTypeEnum{

	/** 用户等级 */
	PlayerLevel(1,"PlayerLevel","用户等级"),
	/** 累计充值 */
	AccumulatedRecharge(3,"AccumulatedRecharge","累计充值"),
	/** 通关章节 */
	ChapterFinish(4,"ChapterFinish","通关章节"),
	/** 参与主线章节 */
	ParticipateChapter(6,"ParticipateChapter","参与主线章节"),
	/** 通关历练（精英） */
	EliteFinish(7,"EliteFinish","通关历练（精英）"),
	/** 领取体力 */
	ReceiveStamina(8,"ReceiveStamina","领取体力"),
	/** 消耗体力 */
	ExertsStamina(9,"ExertsStamina","消耗体力"),
	/** 观看广告 */
	WatchAds(10,"WatchAds","观看广告"),
	/** 击杀怪物（小怪+头目） */
	KillMonsters(11,"KillMonsters","击杀怪物（小怪+头目）"),
	/** 击杀首领怪物 */
	KillBoss(12,"KillBoss","击杀首领怪物"),
	/** 消耗钻石 */
	ConsumesDiamonds(13,"ConsumesDiamonds","消耗钻石"),
	/** 充值 */
	RechargeCnt(14,"RechargeCnt","充值"),
	/** 获得英雄 */
	EarnHero(15,"EarnHero","获得英雄"),
	/** 突破英雄 */
	BreakHero(16,"BreakHero","突破英雄"),
	/** 英雄升级 */
	UpgradeHero(17,"UpgradeHero","英雄升级"),
	/** 黑市购买物品 */
	StorePurchases(18,"StorePurchases","黑市购买物品"),
	/** 抽卡 */
	Gacha(19,"Gacha","抽卡"),
	/** 登录游戏 */
	CumulativeLogins(20,"CumulativeLogins","登录游戏"),
	/** 登录游戏 */
	LogGame(21,"LogGame","登录游戏"),
	/** 快速挂机 */
	QuickHangup(22,"QuickHangup","快速挂机"),
	/** 领取挂机奖励 */
	ReceiveHangup(23,"ReceiveHangup","领取挂机奖励"),
	/** 英雄等级 */
	HeroLevel(24,"HeroLevel","英雄等级"),
    ;
	/** id */
	public final int ID ; 
	/** 英文名称 */
	public final String name ; 
	/** 说明 */
	public final String desc ; 

	private ConditionTypeEnum(int ID, String name, String desc) {
		this.ID = ID; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static ConditionTypeEnum get(int id) {
		ConditionTypeEnum[] values = ConditionTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ConditionTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static ConditionTypeEnum getNullable(int id) {
		ConditionTypeEnum[] values = ConditionTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		return null;
	}

}
