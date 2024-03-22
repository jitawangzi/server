package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 配置表版本文件
 * 
 * 工具生成的，不要手动修改
 */
 public class versionConfig {

	/** id -- id */
	private final int id;		
	/** 配置文件版本 -- 版本 */
	private final String version;		

	public versionConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.version = element.getAttribute("version"); // 配置文件版本
	}
	
	public int getId() {
		return id;
	}
	
	public String getVersion() {
		return version;
	}
	
}
