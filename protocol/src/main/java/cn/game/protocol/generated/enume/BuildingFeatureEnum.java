package cn.game.protocol.generated.enume;

/**
 * 建筑功能类型表
 * 
 * 工具生成的，不要手动修改
 */
public enum BuildingFeatureEnum{

	/** 商店 */
	ItemShop(101,"ItemShop","商店"),
	/** 战斗实验室 */
	AnalysisRoom(102,"AnalysisRoom","战斗实验室"),
	/** 后勤部 */
	LogisticsDepartment(103,"LogisticsDepartment","后勤部"),
	/** 军械库 */
	Armory(104,"Armory","军械库"),
	/** 战术中心 */
	TacticalCenter(105,"TacticalCenter","战术中心"),
	/** 情报站 */
	Intelligence(106,"Intelligence","情报站"),
	/** 图鉴馆 */
	Library(201,"Library","图鉴馆"),
	/** 档案馆 */
	Archives(202,"Archives","档案馆"),
	/** 昨日联盟 */
	YesterdayAlliance(203,"YesterdayAlliance","昨日联盟"),
	/** 深水重工 */
	DeepwaterAlliance(204,"DeepwaterAlliance","深水重工"),
	/** 风铃集裸 */
	MobileAlliance(205,"MobileAlliance","风铃集裸"),
	/** 群星乐园 */
	StarsAlliance(206,"StarsAlliance","群星乐园"),
	/** 伊娜教会 */
	ElnaAlliance(207,"ElnaAlliance","伊娜教会"),
	/** 马戏团 */
	CircusAlliance(208,"CircusAlliance","马戏团"),
	/** 七海神社 */
	SevenSeasAlliance(209,"SevenSeasAlliance","七海神社"),
    ;
	/** id */
	private int id ; 
	/** 英文名称 */
	private String name ; 
	/** 名称 */
	private String desc ; 

	private BuildingFeatureEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static BuildingFeatureEnum get(int id) {
		BuildingFeatureEnum[] values = BuildingFeatureEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【BuildingFeatureEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static BuildingFeatureEnum getNullable(int id) {
		BuildingFeatureEnum[] values = BuildingFeatureEnum.values();
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
