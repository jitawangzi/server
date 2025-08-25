package cn.game.games.net.common.module.activity;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.game.core.event.EventHandler;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.game.module.activity.GameGlobalActivityManager;
import cn.game.games.net.game.module.activity.GlobalActivityManager;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;

public class CrossActivityService implements EventHandler<ServerEventTypeEnum, ServerEvent> {
	private ServerEventTypeEnum[] eventTypes = new ServerEventTypeEnum[] { ServerEventTypeEnum.ActivityOpenTime,
			ServerEventTypeEnum.ActivityShutDownTime, ServerEventTypeEnum.ActivityDestoryTime };


	/** 各个服的不同活动  */
	private Map<String, AbstractActivityManager> serverActivitysMap = new ConcurrentHashMap<String, AbstractActivityManager>(); 
	/** 所有服都一样的活动 */
	private AbstractActivityManager sharedActivityManager = new GameGlobalActivityManager(); 
	
	
	public GlobalActivityManager defaultGlobalActivityManager() {
		return new GameGlobalActivityManager();
	}
	public void init() {
		if (sharedActivityManager == null) {
			sharedActivityManager = defaultGlobalActivityManager();
			Collection<ActivityConfig> list = ActivityManager.instance().list();
			for (ActivityConfig activityConfig : list) {
				if (activityConfig.isMultiplayer) {
					sharedActivityManager.load(activityConfig.ID);
				}
			}
			sharedActivityManager.checkAndOpenActivitys(null);
		}
		ServerEventBus.getInstance().register(this);
	}

	@Override
	public ServerEventTypeEnum[] getEventTypes() {
		return eventTypes;
	}

	@Override
	public void handleEvent(ServerEvent event) {
        switch (event.getType()) {
        case ActivityOpenTime:{
        	int id = event.getIntParameter(0); 
            sharedActivityManager.open(id, null, true);
            break;
        }
		case ActivityShutDownTime: {
        	int id = event.getIntParameter(0); 
            sharedActivityManager.shutdown(id);
            break;
		}
		case ActivityDestoryTime: {
        	int id = event.getIntParameter(0); 
            sharedActivityManager.destroy(id, true);
            break;
		}
		default:
			break;
        }
	}
}
