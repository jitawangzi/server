package cn.game.games.net.game.module.chat;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class ChatModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] {};

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		}
	}


	@Override
	public void buildPlayerAllInfo(Builder builder) {
	}

	@Override
	public void initFromDbAfter() {
		// TODO Auto-generated method stub

	}
}
