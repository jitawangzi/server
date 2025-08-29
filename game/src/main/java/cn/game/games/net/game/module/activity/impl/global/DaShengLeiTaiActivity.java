package cn.game.games.net.game.module.activity.impl.global;

import java.util.List;

import com.google.protobuf.Message;

import cn.game.core.event.ServerEventTypeEnum;
import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.GameActivityBase;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@ActivityType(type = ActivityTypeEnum.DaShengLeiTai)
public class DaShengLeiTaiActivity extends GameActivityBase {

	@Override
	public ServerEventTypeEnum[] getEventTypes() {
		return null;
	}

	@Override
	public void handleEvent(ServerEvent event) {
		
	}

	@Override
	public Message buildActivityShowInfo() {
		// TODO Auto-generated method stub
		return null;
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
