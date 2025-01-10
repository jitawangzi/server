package cn.game.games.net.common.module.activity;

import java.util.Collection;

import cn.game.games.core.event.EventHandler;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.core.event.GlobalEvent;
import cn.game.games.net.game.module.activity.GlobalActivityManager;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;

public class ActivityService implements EventHandler {
	/** 活动数据 */
	private GlobalActivityManager globalActivityManager;
	private EventTypeEnum[] eventTypes = new EventTypeEnum[] { EventTypeEnum.ActivityOpenTime, EventTypeEnum.ActivityShutDownTime,
			EventTypeEnum.ActivityDestoryTime };

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
		GlobalEvent.getInstance().registerEventHandler(this);
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return eventTypes;
	}

	@Override
	public void handleEvent(GameEvent event) {
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
