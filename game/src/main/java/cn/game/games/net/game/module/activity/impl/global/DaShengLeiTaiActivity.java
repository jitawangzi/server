package cn.game.games.net.game.module.activity.impl.global;

import cn.game.core.event.ServerEventTypeEnum;
import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.GameActivityBase;
import cn.game.protocol.generated.enume.ActivityTypeEnum;

@ActivityType(type = ActivityTypeEnum.DaShengLeiTai)
public class DaShengLeiTaiActivity extends GameActivityBase {

	@Override
	public ServerEventTypeEnum[] getEventTypes() {
		return null;
	}

	@Override
	public void handleEvent(ServerEvent event) {
		
	}
}
