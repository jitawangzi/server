package cn.game.protocol.generated.config;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 系统开启权限
 * 
 * 工具生成的，不要手动修改
 */
 public class SystemRootConfig {

	/** id */
	private int id;		
	/** 英文名称 */
	private String name;		
	/** 限制条件 */
	private List<Integer> desc;		

	public SystemRootConfig (Element element) {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 英文名称
		String desc = element.getAttribute("desc"); // 限制条件
		if (desc != null && desc.length() > 0) {
			String[] descStrings = desc.split("\\|"); 
			this.desc = new ArrayList<Integer>(descStrings.length) ; 
			for (int i = 0; i < descStrings.length; i++) {
				Integer temp = Integer.parseInt(descStrings[i]);
				this.desc.add(temp);
			}
		} else {
			this.desc = new ArrayList<Integer>();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public List<Integer> getDesc() {
		return desc;
	}
	
}
