package cn.game.protocol.manual;

import java.util.HashMap;
import java.util.Map;

/**
 * 物品类型
 * 
 */
public enum GoodsTypeEnum{

	/** 玩家资源类 */
	Resource(1, "玩家资源类"),
	/** 道具 */
	Item(2, "道具"),
	/** 英雄*/
	Hero(3, "英雄"),
	/** 头像框*/
	HeadBox(4, "头像框"),
	Pet(6, "宠物"),
	/** 装备，这个暂时没用到 */
	Equipment(7, "装备"),
	/** 英雄时装 暂时没用到*/
	Fashion(8, "英雄时装"),
	/** 合成英雄 */
	HCHero(9, "合成英雄"),

	/** 龙 暂时没用到*/
	Dragon(11, "龙"),
	/** 龙技能 暂时没用到*/
	DragonSkill(12, "龙技能"),

	Secretscript(35, "神通"),
	/** 暂时没用到 */
	Sword(36, "武器"),
	/**  */
	FairyFriend(100, "仙友"),

	/** 合并装备 */
	Merge_Equip(51, "合并装备"),
	/** 皮肤 */
	Skin(88, "皮肤"),
	Gem(80, "宝石"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String desc ; 

	private static Map<Integer, GoodsTypeEnum> enums = new HashMap<Integer, GoodsTypeEnum>();
	static {
		for (GoodsTypeEnum en : GoodsTypeEnum.values()) {
			enums.put(en.id, en);
		}
	}

	private GoodsTypeEnum(int id, String desc) {
		this.id = id; 
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
	public String getDesc(){
		return this.desc;
	}
}
