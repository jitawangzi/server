package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 核心套装
 * 
 * 工具生成的，不要手动修改
 */
 public class CoreSuitConfig {

	/** 套装id */
	private final int id;		
	/** 套装名称 */
	private final String name;		
	/** 套装品质 -- 1-C级绿色； 2-B级蓝色； 3-A级紫色； 4-S级橙色； */
	private final int type;		
	/** 套装属性 -- 4对属性分别对应α、β、γ、Σ */
	private final List<Integer> SuitAttribute;		

	public CoreSuitConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 套装id
		this.name = element.getAttribute("name"); // 套装名称
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 套装品质
		String SuitAttributeString = element.getAttribute("SuitAttribute"); // 套装属性
		if (SuitAttributeString != null && SuitAttributeString.length() > 0) {
			String[] SuitAttributeStrings = SuitAttributeString.split("\\|"); 
			List<Integer> SuitAttribute = new ArrayList<Integer>(SuitAttributeStrings.length) ; 
			for (int i = 0; i < SuitAttributeStrings.length; i++) {
				Integer temp = Integer.parseInt(SuitAttributeStrings[i]);
				SuitAttribute.add(temp);
			}
			this.SuitAttribute = com.google.common.collect.ImmutableList.copyOf(SuitAttribute);						
		} else {
			this.SuitAttribute = java.util.Collections.emptyList();
		}
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
	
	public List<Integer> getSuitAttribute() {
		return SuitAttribute;
	}
	
}
