package cn.game.games.net.game.module.develop.hccommon;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.player.VarConstant;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class HCCommonModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };

	private boolean isBattleSpeedUnlock;

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {

		case NewDay: {
//			freeHcHeroUpTimes = 0;
//			freeHcHeroItemTimes = 0;
			break;
		}
		}
	}


	public boolean isBattleSpeedUnlock() {
		return isBattleSpeedUnlock;
	}

	public void setBattleSpeedUnlock(boolean isBattleSpeedUnlock) {
		this.isBattleSpeedUnlock = isBattleSpeedUnlock;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		builder.setBattleSpeedUnlock(isBattleSpeedUnlock);
		builder.setBattleSpeedAdsCount(player.getVarModule().getVar(VarConstant.BATTLE_SPEED_ADS_COUNT));
	}

}
