package cn.game.protocol.generated.enume;

/**
 * 后勤部效果枚举表
 * 
 * 工具生成的，不要手动修改
 */
public enum TechnologyTreeNodeEnum{

	/** 提升初始补给量 */
	InitialSupplies(1,"InitialSupplies","提升初始补给量"),
	/** 补给箱生成数量 */
	ReplenishmentNumbers(2,"ReplenishmentNumbers","补给箱生成数量"),
	/** 提升补给箱补给数量 */
	ReplenishmentSuppliesNumbers(3,"ReplenishmentSuppliesNumbers","提升补给箱补给数量"),
	/** 补给箱额外效果 */
	ReplenishmentEffect(4,"ReplenishmentEffect","补给箱额外效果"),
	/** 背包格子数 */
	CellNumbers(5,"CellNumbers","背包格子数"),
	/** 侵蚀度上限 */
	MpyLimit(6,"MpyLimit","侵蚀度上限"),
	/** 回复全队一定值的属性 */
	AddTeamAttribute(7,"AddTeamAttribute","回复全队一定值的属性"),
	/** 降低全队一定值的污染值 */
	DelTeamMpy(8,"DelTeamMpy","降低全队一定值的污染值"),
	/** 拾取宝箱时随机获得道具 */
	GetItem(9,"GetItem","拾取宝箱时随机获得道具"),
	/** 装备强化等级上限 */
	EquipmentStrengthLevelMax(10,"EquipmentStrengthLevelMax","装备强化等级上限"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 描述 */
	private String desc ; 

	private TechnologyTreeNodeEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static TechnologyTreeNodeEnum get(int id) {
		TechnologyTreeNodeEnum[] values = TechnologyTreeNodeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【TechnologyTreeNodeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static TechnologyTreeNodeEnum getNullable(int id) {
		TechnologyTreeNodeEnum[] values = TechnologyTreeNodeEnum.values();
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
