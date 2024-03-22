package cn.game.protocol.generated.enume;

/**
 * 标签枚举表
 * 
 * 工具生成的，不要手动修改
 */
public enum RoleTagEnum{

	/** SAN标签 */
	SAN(1,"SAN","SAN标签",1),
	/** 基础常驻标签 */
	BASE(2,"BASE","基础常驻标签",2),
	/** 环境类 */
	ENV(3,"ENV","环境类",0),
	/** 可继承类 */
	INHERIT(4,"INHERIT","可继承类",0),
	/** 压制类 */
	SUPPRESS(5,"SUPPRESS","压制类",1),
	/** 流程类 */
	PROCESS(6,"PROCESS","流程类",0),
	/** 阶段类 */
	STAGE(7,"STAGE","阶段类",1),
	/** 状态类 */
	STATE(8,"STATE","状态类",1),
	/** 事件类 */
	EVENT(9,"EVENT","事件类",1),
	/** 免疫类 */
	IMMUNE(10,"IMMUNE","免疫类",2),
	/** 生存类 */
	EXISTENCE(11,"EXISTENCE","生存类",1),
	/** 强制类 */
	FORCE(12,"FORCE","强制类",1),
	/** 怪物特性 */
	CHARACTER(20,"CHARACTER","怪物特性",0),
    ;
	/** id */
	private int id ; 
	/** 全称 */
	private String name ; 
	/** 名称 */
	private String desc ; 
	/** 数量 */
	private int max ; 

	private RoleTagEnum(int id, String name, String desc, int max) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
		this.max = max; 
	}
	
	public static RoleTagEnum get(int id) {
		RoleTagEnum[] values = RoleTagEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【RoleTagEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static RoleTagEnum getNullable(int id) {
		RoleTagEnum[] values = RoleTagEnum.values();
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
	public int getMax(){
		return this.max;
	}
}
