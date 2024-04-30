package cn.game.games.net.game.module.battle;

import java.util.HashMap;
import java.util.Map;

import cn.game.games.net.game.module.battle.impl.BattleChapterImpl;
import cn.game.games.net.game.module.battle.impl.BattleEventImpl;
import cn.game.games.net.game.module.battle.impl.BattleMainlineImpl;
import cn.game.games.net.game.module.battle.impl.BattleTrainingImpl;

public class BattleFactory {

	public static Map<Integer, IBattleHandler> handlers = new HashMap<Integer, IBattleHandler>();

	static {
		register(new BattleChapterImpl());
		register(new BattleEventImpl());
		register(new BattleTrainingImpl());
		register(new BattleMainlineImpl());
	}

	public static void register(IBattleHandler battleHandler) {
		handlers.put(battleHandler.getType(), battleHandler);
	}
	
	public static IBattleHandler getBattleHandler(int type) {
		return handlers.get(type);
	}

}
