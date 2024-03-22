package cn.game.protocol.generated.enume;

/**
 * 地貌枚举表
 * 
 * 工具生成的，不要手动修改
 */
public enum ExploreLandformsEnum{

	/** 工业区 */
	industry(1,"industry","工业区"),
	/** 流放区 */
	exile(2,"exile","流放区"),
	/** 乐园区 */
	paradise(3,"paradise","乐园区"),
	/** 地下区 */
	underground(4,"underground","地下区"),
	/** 程序开发 */
	program(6,"program","程序开发"),
	/** 美术演示 */
	art(7,"art","美术演示"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String nameCn ; 

	private ExploreLandformsEnum(int id, String name, String nameCn) {
		this.id = id; 
		this.name = name; 
		this.nameCn = nameCn; 
	}
	
	public static ExploreLandformsEnum get(int id) {
		ExploreLandformsEnum[] values = ExploreLandformsEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ExploreLandformsEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static ExploreLandformsEnum getNullable(int id) {
		ExploreLandformsEnum[] values = ExploreLandformsEnum.values();
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
	public String getNameCn(){
		return this.nameCn;
	}
}
