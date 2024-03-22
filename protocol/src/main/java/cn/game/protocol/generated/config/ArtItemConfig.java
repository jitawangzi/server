package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 美术资源表
 * 
 * 工具生成的，不要手动修改
 */
 public class ArtItemConfig {

	/** id */
	private int id;		
	/** 资源 */
	private List<String> res;		
	/** 通用类型 */
	private int type;		
	/** 枚举类型 */
	private int exploreDecorationEnum;		

	public ArtItemConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String resString = element.getAttribute("res"); // 资源
		if (resString != null && resString.length() > 0) {
			String[] resStrings = resString.split("\\|"); 
			this.res = new ArrayList<String>(resStrings.length) ; 
			for (int i = 0; i < resStrings.length; i++) {
				String temp = resStrings[i];
				this.res.add(temp);
			}
		} else {
			this.res = new ArrayList<String>();
		}
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 通用类型
		this.exploreDecorationEnum = Integer.parseInt(element.getAttribute("exploreDecorationEnum") == null || element.getAttribute("exploreDecorationEnum").length() == 0 ? "0"
			: element.getAttribute("exploreDecorationEnum")); // 枚举类型
	}
	
	public int getId() {
		return id;
	}
	
	public List<String> getRes() {
		return res;
	}
	
	public int getType() {
		return type;
	}
	
	public int getExploreDecorationEnum() {
		return exploreDecorationEnum;
	}
	
}
