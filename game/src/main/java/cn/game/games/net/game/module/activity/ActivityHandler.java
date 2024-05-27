package cn.game.games.net.game.module.activity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.activity.impl.player.FirstChargeActivity;
import cn.game.games.net.game.module.activity.impl.player.SevenDayCarnivalActivity;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeBuyRequest_11000010;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeBuyResponse_11000011;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRequest_11000007;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeResponse_11000008;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardResponse_11000013;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityListResponse_11000002;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysCarnivalRequest_11000020;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysCarnivalResponse_11000021;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**
 * 活动处理器
 */
@Component
public class ActivityHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x11;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.ActivityListRequest_11000001, (client, message) -> {
			list(client, message);
		});
//		putInvoker(PbProtocol.ActivityFirstChargeRequest_11000003, (client, message) -> {
//			firstCharge(client, message);
//		});
//		putInvoker(PbProtocol.ActivityFirstChargeBuyRequest_11000005, (client, message) -> {
//			firstChargeBuy(client, message);
//		});
		putInvoker(PbProtocol.ActivityFirstChargeRequest_11000007, (client, message) -> {
			singleCharge(client, message);
		});
		putInvoker(PbProtocol.ActivityFirstChargeBuyRequest_11000010, (client, message) -> {
			singleChargeBuy(client, message);
		});
		putInvoker(PbProtocol.ActivityFirstChargeRewardRequest_11000012, (client, message) -> {
			singleChargeReward(client, message);
		});
		putInvoker(PbProtocol.ActivitySevenDaysCarnivalRequest_11000020, (client, message) -> {
			sevenDaysCarnival(client, message);
		});
	}

	private void empty(NetClient client, Object message) {
		ActivityFirstChargeBuyRequest_11000010 req = (ActivityFirstChargeBuyRequest_11000010) message;
		ActivityFirstChargeRewardResponse_11000013.Builder resp = ActivityFirstChargeRewardResponse_11000013.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		FirstChargeActivity activityBase = (FirstChargeActivity) player.getActivityModule().get(req.getId());
		if (activityBase == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}

		client.sendProtocol(resp);
	}

	private void sevenDaysCarnival(NetClient client, Object message) {
		ActivitySevenDaysCarnivalRequest_11000020 req = (ActivitySevenDaysCarnivalRequest_11000020) message;
		ActivitySevenDaysCarnivalResponse_11000021.Builder resp = ActivitySevenDaysCarnivalResponse_11000021.newBuilder();
		int id = req.getId();
		ActivityConfig activityConfig = ActivityManager.instance().getNullable(id);
		if (activityConfig == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.config_data_not_found.getId());
			return;
		}
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		SevenDayCarnivalActivity activityBase = (SevenDayCarnivalActivity) player.getActivityModule().get(id);
		if (activityBase == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
		client.sendProtocol(activityBase.buildActivityShowInfo());
	}

	private void singleCharge(NetClient client, Object message) {
		ActivityFirstChargeRequest_11000007 req = (ActivityFirstChargeRequest_11000007) message;
		ActivityFirstChargeResponse_11000008.Builder resp = ActivityFirstChargeResponse_11000008.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		FirstChargeActivity activityBase = (FirstChargeActivity) player.getActivityModule().get(req.getId());

		if (activityBase == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}

		client.sendProtocol(activityBase.buildActivityShowInfo());
	}

	private void singleChargeBuy(NetClient client, Object message) {
		ActivityFirstChargeBuyRequest_11000010 req = (ActivityFirstChargeBuyRequest_11000010) message;
		ActivityFirstChargeBuyResponse_11000011.Builder resp = ActivityFirstChargeBuyResponse_11000011.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		int id = req.getId();
		int chargeId = req.getChargeId();
		FirstChargeActivity activityBase = (FirstChargeActivity) player.getActivityModule().get(id);
		if (activityBase == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
		if (!activityBase.buy(chargeId)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
		client.sendProtocol(resp);
	}

	private void singleChargeReward(NetClient client, Object message) {
		ActivityFirstChargeRewardRequest_11000012 req = (ActivityFirstChargeRewardRequest_11000012) message;
		ActivityFirstChargeRewardResponse_11000013.Builder resp = ActivityFirstChargeRewardResponse_11000013.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		int chargeId = req.getChargeId();
		FirstChargeActivity activityBase = (FirstChargeActivity) player.getActivityModule().get(req.getId());
		if (activityBase == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
//		SingleCharge singleCharge = activityBase.getSingleCharge(chargeId);
//		if (singleCharge == null) {
//			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
//			return;
//		}
//		if (rewardDay > DateUtil.getDay() - singleCharge.getDay()) {
//			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
//			return;
//		}
//		if (singleCharge.getSelectedIndex().contains(rewardDay)) {
//			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
//			return;
//		}
		List<RewardInfo> reward = activityBase.reward(chargeId);
		if (reward == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
		resp.addAllRewards(reward);
		client.sendProtocol(resp);
	}
	private void list(NetClient client, Object message) {
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId()); 
		ActivityListResponse_11000002.Builder resp = ActivityListResponse_11000002.newBuilder();
		Collection<ActivityInfo> activityInfos = ActivityStateManager.getInstance().getShowState();
		Map<Integer, ActivityInfo> playerState = player.getActivityModule().getShowState();
		for (ActivityInfo activityInfo : activityInfos) {
			if (!playerState.containsKey(activityInfo.getId())) {
				playerState.put(activityInfo.getId(), activityInfo);
			}
		}
		resp.addAllActivitys(playerState.values());
		client.sendProtocol(resp);
	}

	/*private void firstCharge(NetClient client, Object message) {
		ActivityFirstChargeRequest_11000003 req = (ActivityFirstChargeRequest_11000003) message;
		ActivityFirstChargeResponse_11000004.Builder resp = ActivityFirstChargeResponse_11000004.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		int id = req.getId();
		FirstChargeActivity activityBase = (FirstChargeActivity) player.getActivityModule().get(id);
		if (activityBase != null) {
			resp.setFirstCharge((FirstChargeActivityInfo) activityBase.buildActivityInfo());
		}
		client.sendProtocol(resp);
	}
	
	private void firstChargeBuy(NetClient client, Object message) {
		ActivityFirstChargeBuyRequest_11000005 req = (ActivityFirstChargeBuyRequest_11000005) message;
		ActivityFirstChargeBuyResponse_11000006.Builder resp = ActivityFirstChargeBuyResponse_11000006.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		int id = req.getId();
		int chargeId = req.getChargeId();
		List<Integer> selectedList = req.getSelectedList();
	
		FirstChargeActivity activityBase = (FirstChargeActivity) player.getActivityModule().get(id);
		if (activityBase == null || chargeId != activityBase.getChargeId()) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
		FirstChargeConfig firstChargeConfig = FirstChargeManager.instance().get(chargeId);
	//		if (firstChargeConfig.Cnt != selectedList.size()) {
	//			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
	//			return;
	//		}
		List<Integer> selectedIndex = activityBase.getSelectedIndex();
		for (Integer integer : selectedList) {
			if (selectedIndex.contains(integer)) {
				client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
				return;
			}
		}
		List<RewardInfo> rewards = activityBase.buy(chargeId, selectedList);
		resp.addAllRewards(rewards);
	
		activityBase = (FirstChargeActivity) player.getActivityModule().get(id);
		if (activityBase != null) {
			resp.setFirstCharge((FirstChargeActivityInfo) activityBase.buildActivityInfo());
		}
		client.sendProtocol(resp);
	}*/
}
