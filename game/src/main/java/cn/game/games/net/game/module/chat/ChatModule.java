package cn.game.games.net.game.module.chat;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.core.push.PushService;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class ChatModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.LoginFinish };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {

		case LoginFinish: {
//			PushService.getInstance().addPlayerTags(playerId, player.getServerId());
			// 登陆完成，添加玩家标签,目前不需要按服务器id分。
			PushService.getInstance().addPlayerTags(playerId);
			break;
		}
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
