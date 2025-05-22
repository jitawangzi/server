package cn.game.games.net.common.module.activity;

import java.util.Collection;

import cn.game.core.event.EventHandler;
import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.core.event.server.ServerEventRegistration;
import cn.game.games.core.event.server.ServerEventTypeEnum;
import cn.game.games.net.game.module.activity.GlobalActivityManager;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;

public class ActivityService implements EventHandler<ServerEventTypeEnum, ServerEvent> {
	/** 活动数据 */
	private GlobalActivityManager globalActivityManager;
	private ServerEventTypeEnum[] eventTypes = new ServerEventTypeEnum[] { ServerEventTypeEnum.ActivityOpenTime,
			ServerEventTypeEnum.ActivityShutDownTime, ServerEventTypeEnum.ActivityDestoryTime };

	public GlobalActivityManager defaultGlobalActivityManager() {
		return new GlobalActivityManager();
	}
	public void init() {
		if (globalActivityManager == null) {
			globalActivityManager = defaultGlobalActivityManager();
			Collection<ActivityConfig> list = ActivityManager.instance().list();
			for (ActivityConfig activityConfig : list) {
				if (activityConfig.isMultiplayer) {
					globalActivityManager.load(activityConfig.ID);
				}
			}
			globalActivityManager.checkAndOpenActivitys(null);
		}
		ServerEventRegistration.getInstance().registerEventHandler(this);
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
            globalActivityManager.open(id, null, true);
            break;
        }
		case ActivityShutDownTime: {
        	int id = event.getIntParameter(0); 
            globalActivityManager.shutdown(id);
            break;
		}
		case ActivityDestoryTime: {
        	int id = event.getIntParameter(0); 
            globalActivityManager.destroy(id, true);
            break;
		}
		default:
			break;
        }
	}
}
