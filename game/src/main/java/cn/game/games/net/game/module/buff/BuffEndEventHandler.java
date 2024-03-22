package cn.game.games.net.game.module.buff;

import cn.game.games.cache.entity.Buff;
import cn.game.games.core.event.EventHandler;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
/**
 * buff结束 事件处理器
 */
public class BuffEndEventHandler implements EventHandler {

	private Buff buff;

	public BuffEndEventHandler(Buff buff) {
		this.buff = buff;
	}
	public BuffEndEventHandler() {
	}

	@Override
	public void handleEvent(GameEvent event) {
		// 补给小于某值
		if (event.getType() == EventTypeEnum.SupplyLessThanOneValue) {
			int value = event.getIntParameter(0);
		}
		buff.end();
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
