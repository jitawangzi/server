package cn.game.protocol.generated.enume;

/**
 * 主线交互物体枚举表
 * 
 * 工具生成的，不要手动修改
 */
public enum MainlineObjectEnum{

	/** 果树 */
	fruitTree(1,"fruitTree","果树"),
	/** 泉水 */
	springWater(2,"springWater","泉水"),
	/** 营地 */
	camp(3,"camp","营地"),
	/** 宝箱 */
	treasureChest(4,"treasureChest","宝箱"),
	/** 采集物 */
	collect(5,"collect","采集物"),
	/** 命令交互物 */
	commandOption(6,"commandOption","命令交互物"),
	/** 无功能物体 */
	noFunction(7,"noFunction","无功能物体"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 描述 */
	private String desc ; 

	private MainlineObjectEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static MainlineObjectEnum get(int id) {
		MainlineObjectEnum[] values = MainlineObjectEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【MainlineObjectEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static MainlineObjectEnum getNullable(int id) {
		MainlineObjectEnum[] values = MainlineObjectEnum.values();
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
