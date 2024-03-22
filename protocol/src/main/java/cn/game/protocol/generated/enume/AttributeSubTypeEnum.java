package cn.game.protocol.generated.enume;

/**
 * 属性sub类型枚举
 * 
 * 工具生成的，不要手动修改
 */
public enum AttributeSubTypeEnum{

	/** 总值 */
	total(1,"total","总值"),
	/** 当前值 */
	cur(2,"cur","当前值"),
	/** 当前总值 */
	curTotal(3,"curTotal","当前总值"),
	/** 降低减免% */
	downRedu(4,"downRedu","降低减免%"),
	/** 增加减免% */
	upRedu(5,"upRedu","增加减免%"),
	/** 临时值 */
	temporary(6,"temporary","临时值"),
	/** 降低免疫概率 */
	downImmune(7,"downImmune","降低免疫概率"),
	/** 增加免疫概率 */
	upImmune(8,"upImmune","增加免疫概率"),
	/** 降低减免值 */
	downReduV(9,"downReduV","降低减免值"),
	/** 增加减免值 */
	upReduV(10,"upReduV","增加减免值"),
    ;
	/** id */
	private int id ; 
	/** 全称 */
	private String name ; 
	/** 名称 */
	private String desc ; 

	private AttributeSubTypeEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static AttributeSubTypeEnum get(int id) {
		AttributeSubTypeEnum[] values = AttributeSubTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【AttributeSubTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static AttributeSubTypeEnum getNullable(int id) {
		AttributeSubTypeEnum[] values = AttributeSubTypeEnum.values();
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
