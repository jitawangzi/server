package cn.game.games.net.common.module.activity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.game.core.base.ServerContext;
import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
import cn.game.core.event.EventHandler;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.zookeeper.server.ValidServerService;
import cn.game.games.cache.entity.GameActivity;
import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.data.mapper.GameActivityMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.module.activity.ActivityHelper;
import cn.game.games.net.game.module.activity.GameGlobalActivityManager;
import cn.game.games.net.game.module.activity.GlobalActivityManager;
import cn.game.games.util.DAO;
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
		for (ActivityConfig activityConfig : collect) {
			defaultGlobalActivityManager.open(activityConfig.ID, null, false);
		}
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
	
	/** 
	 * 从数据库中加载活动
	 * @param id
	 */
	public void loadAll() {
		ValidServerService validGameService = ServerContext.getInstance().getValidGameService(); 
		Map<String, VirtualServerView> validServers = validGameService.getValidServers(); 
		
		validServers.forEach((k, v) -> {
			GlobalActivityManager defaultGlobalActivityManager = defaultGlobalActivityManager(); 
			defaultGlobalActivityManager.setServerId(k); 
			serverActivitysMap.put(k, defaultGlobalActivityManager);
		});
		List<GameActivity> allActivity = DAO.executeSync(GameActivityMapper.class, MapperConstant.selectAll);
		// 先初始化存在的活动，排除已经过期的
		for (GameActivity gameActivity : allActivity) {
			ActivityConfig activityConfig = ActivityManager.instance().get(gameActivity.getConfigId()); 
			if (gameActivity.getServerId().equals(AbstractActivityManager.GLOBAL_SERVER_ID)) {
				if (sharedActivityManager.canOpen(activityConfig)) {
					sharedActivityManager.initFromDb(activityConfig, gameActivity.getParams(), null);
				}
			}else {
				AbstractActivityManager abstractActivityManager = serverActivitysMap.get(gameActivity.getServerId());
				if (abstractActivityManager.canOpen(activityConfig)) {
					abstractActivityManager.initFromDb(activityConfig, gameActivity.getParams(), null);
				}
			}
		}
		// 检查新的能开启的活动
		sharedActivityManager.checkAndOpenActivitys(null);
		
		List<ActivityConfig> collect = ActivityManager.instance().getOpenTypeList(ActivityHelper.OPENTYPE_SERVER_OPEN_DAY);
		serverActivitysMap.forEach((k, v) -> {
			for (ActivityConfig activityConfig : collect) {
				if (v.canOpen(activityConfig)) {
					v.open(activityConfig.ID, null, false);
				}
			}
		});
	}
	
	public void close() {
		ServerEventBus.getInstance().unregister(this);
	}
	
}
