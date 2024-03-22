package cn.game.protocol.generated.enume;

/**
 * 天气系统类型表
 * 
 * 工具生成的，不要手动修改
 */
public enum ExploreWeatherSystemEnum{

	/** 灾害系统-晶尘风暴 */
	Storm(1,"Storm","灾害系统-晶尘风暴"),
	/** 剧情-白光 */
	WhiteLight(2,"WhiteLight","剧情-白光"),
    ;
	/** id */
	private int id ; 
	/** 名称 */
	private String name ; 
	/** 名称 */
	private String desc ; 

	private ExploreWeatherSystemEnum(int id, String name, String desc) {
		this.id = id; 
		this.name = name; 
		this.desc = desc; 
	}
	
	public static ExploreWeatherSystemEnum get(int id) {
		ExploreWeatherSystemEnum[] values = ExploreWeatherSystemEnum.values();
		for (int i = 0, len = values.length; i < len; i++) {
			if (values[i].getId() == id) {
				return values[i];
			}
		}
		throw new NullPointerException("【ExploreWeatherSystemEnum】枚举表的" + "id【" + id + "】不存在");
	}

	public static ExploreWeatherSystemEnum getNullable(int id) {
		ExploreWeatherSystemEnum[] values = ExploreWeatherSystemEnum.values();
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
