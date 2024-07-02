package cn.game.games.net.game.helper;

import java.util.List;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventHandler;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.EventOptionConfig;
import cn.game.protocol.generated.enume.EffectEnum;
import cn.game.protocol.generated.manager.EventOptionManager;

public class EventHelper {
	
	public static void handleEvent(long playerId, GameEvent gameEvent) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		player.handleEvent(gameEvent);
	}
	public static void registerEventHandler(long playerId, EventTypeEnum eventType, EventHandler eventHandler) {
//		
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		player.getEventModule().registerEventHandler(eventType, eventHandler);
	}
	public static void unregisterEventHandler(long playerId, EventTypeEnum eventType, EventHandler eventHandler) {
//		
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		player.getEventModule().unregisterEventHandler(eventType, eventHandler);
	}
	
	public static void registerEventHandler(long playerId, EventTypeEnum[] eventTypes, EventHandler eventHandler) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
//
//		
		player.getEventModule().registerEventHandler(eventTypes, eventHandler);
	}

	public static void registerEventHandler(long playerId, EventHandler eventHandler) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
//		
		player.getEventModule().registerEventHandler(eventHandler);
	}

	public static void unregisterEventHandler(long playerId, EventHandler eventHandler) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
//		
		player.getEventModule().unregisterEventHandler(eventHandler);
	}

	/**
	 * 事件可能产生某种类型的效果
	 * @param eventId 事件Id
	 * @param type	  效果类型
	 * @return
	 */
	public static boolean maybeHappen(int eventId, EffectEnum type) {
		List<EventOptionConfig> eventIdList = EventOptionManager.getInstance().getEventIdList(eventId);
		if (eventIdList == null) {
			return false;
		}
		for (EventOptionConfig eventOptionConfig : eventIdList) {
			int[] buffIds = eventOptionConfig.getBuffId();
			for (int buffId : buffIds) {
//				OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buffId);
//				EffectEnum effectType = buffConfig.getEffectType();
//				if (effectType == type) {
//					return true;
//				}
			}
		}
		return false;
	}
}
