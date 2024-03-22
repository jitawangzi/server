package cn.game.protocol.generated.config;
import org.w3c.dom.Element;

/**
 * 主线交互物体枚举表
 * 
 * 工具生成的，不要手动修改
 */
 public class MainLineObjectEnumConfig {

	/** id */
	private int id;		
	/** 名称 */
	private String name;		
	/** 描述 */
	private String desc;		
	/** 是否可重复交互 */
	private int ifRepeat;		
	/** 交互后是否消失 */
	private int ifDisappear;		

	public MainLineObjectEnumConfig (Element element) {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 名称
		this.desc = element.getAttribute("desc"); // 描述
		this.ifRepeat = Integer.parseInt(element.getAttribute("ifRepeat") == null || element.getAttribute("ifRepeat").length() == 0 ? "0"
			: element.getAttribute("ifRepeat")); // 是否可重复交互
		this.ifDisappear = Integer.parseInt(element.getAttribute("ifDisappear") == null || element.getAttribute("ifDisappear").length() == 0 ? "0"
			: element.getAttribute("ifDisappear")); // 交互后是否消失
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
	
	public int getIfRepeat() {
		return ifRepeat;
	}
	
	public int getIfDisappear() {
		return ifDisappear;
	}
	
}
