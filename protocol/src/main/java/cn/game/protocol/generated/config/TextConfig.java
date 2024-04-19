package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 文本提示
 * 
 * 工具生成的，不要手动修改
 */
 public class TextConfig {

	/** ID */
	public final int ID;		
	/** 文本内容 */
	public final String Text;		

	public TextConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Text = element.getAttribute("Text"); // 文本内容
	}
	

}
