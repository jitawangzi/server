package cn.game.protocol.manual;

import java.util.HashMap;
import java.util.Map;

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
	Hero(3, "Hero", "英雄"),
	/** 装备 */
	Equipment(7, "Equipment", "装备"),
	/** 英雄时装 */
	Fashion(8, "Equipment", "英雄时装"),
	Gem(9, "Gem", "宝石"),
	/** 龙 */
	Dragon(11, "Dragon", "龙"),
	/** 龙技能 */
	DragonSkill(12, "DragonSkill", "龙技能"),

	Sword(6, "Sword", "武器"),

	/** 角色 */
	Role(44, "Role", "角色"),
	/** 皮肤 */
	Skin(88, "Skin", "皮肤"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String desc ; 

	private static Map<Integer, GoodsTypeEnum> enums = new HashMap<Integer, GoodsTypeEnum>();
	static {
		for (GoodsTypeEnum en : GoodsTypeEnum.values()) {
			enums.put(en.id, en);
		}
	}

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
