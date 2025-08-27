package cn.game.games.net.cross.activity;

import java.util.List;

import cn.game.games.cache.entity.GameActivity;
import cn.game.games.net.data.mapper.GameActivityMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.games.net.game.module.activity.ActivityFactory;
import cn.game.games.net.game.module.activity.GlobalActivityManager;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;

public class CrossGlobalActivityManager extends GlobalActivityManager {
	
	@Override
	protected boolean canOpen(ActivityConfig config) {
		return super.canOpen(config) && config.isCross;
	}
	
	
	/** 
	 * 从数据库中加载活动
	 * @param id
	 */
	public void load(int id) {
		List<GameActivity> activity = DAO.executeSync(GameActivityMapper.class, MapperConstant.selectByPrimaryKey, new Object[] { 0L, id });
		if (activity == null) {
			return;
		}
		ActivityConfig activityConfig = ActivityManager.instance().get(id);
		ActivityBase newActivity = ActivityFactory.initActivityBase(activityConfig, ((GameActivity) activity).getParams(), null);

		ActivityBase existing = activities.putIfAbsent(id, newActivity);
		if (existing != null) {
			log.warn("重复加载活动:{}", id);
			return;
		}
		afterLoad();
	}

	
}