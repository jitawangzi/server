package cn.game.protocol.generated.enume;

/**
 * 角色属性
 * 
 * 工具生成的，不要手动修改
 */
public enum AttributeTypeEnum{

	/** 生命 */
	hp(1,"hp","生命",1,true,true),
	/** 攻击 */
	attack(2,"attack","攻击",1,false,false),
	/** 速度 */
	speed(3,"speed","速度",1,false,false),
	/** 防御 */
	defense(4,"defense","防御",1,false,false),
	/** 命中 */
	hit(5,"hit","命中",2,false,false),
	/** 闪避 */
	dodge(6,"dodge","闪避",2,false,false),
	/** 暴击 */
	critical(7,"critical","暴击",2,false,false),
	/** 暴击伤害 */
	criticalDamage(8,"criticalDamage","暴击伤害",2,false,false),
	/** 击退抗性 */
	repelResistance(9,"repelResistance","击退抗性",3,false,false),
	/** 眩晕抗性 */
	vertigoResistance(10,"vertigoResistance","眩晕抗性",3,false,false),
	/** 异常抗性 */
	abnormalResistance(11,"abnormalResistance","异常抗性",3,false,false),
	/** 弱化抗性 */
	weakenResistance(12,"weakenResistance","弱化抗性",3,false,false),
	/** 流血抗性 */
	bleedingResistance(13,"bleedingResistance","流血抗性",3,false,false),
	/** 腐蚀抗性 */
	corrosionResistance(14,"corrosionResistance","腐蚀抗性",3,false,false),
	/** 暴击抗性 */
	critResistance(15,"critResistance","暴击抗性",3,false,false),
	/** SAN值 */
	san(16,"san","SAN值",1,true,true),
	/** AP值 */
	ap(17,"ap","AP值",4,true,false),
	/** 治疗加值（攻方） */
	medicalAddValue(18,"medicalAddValue","治疗加值（攻方）",4,false,false),
	/** 伤害加值（攻方） */
	damageAddValue(19,"damageAddValue","伤害加值（攻方）",4,false,false),
    ;
	/** id */
	private int id ; 
	/** 全称 */
	private String name ; 
	/** 名称 */
	private String desc ; 
	/** 属性分类 */
	private int type ; 
	/** 是否有当前值 */
	private boolean hasCur ; 
	/** 是否有当前最大值 */
	private boolean hasCurTotal ; 

	private AttributeTypeEnum(int id, String name, String desc, int type, boolean hasCur, boolean hasCurTotal) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
		this.type = type; 
		this.hasCur = hasCur; 
		this.hasCurTotal = hasCurTotal; 
	}
	
	public static AttributeTypeEnum get(int id) {
		AttributeTypeEnum[] values = AttributeTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【AttributeTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static AttributeTypeEnum getNullable(int id) {
		AttributeTypeEnum[] values = AttributeTypeEnum.values();
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
	public int getType(){
		return this.type;
	}
	public boolean getHasCur(){
		return this.hasCur;
	}
	public boolean getHasCurTotal(){
		return this.hasCurTotal;
	}
}
