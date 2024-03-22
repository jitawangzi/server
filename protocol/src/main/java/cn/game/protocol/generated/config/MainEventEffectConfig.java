package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 主城事件效果表
 * 
 * 工具生成的，不要手动修改
 */
 public class MainEventEffectConfig {

	/** id */
	private int id;		
	/** 类型 */
	private int type;		
	/** 事件图标 */
	private String icon;		
	/** 描述 */
	private String des;		
	/** 事件1 */
	private String event1;		
	/** 枚举参数1 */
	private List<List<Integer>> enum1;		
	/** 事件2 */
	private String event2;		
	/** 枚举参数2 */
	private List<List<Integer>> enum2;		

	public MainEventEffectConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 类型
		this.icon = element.getAttribute("icon"); // 事件图标
		this.des = element.getAttribute("des"); // 描述
		this.event1 = element.getAttribute("event1"); // 事件1
		String enum1String = element.getAttribute("enum1"); // 枚举参数1
		if (enum1String != null && enum1String.length() > 0) {
			String[] enum1Strings = enum1String.split("\\|"); 
			this.enum1 = new ArrayList<List<Integer>>(enum1Strings.length) ; 
			for (int i = 0; i < enum1Strings.length; i++) {
				String[] enum1Strings2 = enum1Strings[i].split(":"); 
				List<Integer> list = new ArrayList<Integer>(enum1Strings2.length) ; 
				for (int j = 0; j < enum1Strings2.length; j++) {
					Integer temp = Integer.parseInt(enum1Strings2[j]);
					list.add(temp) ; 
				}
				this.enum1.add(list);
			}
		} else {
			this.enum1 = new ArrayList<List<Integer>>();
		}
		this.event2 = element.getAttribute("event2"); // 事件2
		String enum2String = element.getAttribute("enum2"); // 枚举参数2
		if (enum2String != null && enum2String.length() > 0) {
			String[] enum2Strings = enum2String.split("\\|"); 
			this.enum2 = new ArrayList<List<Integer>>(enum2Strings.length) ; 
			for (int i = 0; i < enum2Strings.length; i++) {
				String[] enum2Strings2 = enum2Strings[i].split(":"); 
				List<Integer> list = new ArrayList<Integer>(enum2Strings2.length) ; 
				for (int j = 0; j < enum2Strings2.length; j++) {
					Integer temp = Integer.parseInt(enum2Strings2[j]);
					list.add(temp) ; 
				}
				this.enum2.add(list);
			}
		} else {
			this.enum2 = new ArrayList<List<Integer>>();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public String getDes() {
		return des;
	}
	
	public String getEvent1() {
		return event1;
	}
	
	public List<List<Integer>> getEnum1() {
		return enum1;
	}
	
	public String getEvent2() {
		return event2;
	}
	
	public List<List<Integer>> getEnum2() {
		return enum2;
	}
	
}
