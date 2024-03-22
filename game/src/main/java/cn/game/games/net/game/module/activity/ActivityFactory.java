package cn.game.games.net.game.module.activity;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.clazz.ClassManager;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;

public class ActivityFactory {

	public static ActivityBase initActivityBase(ActivityConfig config, String saveString, Player player) {

		ActivityTypeEnum type = config.getType(); 
		ActivityBase activityBase;
		if (!StringUtils.isEmpty(saveString)) {
			activityBase = JSON.parseObject(saveString, ClassManager.getInstance().getActivityClass(type));
		} else {
			activityBase = createActivity(type);
		}
		activityBase.init(config.getId(), player, false);
		return activityBase;
	}

	public static ActivityBase createActivity(ActivityTypeEnum type) {
		return ClassManager.getInstance().createActivityClassInstance(type);
	}

}
