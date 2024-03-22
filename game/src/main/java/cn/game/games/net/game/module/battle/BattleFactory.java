package cn.game.games.net.game.module.battle;

import java.util.HashMap;
import java.util.Map;

public class BattleFactory {

	public static Map<Integer, IBattleHandler> handlers = new HashMap<Integer, IBattleHandler>();

	static {
		register(new BattleChapterImpl());
		register(new BattleEventImpl());
		register(new BattleTrainingImpl());
		register(new BattleMainlineImpl());
		register(new BattleClimbingTowerImpl());
	}

	public static void register(IBattleHandler battleHandler) {
		handlers.put(battleHandler.getType(), battleHandler);
	}
	
	public static IBattleHandler getBattleHandler(int type) {
		return handlers.get(type);
	}

}
