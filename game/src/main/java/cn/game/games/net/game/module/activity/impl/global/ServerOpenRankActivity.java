package cn.game.games.net.game.module.activity.impl.global;

import java.util.List;

import com.google.protobuf.Message;

import cn.game.core.event.ServerEventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.GameActivityBase;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@ActivityType(type = ActivityTypeEnum.ServerOpenRank)
public class ServerOpenRankActivity extends GameActivityBase {
	private static final ServerEventTypeEnum[] eventTypes = new ServerEventTypeEnum[] { ServerEventTypeEnum.PlayerEvent };

	@Override
	public ServerEventTypeEnum[] getEventTypes() {
		return eventTypes;
	}

	@Override
	public void handleEvent(ServerEvent event) {
		if (event.getType() == ServerEventTypeEnum.PlayerEvent) {
			PlayerEvent playerEvent = event.getParameter(0); 
			switch (playerEvent.getType()) {
			
			case BattleEnd:
				
				break;
			default:
				break;
			}
		}
	}

	@Override
	public Message buildActivityShowInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unregisterEvent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void syncActivityInfo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<RewardInfo> receive(int id) {
		// TODO Auto-generated method stub
		return null;
	}


}
