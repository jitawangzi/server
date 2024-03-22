package cn.game.protocol.generated.enume;

/**
 * 物品类型
 * 
 * 工具生成的，不要手动修改
 */
public enum GoodsTypeEnum{

	/** 玩家资源类 */
	Resource(1,"Resource","玩家资源类"),
	/** 道具 */
	Item(2,"Item","道具"),
	/** 英雄*/
	Hero(7, "Hero", "英雄"),

	/** 角色 */
	Role(44, "Role", "角色"),
	/** 装备 */
	Equipment(55, "Equipment", "装备"),
	/** 皮肤 */
	Skin(88, "Skin", "皮肤"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String desc ; 

	private GoodsTypeEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static GoodsTypeEnum get(int id) {
		GoodsTypeEnum[] values = GoodsTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【GoodsTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static GoodsTypeEnum getNullable(int id) {
		GoodsTypeEnum[] values = GoodsTypeEnum.values();
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
