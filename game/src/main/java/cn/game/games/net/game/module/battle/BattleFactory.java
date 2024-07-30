package cn.game.games.net.game.module.battle;

import java.util.HashMap;
import java.util.Map;

import cn.game.games.net.game.module.battle.impl.BattleChapterImpl;
import cn.game.games.net.game.module.battle.impl.DaoHeartImpl;
import cn.game.games.net.game.module.battle.impl.DayChallengeImpl;
import cn.game.games.net.game.module.battle.impl.HCBattleChapterImpl;
import cn.game.games.net.game.module.battle.impl.MengYanMiJingImpl;
import cn.game.games.net.game.module.battle.impl.XinMoImpl;
import cn.game.games.net.game.module.battle.impl.YaoWangImpl;

public class BattleFactory {

	public static Map<Integer, IBattleHandler> handlers = new HashMap<Integer, IBattleHandler>();

	static {
		register(new BattleChapterImpl());
		register(new DaoHeartImpl());
		register(new XinMoImpl());
		register(new YaoWangImpl());
		register(new MengYanMiJingImpl());
		register(new HCBattleChapterImpl());
		register(new DayChallengeImpl());
	}

	public static void register(IBattleHandler battleHandler) {
		handlers.put(battleHandler.getType(), battleHandler);
	}
	
	public static IBattleHandler getBattleHandler(int type) {
		return handlers.get(type);
	}

}
