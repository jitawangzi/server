package cn.game.protocol.generated.enume;

/**
 * 情报标签
 * 
 * 工具生成的，不要手动修改
 */
public enum IntelligenceTagEnum{

	/**  */
	ACTIONRECORD(1,"ACTIONRECORD"),
	/**  */
	REGIONALINFORMATION(2,"REGIONALINFORMATION"),
	/**  */
	STARECLIPSEREPORT(3,"STARECLIPSEREPORT"),
	/**  */
	UNCLASSIFIEDINTELLIGENCE(4,"UNCLASSIFIEDINTELLIGENCE"),
	/**  */
	RESEARCHREPORT(101,"RESEARCHREPORT"),
	/**  */
	THEHUNTERWHISPERS(102,"THEHUNTERWHISPERS"),
	/**  */
	MAPPINGDOMAINRESIDENTS(103,"MAPPINGDOMAINRESIDENTS"),
	/**  */
	UNKNOWNSOUND(104,"UNKNOWNSOUND"),
	/**  */
	CAROLINE(105,"CAROLINE"),
	/**  */
	INDUSTRIALAREA(106,"INDUSTRIALAREA"),
    ;
	/** id */
	private int id ; 
	/** 全称 */
	private String name ; 

	private IntelligenceTagEnum(int id, String name) {
		this.id = id; 
		this.name = name; 
	}
	
	public static IntelligenceTagEnum get(int id) {
		IntelligenceTagEnum[] values = IntelligenceTagEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【IntelligenceTagEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static IntelligenceTagEnum getNullable(int id) {
		IntelligenceTagEnum[] values = IntelligenceTagEnum.values();
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
}
