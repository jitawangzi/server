package cn.game.games.net.game.module.activity.impl.global;

import java.util.List;

import com.google.protobuf.Message;

import cn.game.core.base.ServerContext;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.GameActivityBase;
import cn.game.games.net.game.module.rank.RankHelper;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.ActivityServerOpenRankConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.ActivityServerOpenRankManager;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@ActivityType(type = ActivityTypeEnum.ServerOpenRank)
public class ServerOpenRankActivity extends GameActivityBase {
	private static final ServerEventTypeEnum[] eventTypes = new ServerEventTypeEnum[] { ServerEventTypeEnum.NewDay };

	@Override
	public ServerEventTypeEnum[] getEventTypes() {
		return eventTypes;
	}

	@Override
	public void handleEvent(ServerEvent event) {

		switch (event.getType()) {
		case NewDay:
			if (ServerContext.getInstance().isLeader() == false) {
				return;
			}
			// 复制前一天的排行榜
			ActivityServerOpenRankConfig activityServerOpenRankConfig = ActivityServerOpenRankManager.instance().getNullable(ServerHelper.getServerOpenDay(serverId) -1); 
			if (activityServerOpenRankConfig != null) {
				RankType sourceRankType = RankType.get(activityServerOpenRankConfig.RankID); 
				RankType targetRankType = RankType.get(activityServerOpenRankConfig.RewardRankId); 
				RankHelper.copyRank(serverId, sourceRankType, targetRankType, activityServerOpenRankConfig.PlayerCount); 
				// 结算前一天的排行榜奖励
				RankService.getInstance().serverOpenActivityReward(new String[] {serverId}, activityServerOpenRankConfig.RewardRankId);
			}
			break;
		default:
			break;
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
