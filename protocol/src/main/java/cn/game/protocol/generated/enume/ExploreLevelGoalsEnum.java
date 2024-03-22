package cn.game.protocol.generated.enume;

/**
 * 探索区域目标类型
 * 
 * 工具生成的，不要手动修改
 */
public enum ExploreLevelGoalsEnum{

	/** 击杀boss */
	Boss(1,"Boss","击杀boss"),
	/** 撤退 */
	Retreat(2,"Retreat","撤退"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String desc ; 

	private ExploreLevelGoalsEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static ExploreLevelGoalsEnum get(int id) {
		ExploreLevelGoalsEnum[] values = ExploreLevelGoalsEnum.values();
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
