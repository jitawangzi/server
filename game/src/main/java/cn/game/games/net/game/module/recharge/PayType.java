package cn.game.games.net.game.module.recharge;

public enum PayType {
	/** 单笔充值 */
	FirstCharge(1),
	SingleCharge(1),

	/** 金钱豹爆爆 */
	ActivityJQB(2),

	/** 章节礼包 */
	ChapterPacks(3),

	/** 月卡 */
	MonthCard(4),

	/** 商店商品 */
	ShopItem(5),

	/** 通行证 */
	FundPass(6),

	/** 普通充值兑换 */
	Recharge(7),

	/** Vip礼包 */
	VipGift(8);

	private int id;

	PayType(int id) {
		this.id = id;
	}
}