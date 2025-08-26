package cn.game.games.net.common.module.activity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.game.core.base.ServerContext;
import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
import cn.game.core.event.EventHandler;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.zookeeper.server.ValidServerService;
import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.game.module.activity.ActivityHelper;
import cn.game.games.net.game.module.activity.GameGlobalActivityManager;
import cn.game.games.net.game.module.activity.GlobalActivityManager;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;

/**    
 * Game服务器混服的活动管理器，保存所有服务器的活动状态
 * 注意这里只是保存活动状态，并且这些活动本身不存储活动数据。 
 * 也就是所有的Game进程都有相同的一份数据
 * 2025年8月22日 18:28:40
 * @author SYQ
 */
public class GameActivityService implements EventHandler<ServerEventTypeEnum, ServerEvent> {
	private ServerEventTypeEnum[] eventTypes = new ServerEventTypeEnum[] {ServerEventTypeEnum.VirtualServerOpen, ServerEventTypeEnum.ActivityOpenTime,
			ServerEventTypeEnum.ActivityShutDownTime, ServerEventTypeEnum.ActivityDestoryTime };

	/** 各个服的不同活动  */
	private Map<String, AbstractActivityManager> serverActivitysMap ; 
	/** 所有服都一样的活动 */
	private AbstractActivityManager sharedActivityManager ; 
	
	private static class SingletonHolder {
		private static final GameActivityService instance = new GameActivityService();
	}
	protected GameActivityService() {
		init(); 
	}
	public static GameActivityService getInstance() {
		return SingletonHolder.instance;
	}
	
	public GlobalActivityManager defaultGlobalActivityManager() {
		return new GameGlobalActivityManager();
	}
	public GameActivityService init() {
		if (sharedActivityManager == null) {
			sharedActivityManager = initActivityManager();
		}
		if (serverActivitysMap == null) {
			serverActivitysMap = initActivityManagerByServerOpenTime();
		}
		ServerEventBus.getInstance().register(this);
		return this;
	}
	/** 
	 * 初始化按时间开启的活动
	 * @return
	 */
	private AbstractActivityManager initActivityManager() {
		AbstractActivityManager activityManager = defaultGlobalActivityManager();
		activityManager.checkAndOpenActivitys(null);
		return activityManager; 
	}
	/** 
	 * 初始化按开服时间开启的活动
	 * @return
	 */
	private Map<String, AbstractActivityManager> initActivityManagerByServerOpenTime() {
		Map<String, AbstractActivityManager> retMap  = new ConcurrentHashMap<String, AbstractActivityManager>();
		ValidServerService validGameService = ServerContext.getInstance().getValidGameService(); 
		Map<String, VirtualServerView> validServers = validGameService.getValidServers(); 
		
		validServers.forEach((k, v) -> {
			GlobalActivityManager defaultGlobalActivityManager = defaultGlobalActivityManager(); 
			defaultGlobalActivityManager.setServerId(k); 
			retMap.put(k, defaultGlobalActivityManager);
		});
		
		List<ActivityConfig> collect = ActivityManager.instance().getOpenTypeList(ActivityHelper.OPENTYPE_SERVER_OPEN_DAY);
		
		retMap.forEach((k, v) -> {
			for (ActivityConfig activityConfig : collect) {
				if (v.canOpen(activityConfig)) {
					v.open(activityConfig.ID, null, false);
				}
			}
		});
		return retMap; 
	}

	@Override
	public ServerEventTypeEnum[] getEventTypes() {
		return eventTypes;
	}

	@Override
	public void handleEvent(ServerEvent event) {
        switch (event.getType()) {
        case VirtualServerOpen:{
        	this.serverActivitysMap = initActivityManagerByServerOpenTime();
        	break;
        }
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
	
	public AbstractActivityManager getServerActivityManager(String serverId) {
        return serverActivitysMap.get(serverId);
	}
}
