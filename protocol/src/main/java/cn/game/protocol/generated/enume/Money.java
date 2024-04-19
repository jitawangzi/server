package cn.game.protocol.generated.enume;
import java.util.Map;

/**
 * 货币表
 * 
 * 工具生成的，不要手动修改
 */
public enum Money{

	/**  */
	钻石(100001,1,1,1,"钻石","用于扭蛋机中抽取英雄","",cn.game.util.MapUtil.newHashMap("1;2|3;4","Map<Integer,Byte>")),
	/**  */
	金币(100002,1,1,1,"金币","有钱能使小熊猫推磨","",cn.game.util.MapUtil.newHashMap("","Map<Integer,Byte>")),
    ;
	/** 物品ID */
	public final int ID ; 
	/** 总类型 1-货币 2-物品 3-装备 4-铁哥们 5-好友 6-宠物 */
	public final int TotalType ; 
	/** 物品类型 1-钻石 2-金币 */
	public final int ItemType ; 
	/** 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一 */
	public final int Quality ; 
	/** 物品名称 */
	public final String Name ; 
	/** 物品tips */
	public final String Tips ; 
	/** 图标Icon 文件名 */
	public final String Icon ; 
	/** 货币有效期 1-天数     配置：1;7 2-具体失效时间，失效为当天23:59:59，失效即物品消失     配置：2;20240705  例如节日专用货币，到期直接全部删除 */
	public final Map<Integer,Byte> Period ; 

	private Money(int ID, int TotalType, int ItemType, int Quality, String Name, String Tips, String Icon, Map<Integer,Byte> Period) {
		this.ID = ID; 
		this.TotalType = TotalType; 
		this.ItemType = ItemType; 
		this.Quality = Quality; 
		this.Name = Name; 
		this.Tips = Tips; 
		this.Icon = Icon; 
		this.Period = Period; 
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
