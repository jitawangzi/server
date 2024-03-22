package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 装备稀有度枚举表
 * 
 * 工具生成的，不要手动修改
 */
 public class EquipmentEnumConfig {

	/** id -- id */
	private final int id;		
	/** 全称 -- 全称 */
	private final String name;		
	/** 名称 -- 名称 */
	private final String desc;		
	/** 效果数量 -- 饰品词缀的上限数量 下限默认为0 */
	private final int buffNum;		

	public EquipmentEnumConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 全称
		this.desc = element.getAttribute("desc"); // 名称
		this.buffNum = Integer.parseInt(element.getAttribute("buffNum") == null || element.getAttribute("buffNum").length() == 0 ? "0"
			: element.getAttribute("buffNum")); // 效果数量
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public int getBuffNum() {
		return buffNum;
	}
	
}
