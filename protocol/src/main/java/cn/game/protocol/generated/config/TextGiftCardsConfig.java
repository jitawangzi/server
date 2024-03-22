package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 礼物卡文本表
 * 
 * 工具生成的，不要手动修改
 */
 public class TextGiftCardsConfig {

	/** id -- id */
	private final int id;		

	public TextGiftCardsConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
	}
	
	public int getId() {
		return id;
	}
	
}
