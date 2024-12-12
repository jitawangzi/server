package cn.game.games.net.game.module.func;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class FuncModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.LevelUp };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {

		case LevelUp: {
			int exp = event.getIntParameter(0);
			int level = event.getIntParameter(1);
			if (exp == Asset.playerExp.ID) {
				refreshFuncOpen(level);
			}
			break;
		}
		case PLAYER_CREATE: {
			refreshFuncOpen(1);
			break;
		}
		}

	}

	public boolean isFuncOpen(int id) {
		return true;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

	public void refreshFuncOpen(int level) {
		InitialUI[] values = InitialUI.values();
		for (InitialUI initialUI : values) {
			if (initialUI.DisplayLevel == level) {
				player.handleEvent(EventTypeEnum.FuncOpen, initialUI);
			}
		}
	}
}
