package cn.game.games.net.game.module.activity;

import org.apache.commons.lang3.StringUtils;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.clazz.ClassManager;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.util.JsonUtil;

public class ActivityFactory {

	public static ActivityBase createActivityBase(ActivityConfig config, String saveString) {

		ActivityBase activityBase;
		if (!StringUtils.isEmpty(saveString)) {
			activityBase = JsonUtil.parseObjectWithType(saveString);
		} else {
			activityBase = createActivityBase(config.type);
		}
//		activityBase.init(config.ID, owner, false);
		return activityBase;
	}

	public static ActivityBase createActivityBase(int type) {
		return ClassManager.getInstance().createActivityClassInstance(type);
	}

}
