package cn.game.games.net.game.module.chat;

import java.text.MessageFormat;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.core.push.PushService;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.MarqueeConfig;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.MarqueeManager;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class ChatModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.LoginFinish, EventTypeEnum.HeroQuality };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {

		case LoginFinish: {
			// 用在跑马灯广播
			PushService.getInstance().addPlayerTags(playerId, player.getServerId());
			// 全服聊天广播
			PushService.getInstance().addPlayerTags(playerId);
			break;
		}
		case HeroQuality: {
			Hero hero = event.getParameter(0);
			HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
			MarqueeConfig marqueeConfig = MarqueeManager.instance().get(2);
			if (hero.getQuality() >= marqueeConfig.Para) {

				ChatHelper.marquee(MessageFormat.format(marqueeConfig.Text, player.getData().getName(), heroConfig.name,
						GlobalConst.HeroQuality1.get(hero.getQuality())),
						player.getServerId());
			}
			break;
		}
		}
	}


	@Override
	public void buildPlayerAllInfo(Builder builder) {
	}
}
