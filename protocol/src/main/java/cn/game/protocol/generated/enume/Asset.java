package cn.game.protocol.generated.enume;

/**
 * 资产表
 * 
 * 工具生成的，不要手动修改
 */
public enum Asset{

	/** 元宝 */
	diamond(100001,"diamond","元宝",1,4,"游戏中的万用货币","recharge_icon_01"),
	/** 金币 */
	gold(100002,"gold","金币",1,3,"游戏中购买物品的标准货币","recharge_icon_07"),
	/** 经验 */
	playerExp(100201,"playerExp","经验",2,3,"提升玩家等级之用","com_icon_jingyan"),
	/** 每日任务活跃度 */
	DailyPoint(100202,"DailyPoint","每日任务活跃度",1,3,"获得指定数量后可以领取奖励","task_icon_rihuoyue"),
	/** 每周任务活跃度 */
	WeeklyPoint(100203,"WeeklyPoint","每周任务活跃度",1,3,"获得指定数量后可以领取奖励","task_icon_zhouhuoyue"),
	/** 体力 */
	playerEnergy(100301,"playerEnergy","体力",3,3,"用于挑战、扫荡关卡，获取关卡奖励","physicalpower_icon_01"),
    ;
	/** ID */
	public final int ID ; 
	/** 物品英文名 */
	public final String Name ; 
	/** 物品名称 */
	public final String Desc ; 
	/** 物品类型 1=普通货币 2=经验 3=体力 */
	public final int Type ; 
	/** 品质 */
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
