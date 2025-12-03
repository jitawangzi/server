package cn.game.games.net.game.module.activity.impl.global;

import java.util.List;

import com.google.protobuf.Message;

import cn.game.core.base.ServerContext;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.manager.GameConstants;
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
	protected void afterStart() {
		refresh();
	}

	@Override
	public void handleEvent(ServerEvent event) {

		switch (event.getType()) {
		case NewDay:
			refresh();
			break;
		default:
			break;
		}

	}

	private void refresh() {
		if (ServerContext.getInstance().isLeader() == false) {
			return;
		}
		int serverOpenDay = ServerHelper.getServerOpenDay(serverId);
		ActivityServerOpenRankConfig yestodayConfig = ActivityServerOpenRankManager.instance().getNullable(serverOpenDay -1);
		ActivityServerOpenRankConfig todayConfig = ActivityServerOpenRankManager.instance().getNullable(serverOpenDay);
		if (todayConfig != null && todayConfig.RewardRankId == RankType.DaShengLeiTaiServerOpenActivity.ID) {
			// 大圣擂台榜，复制日榜数据作为初始数据
			RankHelper.copyRank(serverId, RankType.DaShengLeiTaiDay, RankType.DaShengLeiTaiServerOpenActivity,
					todayConfig.PlayerCount*2, GameConstants.BOT_MAX_ID);
		}
		// 复制前一天的排行榜，作为历史数据显示
		if (yestodayConfig != null) {
			RankType sourceRankType = RankType.get(yestodayConfig.RankID);
			RankType targetRankType = RankType.get(yestodayConfig.RewardRankId);
			if (targetRankType != RankType.DaShengLeiTaiServerOpenActivity) {
				RankHelper.copyRank(serverId, sourceRankType, targetRankType, yestodayConfig.PlayerCount);
			}
			// 结算前一天的排行榜奖励
			RankService.getInstance().serverOpenActivityReward(new String[] { serverId }, yestodayConfig.RewardRankId);
			// 结算总榜
			if (serverOpenDay - 1 == ActivityServerOpenRankManager.instance().list().size()) {
				RankService.getInstance().reward(new String[] { serverId }, RankType.TotalServerOpenActivity.ID);
			}
		}
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
