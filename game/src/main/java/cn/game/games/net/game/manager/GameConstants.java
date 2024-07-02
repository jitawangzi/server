package cn.game.games.net.game.manager;


/**
 * 游戏中自定义常量
 */
public class GameConstants {
    /**爬塔开始当天的小时*/
    public static final int TOWER_STARHOUR = 12;
    /**爬塔开始当天的分钟*/
    public static final int TOWER_STARMINITE = 0;
    /**爬塔活动id*/
    public static final int TOWER_ACTIVITYID = 50;
    /**组里最大成员*/
    public static final int GROUPNUM_MAX = 20;
    /**装备属性值类型 固定*/
    public static final int EQUIP_FIXEDVAL = 0;
    /**装备属性值类型 百分比*/
    public static final int EQUIP_PERCENTVAL = 1;
    /**基础建筑类型**/
    public static final int BASE_BUILDING_TYPE = 1;
    /**高级建筑类型**/
    public static final int SUPER_BUILDING_TYPE = 2;
    /**装饰建筑类型**/
    public static final int DECORATE_BUILDING_TYPE = 3;
    /**天赋节点**/
    public static final int  OCCUPATIONTALENT_NODE= 1;
    /**强化节点**/
    public static final int STRENGTH_NODE = 2;
    /**编队最小位置（相对位置）前端根据相对位置和左右方位找绝对位置**/
    public static final int LINEUP_MIN_POS = 4;
    /**编队最大位置**/
    public static final int LINEUP_MAX_POS = 12;
    /**玩家在进入首个区域时，从卡组中随机抽取3张卡片，放入可用卡片列表**/
    public static final int STRATEGY_CARDS = 3;
    /**充满希望**/
    public static final int EVENT_TYPE_ONE = 1;
    /**良好**/
    public static final int EVENT_TYPE_TWO = 2;
    /**一般**/
    public static final int EVENT_TYPE_THREE = 3;
    /**较差**/
    public static final int EVENT_TYPE_FOUR = 4;
    /**令人失望**/
    public static final int EVENT_TYPE_FIVE = 5;
    /**默认卡组名称**/
    public static final String INIT_CARD_GROUP ="默认卡组";
    
	/** 新buff覆盖老buff(只更新回合) */
	public static final int NEW_BUFF_COVER = 0;
	/** 有老buff新buff不添加 */
	public static final int NEW_BUFF_NOT_ADD = 1;
	/** 老buff,新buff共存 */
	public static final int NEW_BUFF_INSERT = 2;
	/** 老buff叠加新buff层数 */
	public static final int NEW_BUFF_ADD = 3;
    /** 探索装备 */
    public static final int EXPLORE_EQUIP = 2;
    /** buff效果回合状态---立即 */
    public static final int TYPE_INSTANT = 1;
    /** buff效果回合状态---持续 */
    public static final int TYPE_CONTINUE = 2;
    /**属性变化增加 */
    public static final int ATTR_INCR = 1;
	/** 数值类型-绝对值 */
	public static final int ABSOLUTE_VALUE = 0;
	/** 数值类型-上限的百分比 */
	public static final int TOPLIMIT_PERCENT = 1;
	/** 数值类型-当前值的百分比 */
	public static final int CUR_PERCENT = 2;
    /** 固有装备 */
    public static final int FIXED_EQUIP = 1;
    /** 饰品 */
    public static final int DECORATION_EQUIP = 2;
    /** 等级4 */
    public static final int LEVEL_FOUR = 4;
    /** 等级7 */
    public static final int LEVEL_SEVEN = 7;
    /** 等级10 */
    public static final int LEVEL_TEN = 10;
    /** 晶尘风暴id */
    public static final int CRYSTAL_DUST_STORM_ID = 1001;
    
	/** 序章 */
	public static final int INTRODUCTION = 21005;
    
}
