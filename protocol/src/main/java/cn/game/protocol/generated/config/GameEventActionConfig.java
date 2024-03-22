package cn.game.protocol.generated.config;

import cn.game.protocol.generated.enume.GameEventActionEnum;
import org.w3c.dom.Element;

/**
 * 游戏事件行为表
 * 
 * 工具生成的，不要手动修改
 */
 public class GameEventActionConfig {

	/** id -- id */
	private final int id;		
	/** 游戏事件id -- GameEvent表id，属于哪个事件 */
	private final int eventId;		
	/** 行为类型 */
	private final GameEventActionEnum type;		
	/** 行为参数 */
	private final int[] params;		

	public GameEventActionConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.eventId = Integer.parseInt(element.getAttribute("eventId") == null || element.getAttribute("eventId").length() == 0 ? "0"
			: element.getAttribute("eventId")); // 游戏事件id
		this.type = GameEventActionEnum.get(Integer.parseInt(element.getAttribute("type")));	// 行为类型
		String paramsString = element.getAttribute("params"); // 行为参数
		if (paramsString != null && paramsString.length() > 0) {
			String[] paramsStrings = paramsString.split("\\|"); 
			int[] params = new int[paramsStrings.length] ; 
			for (int i = 0; i < paramsStrings.length; i++) {
				int temp = Integer.parseInt(paramsStrings[i]);
				params[i] = temp;
			}
			this.params = params ;			
		} else {
			this.params = new int[] {};
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getEventId() {
		return eventId;
	}
	
	public GameEventActionEnum getType() {
		return type;
	}
	
	public int[] getParams() {
		return params;
	}
	
}
