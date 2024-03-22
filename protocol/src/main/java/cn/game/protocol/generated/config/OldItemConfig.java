package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 道具
 * 
 * 工具生成的，不要手动修改
 */
 public class OldItemConfig {

	/** id -- id第四位是道具类型 */
	private final int id;		
	/** 道具名称 */
	private final String name;		
	/** 道具类型 -- 对应ItemTypeEnum.xlsm的id */
	private final int type;		
	/** 品质 -- 1-白 2-绿 3-蓝 4-紫 5-橙 */
	private final int quality;		
	/** 是否出售 -- （背包） 道具是否可出售 0-不可出售 1-可出售 */
	private final boolean sell;		
	/** 售价 */
	private final int price;		
	/** 是否使用 -- （背包） 道具是否可用 0-不可使用 1-可使用 */
	private final boolean use;		
	/** 自动使用 -- 获取后是否自动立即使用 0-不自动 1-自动 */
	private final boolean isAutoUse;		
	/** 堆叠上限 */
	private final int stack;		
	/** 排序类型 */
	private final int sortType;		
	/** 效果参数 -- 效果参数 根据道具类型解析 2 经验值 4 掉落id */
	private final int effect;		

	public OldItemConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 道具名称
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 道具类型
		this.quality = Integer.parseInt(element.getAttribute("quality") == null || element.getAttribute("quality").length() == 0 ? "0"
			: element.getAttribute("quality")); // 品质
		this.sell = Boolean.parseBoolean(element.getAttribute("sell") == null || element.getAttribute("sell").length() == 0 ? "false"
			: element.getAttribute("sell")); // 是否出售
		this.price = Integer.parseInt(element.getAttribute("price") == null || element.getAttribute("price").length() == 0 ? "0"
			: element.getAttribute("price")); // 售价
		this.use = Boolean.parseBoolean(element.getAttribute("use") == null || element.getAttribute("use").length() == 0 ? "false"
			: element.getAttribute("use")); // 是否使用
		this.isAutoUse = Boolean.parseBoolean(element.getAttribute("isAutoUse") == null || element.getAttribute("isAutoUse").length() == 0 ? "false"
			: element.getAttribute("isAutoUse")); // 自动使用
		this.stack = Integer.parseInt(element.getAttribute("stack") == null || element.getAttribute("stack").length() == 0 ? "0"
			: element.getAttribute("stack")); // 堆叠上限
		this.sortType = Integer.parseInt(element.getAttribute("sortType") == null || element.getAttribute("sortType").length() == 0 ? "0"
			: element.getAttribute("sortType")); // 排序类型
		this.effect = Integer.parseInt(element.getAttribute("effect") == null || element.getAttribute("effect").length() == 0 ? "0"
			: element.getAttribute("effect")); // 效果参数
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getType() {
		return type;
	}
	
	public int getQuality() {
		return quality;
	}
	
	public boolean getSell() {
		return sell;
	}
	
	public int getPrice() {
		return price;
	}
	
	public boolean getUse() {
		return use;
	}
	
	public boolean getIsAutoUse() {
		return isAutoUse;
	}
	
	public int getStack() {
		return stack;
	}
	
	public int getSortType() {
		return sortType;
	}
	
	public int getEffect() {
		return effect;
	}
	
}
