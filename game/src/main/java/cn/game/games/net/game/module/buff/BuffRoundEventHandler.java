package cn.game.games.net.game.module.buff;

import cn.game.games.cache.entity.Buff;
import cn.game.games.core.event.EventHandler;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
/**
 * buff回合变更 事件处理器
 */
public class BuffRoundEventHandler implements EventHandler {

	private Buff buff;

	public BuffRoundEventHandler(Buff buff) {
		this.buff = buff;
	}
	public BuffRoundEventHandler() {
	}

	@Override
	public void handleEvent(GameEvent event) {
		buff.consumeRound();
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

}
