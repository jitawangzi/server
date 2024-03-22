package cn.game.protocol.generated.config;

import org.w3c.dom.Element;

/**
 * 队伍经验表
 * 
 * 工具生成的，不要手动修改
 */
 public class TeamExpConfig {

	/** id -- 100级满经验后不再加经验 */
	private final int id;		
	/** 主线经验 -- 升级到下一级所需经验 */
	private final int expThread;		
	/** 地牢经验 -- 升级到下一级所需经验 */
	private final int expDungeon;		

	public TeamExpConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.expThread = Integer.parseInt(element.getAttribute("expThread") == null || element.getAttribute("expThread").length() == 0 ? "0"
			: element.getAttribute("expThread")); // 主线经验
		this.expDungeon = Integer.parseInt(element.getAttribute("expDungeon") == null || element.getAttribute("expDungeon").length() == 0 ? "0"
			: element.getAttribute("expDungeon")); // 地牢经验
	}
	
	public int getId() {
		return id;
	}
	
	public int getExpThread() {
		return expThread;
	}
	
	public int getExpDungeon() {
		return expDungeon;
	}
	
}
