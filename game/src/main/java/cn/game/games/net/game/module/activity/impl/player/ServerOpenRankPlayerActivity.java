package cn.game.games.net.game.module.activity.impl.player;

import java.util.List;

import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.ActivityServerOpenRankConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.ActivityServerOpenRankManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankResponse_11000201;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@ActivityType(type = ActivityTypeEnum.ServerOpenRankPlayer)
public class ServerOpenRankPlayerActivity extends PlayerActivityBase {
	private static final EventTypeEnum[] eventTypes = new EventTypeEnum[] { EventTypeEnum.DaShengPointsAdd };

	private boolean isDayReward ; 
	
	@Override
	public EventTypeEnum[] getEventTypes() {
		return eventTypes;
	}
	@Override
	public boolean hasRed() {
		return !isDayReward;
	}

	@Override
	public boolean newDay() {
		isDayReward = false;
		return true; 
	};

	@Override
	public void handleEvent(PlayerEvent event) {

		switch (event.getType()) {
		case DaShengPointsAdd:
			// 大圣积分变化，可能会影响排名
			int serverOpenDay = ServerHelper.getServerOpenDay(serverId); 
			ActivityServerOpenRankConfig config = ActivityServerOpenRankManager.instance().getNullable(serverOpenDay); 
			if (config != null && config.RankID == RankType.DaShengLeiTaiServerOpenActivity.ID) {
				int score =  event.get(0) + event.get(1) ; 
	            RankService.getInstance().setScoreAsync(player.getServerId(), RankType.DaShengLeiTaiServerOpenActivity, player.getPlayerId(), score);
			}
			break;
		default:
			break;
		}
	
	}

	@Override
	public Message buildActivityShowInfo() {
		ActivityServerOpenRankResponse_11000201.Builder builder = ActivityServerOpenRankResponse_11000201.newBuilder();
		builder.setDayReward(isDayReward); 
		builder.setOpenDay(ServerHelper.getServerOpenDay(player.getServerId()));
		return builder.build();
	}

	@Override
	public List<RewardInfo> receive(int id) {
		if (isDayReward) {
			player.fail(ErrorMsgEnum.repeat_request);
		}
//		ActivityServerOpenRankConfig nullable = ActivityServerOpenRankManager.instance().getNullable(ServerHelper.getServerOpenDay(player.getServerId())); 
//		if (nullable == null) {
//			player.fail(ErrorMsgEnum.request_parameter_error);
//		}
		List<RewardInfo> resources = PlayerHelper.addResources(player, GlobalConst.SevenDayActivityReward, OpType.ServerOpenRankDayReward); 
		isDayReward = true; 
		return resources;
	}
}
