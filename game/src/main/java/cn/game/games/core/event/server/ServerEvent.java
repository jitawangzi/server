package cn.game.games.core.event.server;

import cn.game.core.event.AbstractEvent;

/**    
 * 服务器事件
 * 2025年5月22日 11:37:51
 * @author SYQ
 */
public class ServerEvent extends AbstractEvent<ServerEventTypeEnum> {

	public ServerEvent() {
	}

	public ServerEvent(ServerEventTypeEnum eventType, Object[] params) {
		super(eventType, params);
	}
}
