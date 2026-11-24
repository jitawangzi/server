package cn.game.games.net.game.module.activity;

import cn.game.protocol.generated.config.ActivityConfig;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;

/**    
 * Game中的全体活动，只是控制活动开启、关闭等，不保存活动数据
 * 2025年8月25日 10:22:51
 * @author SYQ
 */
public class GameGlobalActivityManager extends GlobalActivityManager{

	
	@Override
	public boolean canOpen(ActivityConfig config) {
		return !config.isCross && super.canOpen(config);
	}

	@Override
	protected void afterActivityOpen(ActivityBase activity) {
		// Game 节点全体活动保持纯内存态，禁止写 t_game_activity
	}

	@Override
	protected void afterActivityDestroy(ActivityBase activity) {
		// Game 节点全体活动保持纯内存态，禁止删 t_game_activity
	}

	@Override
	public Future<@Nullable Object> insert(ActivityBase activity) {
		return Future.succeededFuture();
	}

	@Override
	public Future<@Nullable Object> delete(ActivityBase activity) {
		return Future.succeededFuture();
	}

	/** 
	 * 从数据库中加载活动
	 * @param id
	 */
//	public void loadAll(String serverId) {
//		Object activity = DAO.executeSync(GameActivityMapper.class, MapperConstant.selectByPrimaryKey, new Object[] { 0L, id });
//		if (activity == null) {
//			return;
//		}
//		ActivityConfig activityConfig = ActivityManager.instance().get(id);
//		ActivityBase newActivity = ActivityFactory.initActivityBase(activityConfig, ((GameActivity) activity).getParams(), null);
//
//		ActivityBase existing = activities.putIfAbsent(id, newActivity);
//		if (existing != null) {
//			log.warn("重复加载活动:{}", id);
//			return;
//		}
//		afterLoad();
//	}
	
}
