package cn.game.games.net.game.module.develop.fairyfriend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.FairyFriendFightConfig;
import cn.game.protocol.generated.config.FairyFriendFightTravelingConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.FairyFriendFightManager;
import cn.game.protocol.generated.manager.FairyFriendFightTravelingManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendFightRequest_27000003;
import cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendFightResponse_27000004;
import cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendFightRewardRequest_27000005;
import cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendFightRewardResponse_27000006;
import cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendGiftRequest_27000001;
import cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendGiftResponse_27000002;
import cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendTravelRequest_27000007;
import cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendTravelResponse_27000008;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.Rnd;

@Component
public class FairyFriendHandler extends GameBaseHandler {

	@Override
	protected int getModule() {
		return 0x27;
	}

	@Override
	protected InitialUI getInitialUI() {
		return InitialUI.FairyFriends;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.FairyFriendFightRequest_27000003, this::fight);
		putInvoker(PbProtocol.FairyFriendFightRewardRequest_27000005, this::fightReward);
		putInvoker(PbProtocol.FairyFriendGiftRequest_27000001, this::gift);
		putInvoker(PbProtocol.FairyFriendTravelRequest_27000007, this::travel);

	}


	private void travel(NetClient client, Object message) {
		FairyFriendTravelRequest_27000007 req = (FairyFriendTravelRequest_27000007) message;
		FairyFriendTravelResponse_27000008.Builder resp = FairyFriendTravelResponse_27000008.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.FairyFriends)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		int count = req.getCount();
		if (count <= 0 || count > 10) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
		PlayerHelper.delResources(player, Asset.TravelStamina.ID, GlobalConst.FairyFriendConsume * count, OpType.FairyFriend);
		FairyFriendModule module = player.getModule(FairyFriendModule.class);
		Map<FairyFriend, Integer> updateFairyFriends = new HashMap<FairyFriend, Integer>();
		for (int i = 0; i < count; i++) {
			FairyFriendFightTravelingConfig config = Rnd.randomElement(FairyFriendFightTravelingManager.instance().list(), r -> r.PositionWeight);
			int expAdd = config.Favorability[1];
			// 经验奖励
			int randomIndex = Rnd.randomIndex(config.FairyListIDWeight);
			int fairyId = config.FairyListID[randomIndex];
			// 通用奖励
			List<RewardInfo> reward = PlayerHelper.addReward(player, config.RandomID, OpType.FairyFriend);
			resp.addAllReward(reward);
			resp.addTravelId(config.ID);
			FairyFriend fairyFriend = module.get(fairyId);
			if (fairyFriend == null) {
				continue;
			}
			int[] exp = PlayerHelper.addExp(Asset.Favorability.ID, fairyId, fairyFriend.getLevel(), fairyFriend.getExp(), expAdd);
			fairyFriend.setExp(exp[0]);
			fairyFriend.setLevel(exp[1]);
			updateFairyFriends.compute(fairyFriend, (k, v) -> v == null ? expAdd : v + expAdd);
		}
		updateFairyFriends.forEach((k, v) -> {
			resp.addFavorabilityCount(v);
			resp.addFairyFriend(k.toProto());
		});
		player.fireAndHandleEvent(EventTypeEnum.FairyFriendsTravel, count);
		client.sendProtocol(resp.build());
	}

	private void fightReward(NetClient client, Object message) {
		FairyFriendFightRewardRequest_27000005 req = (FairyFriendFightRewardRequest_27000005) message;
		FairyFriendFightRewardResponse_27000006.Builder resp = FairyFriendFightRewardResponse_27000006.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.FairyFriends)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		int id = req.getId(); 
		FairyFriendModule module = player.getModule(FairyFriendModule.class);
		Map<Integer, Integer> fightMap = module.getFightMap();
		Integer fightId = fightMap.get(id);
		if (fightId == null || fightId == 2) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
		fightMap.put(id, 2);
		FairyFriendFightConfig fairyFriendFightConfig = FairyFriendFightManager.instance().get(id);

		List<RewardInfo> reward = PlayerHelper.addReward(player, fairyFriendFightConfig.RandomID, OpType.FairyFriend);
		resp.addAllReward(reward);
		client.sendProtocol(resp.build());
	}

	private void fight(NetClient client, Object message) {
		FairyFriendFightRequest_27000003 req = (FairyFriendFightRequest_27000003) message;
		FairyFriendFightResponse_27000004.Builder resp = FairyFriendFightResponse_27000004.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.FairyFriends)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		int id = req.getId();
		FairyFriendFightConfig fairyFriendFightConfig = FairyFriendFightManager.instance().get(id);
		FairyFriendModule module = player.getModule(FairyFriendModule.class);

		FairyFriend fairyFriend = module.get(fairyFriendFightConfig.FairyListID);
		if (fairyFriend == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		if (player.getDevelopModule().getHeavenlyDaoLevel() < fairyFriendFightConfig.Condition[0] ||
				fairyFriend.getLevel() < fairyFriendFightConfig.Condition[1]) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.condition_check_error.getId());
			return;
		}
		Map<Integer, Integer> fightMap = module.getFightMap();
		fightMap.put(id, 1);

		client.sendProtocol(resp.build());
	}

	private void gift(NetClient client, Object message) {
		FairyFriendGiftRequest_27000001 req = (FairyFriendGiftRequest_27000001) message;
		FairyFriendGiftResponse_27000002.Builder resp = FairyFriendGiftResponse_27000002.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (!player.isFuncOpen(InitialUI.FairyFriends)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.func_not_open.getId());
			return;
		}
		int id = req.getId();
		int itemId = req.getItemId();
		int count = req.getCount();

		FairyFriendModule module = player.getModule(FairyFriendModule.class);
		FairyFriend fairyFriend = module.get(id);
		if (fairyFriend == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		// 校验数量。
		PlayerHelper.delResources(player, itemId, count, OpType.FairyFriend);
		int addExp = GlobalConst.FairyFriendGift.get(itemId) * count;
		int[] exp = PlayerHelper.addExp(Asset.Favorability.ID, id, fairyFriend.getLevel(), fairyFriend.getExp(), addExp);
		fairyFriend.setExp(exp[0]);
		fairyFriend.setLevel(exp[1]);
		resp.setFairyFriendInfo(fairyFriend.toProto());
		player.fireAndHandleEvent(EventTypeEnum.FairyFriendsGift, count);

		client.sendProtocol(resp.build());
	}
}

