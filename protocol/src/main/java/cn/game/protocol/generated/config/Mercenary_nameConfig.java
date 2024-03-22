package cn.game.protocol.generated.config;
import org.w3c.dom.Element;

/**
 * 佣兵名称库
 * 
 * 工具生成的，不要手动修改
 */
 public class Mercenary_nameConfig {

	/** id */
	private int id;		
	/** 名称 */
	private String name;		
	/** 角色id */
	private String nameId;		
	/** 英文名称 */
	private String ename;		
	/** 英灵名称 */
	private String yname;		
	/** 描述 */
	private String des;		

	public Mercenary_nameConfig (Element element) {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 名称
		this.nameId = element.getAttribute("nameId"); // 角色id
		this.ename = element.getAttribute("ename"); // 英文名称
		this.yname = element.getAttribute("yname"); // 英灵名称
		this.des = element.getAttribute("des"); // 描述
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getNameId() {
		return nameId;
	}
	
	public String getEname() {
		return ename;
	}
	
	public String getYname() {
		return yname;
	}
	
	public String getDes() {
		return des;
	}
	
}
