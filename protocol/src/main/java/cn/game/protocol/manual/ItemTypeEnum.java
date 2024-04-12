package cn.game.protocol.manual;

/**
 * 道具类型
 * 
 * 工具生成的，不要手动修改
 */
public enum ItemTypeEnum{

	/** 剧情道具 */
	Common(1,"Common","剧情道具"),
	/** 经验道具 */
	Exp(2,"Exp","经验道具"),
	/** 角色碎片 */
	RolePiece(3,"RolePiece","角色碎片"),
	/** 宝箱 */
	Box(4,"Box","宝箱"),
	/** 体力道具 */
	AP(5,"AP","体力道具"),
	/** 灵武相关道具 */
	MechaUnit(6,"MechaUnit","灵武相关道具"),
	/** 装备相关道具 */
	CoreItem(7,"CoreItem","装备相关道具"),
	/** 亲密度道具 */
	FriendlyItem(8,"FriendlyItem","亲密度道具"),
	/** 誓约道具 */
	Oath(9,"Oath","誓约道具"),
	/** 职阶相关道具 */
	Origin(10,"Origin","职阶相关道具"),
	/** 食材相关道具 */
	ChipsItem(11,"ChipsItem","食材相关道具"),
	/** 泰坦相关道具 */
	TitanItem(12,"TitanItem","泰坦相关道具"),
	/** 消耗品 */
	Consumables(21,"Consumables","消耗品"),
	/** 资源包 */
	ResourcePackage(22,"ResourcePackage","资源包"),
	/** 探索的剧情道具 */
	ExploreItem(23,"ExploreItem","探索的剧情道具"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String desc ; 

	private ItemTypeEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static ItemTypeEnum get(int id) {
		ItemTypeEnum[] values = ItemTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ItemTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static ItemTypeEnum getNullable(int id) {
		ItemTypeEnum[] values = ItemTypeEnum.values();
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
