package cn.game.games.net.common.module.activity;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.protocol.generated.config.ActivityConfig;

public class ActivityFactory {

	public static ActivityBase initActivityBase(ActivityConfig config, String saveString, Player player) {

		ActivityBase activityBase;
		if (!StringUtils.isEmpty(saveString)) {
			activityBase = JSON.parseObject(saveString, ClassManager.getInstance().getActivityClass(config.type));
		} else {
			activityBase = createActivity(config.type);
		}
		activityBase.init(config.ID, player, false);
		return activityBase;
	}

	public static ActivityBase createActivity(int type) {
		return ClassManager.getInstance().createActivityClassInstance(type);
	}

}
