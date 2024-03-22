package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 文本总表
 * 
 * 工具生成的，不要手动修改
 */
 public class TextContentConfig {

	/** id -- id */
	private final int id;		
	/** 文本内容 -- 内容 */
	private final String textContent;		

	public TextContentConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.textContent = element.getAttribute("textContent"); // 文本内容
	}
	
	public int getId() {
		return id;
	}
	
	public String getTextContent() {
		return textContent;
	}
	
}
