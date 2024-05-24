package cn.game.protocol.generated.enume;

/**
 * 资产表
 * 
 * 工具生成的，不要手动修改
 */
public enum Asset{

	/** 元宝 */
	diamond(100001,"diamond","元宝",1,6,"有钱能使鬼推磨","recharge_icon_01"),
	/** 金币 */
	gold(100002,"gold","金币",1,6,"大唐统一货币","recharge_icon_07"),
	/** 银币 */
	silver(100003,"silver","银币",1,1,"大唐统一购置装备货币","recharge_icon_08"),
	/** 经验 */
	playerExp(100201,"playerExp","经验",2,5,"提升玩家等级之用","com_icon_jingyan"),
	/** 每日任务活跃度 */
	DailyPoint(100202,"DailyPoint","每日任务活跃度",1,5,"获得指定数量后可以领取奖励","task_icon_rihuoyue"),
	/** 每周任务活跃度 */
	WeeklyPoint(100203,"WeeklyPoint","每周任务活跃度",1,5,"获得指定数量后可以领取奖励","task_icon_zhouhuoyue"),
	/** 7日任务活跃度 */
	SevenDaysPoint(100204,"SevenDaysPoint","7日任务活跃度",1,5,"获得指定数量后可以领取奖励","sevendaygift_icon_integral"),
	/** 通行证经验 */
	FundPass(100205,"FundPass","通行证经验",1,5,"获得指定数量后可以领取奖励",""),
	/** 灵韵珠 */
	PotentialLvM(100206,"PotentialLvM","灵韵珠",1,4,"用于潜力升级","icon_hulu_cai"),
	/** 蕴灵液 */
	PotentialBreakM(100207,"PotentialBreakM","蕴灵液",1,5,"用于潜力突破","icon_hulu_jin"),
	/** 体力 */
	playerEnergy(100301,"playerEnergy","体力",3,5,"吃了人参果，阎王能打过","physicalpower_icon_01"),
	/** 日常活动积分 */
	dailyIntegral(100302,"dailyIntegral","日常活动积分",1,6,"参加日常活动获得积分","icon_hulu_cai"),
    ;
	/** ID */
	public final int ID ; 
	/** 物品英文名 */
	public final String Name ; 
	/** 物品名称 */
	public final String Desc ; 
	/** 物品类型 1=普通货币 2=经验 3=体力 */
	public final int Type ; 
	/** 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一 */
	public final int Quality ; 
	/** 物品tips */
	public final String Tips ; 
	/** 图标Icon 调用：west\src\First_party\art\xiyou UI\icon_图标 */
	public final String Icon ; 

	private Asset(int ID, String Name, String Desc, int Type, int Quality, String Tips, String Icon) {
		this.ID = ID; 
		this.Name = Name; 
		this.Desc = Desc; 
		this.Type = Type; 
		this.Quality = Quality; 
		this.Tips = Tips; 
		this.Icon = Icon; 
	}
	
	public static Asset get(int id) {
		Asset[] values = Asset.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【Asset】枚举表的" + "id【" + id + "】不存在");
	}

	public static Asset getNullable(int id) {
		Asset[] values = Asset.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		return null;
	}

}
