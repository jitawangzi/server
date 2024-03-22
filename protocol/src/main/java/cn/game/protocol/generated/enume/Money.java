package cn.game.protocol.generated.enume;

/**
 * 货币表
 * 
 * 工具生成的，不要手动修改
 */
public enum Money{

	/** 钻石 */
	diamond(100001,"diamond","钻石",1,1,"用于扭蛋机中抽取英雄",""),
	/** 金币 */
	gold(100002,"gold","金币",1,1,"有钱能使小熊猫推磨",""),
	/** 玩家经验 */
	playerExp(100201,"playerExp","玩家经验",2,1,"",""),
	/** 捕虫经验 */
	bugExp(100202,"bugExp","捕虫经验",2,1,"",""),
	/** 玩家体力 */
	playerEnergy(100301,"playerEnergy","玩家体力",3,1,"",""),
	/** 端午节活动体力 */
	dwjActivityEnergy(100302,"dwjActivityEnergy","端午节活动体力",3,1,"",""),
	/** 战士升级材料 */
	warriorUpgradeMaterials(100401,"warriorUpgradeMaterials","战士升级材料",4,1,"",""),
	/** 刺客升级材料 */
	assassinUpgradeMaterials(100402,"assassinUpgradeMaterials","刺客升级材料",4,1,"",""),
    ;
	/** 物品ID */
	public final int ID ; 
	/** 物品英文名 */
	public final String Name ; 
	/** 物品名称 */
	public final String Desc ; 
	/** 物品类型 1-各种货币 2-经验(能升级的) 3-体力(一般指能定时恢复的) 4-英雄升级材料 */
	public final int Type ; 
	/** 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一 */
	public final int Quality ; 
	/** 物品tips */
	public final String Tips ; 
	/** 图标Icon 文件名 */
	public final String Icon ; 

	private Money(int ID, String Name, String Desc, int Type, int Quality, String Tips, String Icon) {
		this.ID = ID; 
		this.Name = Name; 
		this.Desc = Desc; 
		this.Type = Type; 
		this.Quality = Quality; 
		this.Tips = Tips; 
		this.Icon = Icon; 
	}
	
	public static Money get(int id) {
		Money[] values = Money.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【Money】枚举表的" + "id【" + id + "】不存在");
	}

	public static Money getNullable(int id) {
		Money[] values = Money.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].ID == id) {
				return values[i];
			}
		}
		return null;
	}

}
