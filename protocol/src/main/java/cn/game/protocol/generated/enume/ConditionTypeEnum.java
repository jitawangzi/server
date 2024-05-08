package cn.game.protocol.generated.enume;

/**
 * 条件类型
 * 
 * 工具生成的，不要手动修改
 */
public enum ConditionTypeEnum{

	/**  */
	PlayerLevel(1,"PlayerLevel"),
	/**  */
	PlayerCombat(2,"PlayerCombat"),
	/**  */
	AccumulatedRecharge(3,"AccumulatedRecharge"),
	/**  */
	ChapterFinish(4,"ChapterFinish"),
	/**  */
	MonthCard(5,"MonthCard"),
	/**  */
	ParticipateChapter(6,"ParticipateChapter"),
	/**  */
	EliteFinish(7,"EliteFinish"),
	/**  */
	ReceiveStamina(8,"ReceiveStamina"),
	/**  */
	ExertsStamina(9,"ExertsStamina"),
	/**  */
	WatchAds(10,"WatchAds"),
	/**  */
	KillMonsters(11,"KillMonsters"),
	/**  */
	KillBoss(12,"KillBoss"),
	/**  */
	ConsumesDiamonds(13,"ConsumesDiamonds"),
	/**  */
	RechargeCnt(14,"RechargeCnt"),
	/**  */
	EarnHero(15,"EarnHero"),
	/**  */
	BreakHero(16,"BreakHero"),
	/**  */
	UpgradeHero(17,"UpgradeHero"),
	/**  */
	StorePurchases(18,"StorePurchases"),
	/**  */
	Gacha(19,"Gacha"),
	/**  */
	CumulativeLogins(20,"CumulativeLogins"),
	/**  */
	LogGame(21,"LogGame"),
	/**  */
	QuickHangup(22,"QuickHangup"),
	/**  */
	ReceiveHangup(23,"ReceiveHangup"),
	/**  */
	HeroLevel(24,"HeroLevel"),
    ;
	/** id */
	public final int ID ; 
	/** 英文名称 */
	public final String name ; 

	private ConditionTypeEnum(int ID, String name) {
		this.ID = ID; 
		this.name = name; 
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
