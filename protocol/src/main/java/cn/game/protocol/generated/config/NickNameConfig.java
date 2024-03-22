package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 昵称
 * 
 * 工具生成的，不要手动修改
 */
 public class NickNameConfig {

	/** 昵称id */
	private final int ID;		
	/** 玩家昵称 生成角色时，需要在该list中随机生成一个 玩家可通过点击头像模块，进行第1次免费改名 */
	private final String NickName;		

	public NickNameConfig (Element element) throws Exception {
	
		this.ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 昵称id
		this.NickName = element.getAttribute("NickName"); // 玩家昵称 生成角色时，需要在该list中随机生成一个 玩家可通过点击头像模块，进行第1次免费改名
	}
	
	public int getID() {
		return ID;
	}
	
	public String getNickName() {
		return NickName;
	}
	
}
