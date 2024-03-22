package cn.game.games.core.event;


/**
 * 游戏事件处理器
 * 
 * @author SYQ
 * 
 */
public interface EventHandler {

	/**
	 * 监听的事件类型
	 * @return
	 */
	EventTypeEnum[] getEventTypes();
	
	/**
	 * 处理事件
	 * 
	 * @param event
	 */
	public void handleEvent(GameEvent event);
}
