package cn.game.games.util;

import cn.game.core.net.vertx.VxHolder;
import cn.game.games.core.log.GameLogger;
import cn.game.util.DateUtil;

public class BIHelper {

	public static void start() {
		VxHolder.vertx.setPeriodic(DateUtil.MINUTE_MILLIS, r -> {
			GameLogger.heart();
		});
	}
}
