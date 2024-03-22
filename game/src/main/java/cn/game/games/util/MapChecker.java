package cn.game.games.util;

import ch.qos.logback.core.joran.spi.JoranException;
import cn.game.util.LogbackConfig;

public class MapChecker {

	public static void main(String[] args) {
		try {
			LogbackConfig.init(true, "config/logback.xml");
		} catch (JoranException e) {
			e.printStackTrace();
		}

////		ExploreMapDataManager.getInstance().load();
//		ExploreMapDataManager.getInstance().checkBarrierRing();
	}
}
