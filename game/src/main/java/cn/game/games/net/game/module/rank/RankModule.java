package cn.game.games.net.game.module.rank;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.protocol.generated.config.RankConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.RankManager;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class RankModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.ChapterFirstWin, EventTypeEnum.LevelUp };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {

		case LevelUp: {
			int type = event.getIntParameter(0);
			int level = event.getIntParameter(1);
			if (type == Asset.playerExp.ID) {
				RankConfig rankConfig = RankManager.instance().get(RankType.Level.ID);
				if (level >= rankConfig.Request) {
					RankService.getInstance().setScore(player.getServerId(), RankType.Level, playerId, level);
				}
			}
			break;
		}
		case ChapterFirstWin: {
			RankConfig rankConfig = RankManager.instance().get(RankType.Battle.ID);
			int battleId = event.getIntParameter(0);

			if (battleId == rankConfig.Request || BattleHelper.isPreBattle(rankConfig.Request, battleId)) {
				RankService.getInstance().setScore(player.getServerId(), RankType.Battle, playerId, battleId);
			}
			break;
		}
		}
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initFromDbAfter() {
		// TODO Auto-generated method stub

	}

}
