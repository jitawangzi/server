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
import cn.game.protocol.generated.config.FirstChargeConfig;
import cn.game.protocol.generated.manager.FirstChargeManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeBuyRequest_11000005;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeBuyResponse_11000006;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRequest_11000003;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeResponse_11000004;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityListResponse_11000002;
import cn.game.protocol.protobuf.ActivityMsg.FirstChargeActivityInfo;
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
		putInvoker(PbProtocol.ActivityFirstChargeRequest_11000003, (client, message) -> {
			firstCharge(client, message);
		});
		putInvoker(PbProtocol.ActivityFirstChargeBuyRequest_11000005, (client, message) -> {
			firstChargeBuy(client, message);
		});
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

	private void firstCharge(NetClient client, Object message) {
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
		if (firstChargeConfig.Cnt != selectedList.size()) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
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
	}
}
