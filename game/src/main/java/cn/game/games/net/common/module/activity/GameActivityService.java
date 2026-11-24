package cn.game.games.net.common.module.activity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
import cn.game.core.event.EventHandler;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.game.helper.ServerHelper;
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
	private ServerEventTypeEnum[] eventTypes = new ServerEventTypeEnum[] {
			ServerEventTypeEnum.VirtualServerOpen, ServerEventTypeEnum.ActivityOpenTime,
			ServerEventTypeEnum.ActivityShutDownTime, ServerEventTypeEnum.ActivityDestoryTime,
			ServerEventTypeEnum.NewDay };

	/** 各个服的不同活动,例如开服时间不同  */
	private Map<String, AbstractActivityManager> serverActivitysMap = new ConcurrentHashMap<String, AbstractActivityManager>() ; 
	/** 所有服都一样的活动 */
	private AbstractActivityManager sharedActivityManager = defaultGlobalActivityManager() ; 
	
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
		loadAll(); 
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
		activityManager.endTimeTask();
		activityManager.destroyTimeTask();
		return activityManager; 
	}
	/** 
	 * 初始化按开服时间开启的活动
	 * @return
	 */
	private void initActivityManagerByServer(String serverId) {
		AbstractActivityManager abstractActivityManager = serverActivitysMap.get(serverId); 
		if (abstractActivityManager != null) {
			return;
		}
		GlobalActivityManager defaultGlobalActivityManager = defaultGlobalActivityManager(); 
		defaultGlobalActivityManager.setServerId(serverId); 
		serverActivitysMap.put(serverId, defaultGlobalActivityManager);
		
		List<ActivityConfig> collect = ActivityManager.instance().getOpenTypeList(ActivityHelper.OPENTYPE_SERVER_OPEN_DAY);
		if (collect != null) {
			for (ActivityConfig activityConfig : collect) {
				if (defaultGlobalActivityManager.canOpen(activityConfig)) {
					defaultGlobalActivityManager.open(activityConfig.ID, null, false);
				}
			}
		}
		defaultGlobalActivityManager.endTimeTask();
		defaultGlobalActivityManager.destroyTimeTask();
	}

	@Override
	public ServerEventTypeEnum[] getEventTypes() {
		return eventTypes;
	}

	@Override
	public void handleEvent(ServerEvent event) {
        switch (event.getType()) {
        case VirtualServerOpen:{
        	initActivityManagerByServer(event.getStringParameter(0));
        	break;
        }
        case ActivityOpenTime:{
        	int id = event.getIntParameter(0); 
            sharedActivityManager.open(id, null, true);
            sharedActivityManager.endTimeTask();
            sharedActivityManager.destroyTimeTask();
            break;
        }
		case ActivityShutDownTime: {
        	int id = event.getIntParameter(0); 
            sharedActivityManager.end(id,true);
            break;
		}
		case ActivityDestoryTime: {
        	int id = event.getIntParameter(0); 
            sharedActivityManager.destroy(id, true);
            break;
		}
		case NewDay: {
			// 1. 跨天日结结算：先推进已有活动日结（榜单结算/发奖），严防被提前 end 注销事件
			sharedActivityManager.newDay();
			serverActivitysMap.values().forEach(AbstractActivityManager::newDay);

			// 2. 到期检查：将已经到达结束或销毁时刻的活动推进到对应终态
			sharedActivityManager.checkExpired();
			serverActivitysMap.values().forEach(AbstractActivityManager::checkExpired);

			// 3. 检查今日新满足条件的活动
			sharedActivityManager.checkAndOpenActivitys(null);
			List<ActivityConfig> openDayConfigs = ActivityManager.instance().getOpenTypeList(ActivityHelper.OPENTYPE_SERVER_OPEN_DAY);
			if (openDayConfigs != null) {
				serverActivitysMap.values().forEach(mgr -> {
					for (ActivityConfig activityConfig : openDayConfigs) {
						if (mgr.canOpen(activityConfig)) {
							mgr.open(activityConfig.ID, null, false);
						}
					}
				});
			}

			// 4. 重新挂载到期与销毁定时器（防重取消并更新剩余延迟）
			sharedActivityManager.endTimeTask();
			sharedActivityManager.destroyTimeTask();
			serverActivitysMap.values().forEach(mgr -> {
				mgr.endTimeTask();
				mgr.destroyTimeTask();
			});
			break;
		}
		default:
			break;
        }
	}
	
	public AbstractActivityManager getServerActivityManager(String serverId) {
        return serverActivitysMap.get(serverId);
	}
	
	/** 
	 * 内存态恢复活动，彻底不读 t_game_activity
	 */
	public void loadAll() {
		Map<String, VirtualServerView> validServers = ServerHelper.getServerOpenMap(); 
		if (validServers != null) {
			validServers.forEach((k, v) -> {
				GlobalActivityManager defaultGlobalActivityManager = defaultGlobalActivityManager(); 
				defaultGlobalActivityManager.setServerId(k); 
				serverActivitysMap.put(k, defaultGlobalActivityManager);
			});
		}
		
		// 检查新的能开启的活动
		sharedActivityManager.checkAndOpenActivitys(null);
		sharedActivityManager.endTimeTask();
		sharedActivityManager.destroyTimeTask();
		
		List<ActivityConfig> collect = ActivityManager.instance().getOpenTypeList(ActivityHelper.OPENTYPE_SERVER_OPEN_DAY);
		if (collect != null) {
			serverActivitysMap.forEach((k, v) -> {
				for (ActivityConfig activityConfig : collect) {
					if (v.canOpen(activityConfig)) {
						v.open(activityConfig.ID, null, false);
					}
				}
				v.endTimeTask();
				v.destroyTimeTask();
			});
		}
	}
	
	public void close() {
		ServerEventBus.getInstance().unregister(this);
	}
	
}
