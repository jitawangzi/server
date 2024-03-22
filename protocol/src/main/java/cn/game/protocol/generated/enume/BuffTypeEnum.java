package cn.game.protocol.generated.enume;

/**
 * buff类型枚举
 * 
 * 工具生成的，不要手动修改
 */
public enum BuffTypeEnum{

	/** 无 */
	Null(-1,"Null","","无",0,0,0),
	/** 眩晕：无法做任何行动，眩晕时自动跳过回合 */
	stun(101,"stun","眩晕","眩晕：无法做任何行动，眩晕时自动跳过回合",1,10,0),
	/** 定身：无法移动 */
	banMove(201,"banMove","定身","定身：无法移动",2,11,0),
	/** 沉默：无法使用任何技能，只能移动 */
	banAllSkill(202,"banAllSkill","沉默","沉默：无法使用任何技能，只能移动",2,11,0),
	/** 封禁：随机禁用2个技能 */
	banSkill(203,"banSkill","封禁","封禁：随机禁用2个技能",2,11,0),
	/** 禁疗：禁止使用所有治疗类技能，包括HOT类型的治疗技能 */
	banCureSkill(204,"banCureSkill","禁疗","禁疗：禁止使用所有治疗类技能，包括HOT类型的治疗技能",2,11,0),
	/** 属性最大值削弱 */
	attributeWeaken(301,"attributeWeaken","减益","属性最大值削弱",3,12,0),
	/** 超级属性最大值削弱（创伤、崩溃等） */
	attributeWeakenS(302,"attributeWeakenS","减益S","超级属性最大值削弱（创伤、崩溃等）",3,0,0),
	/** 属性最大值增强 */
	attributeStrengthen(401,"attributeStrengthen","增益","属性最大值增强",4,0,0),
	/** 流血：每次行动/探索回合损失生命值，同时受治疗量下降 */
	bleed(501,"bleed","流血","流血：每次行动/探索回合损失生命值，同时受治疗量下降",5,13,0),
	/** 腐蚀：每次行动/探索回合损失生命值，同时全抗性下降 */
	corrosion(502,"corrosion","腐蚀","腐蚀：每次行动/探索回合损失生命值，同时全抗性下降",5,14,0),
	/** 燃烧：每次行动/探索回合损失生命值 */
	burn(503,"burn","燃烧","燃烧：每次行动/探索回合损失生命值",5,0,504),
	/** 渐冻：持续期间AP-1 */
	frozen(504,"frozen","渐冻","渐冻：持续期间AP-1",5,0,503),
	/** 恐惧：每次行动/探索回合损失SAN值，同时命中下降 */
	fear(505,"fear","恐惧","恐惧：每次行动/探索回合损失SAN值，同时命中下降",5,0,0),
	/** 慌乱：每次行动/探索回合损失SAN值，同时不受控制，优先攻击最近目标，不分敌我 */
	panic(506,"panic","慌乱","慌乱：每次行动/探索回合损失SAN值，同时不受控制，优先攻击最近目标，不分敌我",5,0,0),
	/** 治愈：每次行动/探索回合恢复少量生命值，无视一切治疗损减（包括污染值和流血的附加状态） */
	cure(601,"cure","治愈","治愈：每次行动/探索回合恢复少量生命值，无视一切治疗损减（包括污染值和流血的附加状态）",6,0,0),
	/** 冷静：每次行动/探索回合恢复少量SAN值 */
	calm(602,"calm","冷静","冷静：每次行动/探索回合恢复少量SAN值",6,0,0),
	/** 失去资源 */
	spentResource(801,"spentResource","失去资源","失去资源",8,0,0),
	/** 获得资源 */
	rewardResource(802,"rewardResource","获得资源","获得资源",8,0,0),
	/** 专属类：射击值/战斗命令 */
	exclusive(901,"exclusive","专属","专属类：射击值/战斗命令",9,0,0),
	/**  */
	itemResume(1001,"itemResume","道具恢复类","",10,0,0),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String string ; 
	/** 描述 */
	private String desc ; 
	/** 分组 */
	private int group ; 
	/** 抗性属性ID */
	private int resistanceAttrId ; 
	/** 相互抵消的buff类型 */
	private int counteractType ; 

	private BuffTypeEnum(int id, String name, String string, String desc, int group, int resistanceAttrId, int counteractType) {
		this.id = id; 
		this.name = name; 
		this.string = string; 
		this.desc = desc; 
		this.group = group; 
		this.resistanceAttrId = resistanceAttrId; 
		this.counteractType = counteractType; 
	}
	
	public static BuffTypeEnum get(int id) {
		BuffTypeEnum[] values = BuffTypeEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【BuffTypeEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static BuffTypeEnum getNullable(int id) {
		BuffTypeEnum[] values = BuffTypeEnum.values();
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
	public String getString(){
		return this.string;
	}
	public String getDesc(){
		return this.desc;
	}
	public int getGroup(){
		return this.group;
	}
	public int getResistanceAttrId(){
		return this.resistanceAttrId;
	}
	public int getCounteractType(){
		return this.counteractType;
	}
}
