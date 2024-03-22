package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 玩家经验表
 * 
 * 工具生成的，不要手动修改
 */
 public class PlayerExpConfig {

	/** id -- 100级满经验后不再加经验 */
	private final int id;		
	/** 主角经验 -- 主角升级到下一级所需经验 */
	private final int exp;		

	public PlayerExpConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.exp = Integer.parseInt(element.getAttribute("exp") == null || element.getAttribute("exp").length() == 0 ? "0"
			: element.getAttribute("exp")); // 主角经验
	}
	
	public int getId() {
		return id;
	}
	
	public int getExp() {
		return exp;
	}
	
}
