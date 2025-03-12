package cn.game.games.net.game.handler;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.google.protobuf.ProtocolStringList;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Friend;
import cn.game.games.cache.entity.FriendApplication;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.helper.FriendHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.friend.FriendModule;
import cn.game.games.util.DAO;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.FriendMsg.FriendAddPush_30000023;
import cn.game.protocol.protobuf.FriendMsg.FriendApplicationRequest_30000007;
import cn.game.protocol.protobuf.FriendMsg.FriendApplicationResponse_30000008;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyListResponse_30000054;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyPush_30000022;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyRequest_30000005;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyResponse_30000006;
import cn.game.protocol.protobuf.FriendMsg.FriendBlackListResponse_30000052;
import cn.game.protocol.protobuf.FriendMsg.FriendBlackRequest_30000010;
import cn.game.protocol.protobuf.FriendMsg.FriendBlackResponse_30000011;
import cn.game.protocol.protobuf.FriendMsg.FriendDelPush_30000024;
import cn.game.protocol.protobuf.FriendMsg.FriendDeleteRequest_30000009;
import cn.game.protocol.protobuf.FriendMsg.FriendDeleteResponse_3000000a;
import cn.game.protocol.protobuf.FriendMsg.FriendGiftPush_30000028;
import cn.game.protocol.protobuf.FriendMsg.FriendGiftReceiveRequest_30000014;
import cn.game.protocol.protobuf.FriendMsg.FriendGiftReceiveResponse_30000015;
import cn.game.protocol.protobuf.FriendMsg.FriendGiftRequest_30000012;
import cn.game.protocol.protobuf.FriendMsg.FriendGiftResponse_30000013;
import cn.game.protocol.protobuf.FriendMsg.FriendInfo;
import cn.game.protocol.protobuf.FriendMsg.FriendListRequest_30000001;
import cn.game.protocol.protobuf.FriendMsg.FriendListResponse_30000002;
import cn.game.protocol.protobuf.FriendMsg.FriendRecommendRequest_30000003;
import cn.game.protocol.protobuf.FriendMsg.FriendRecommendResponse_30000004;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import io.vertx.core.Future;

@Component
public class FriendHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x30;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.FriendListRequest_30000001, this::list);
		putInvoker(PbProtocol.FriendBlackListRequest_30000051, this::blackList);
		putInvoker(PbProtocol.FriendApplyListRequest_30000053, this::applyList);
		putInvoker(PbProtocol.FriendRecommendRequest_30000003, this::recommend);
//		putInvoker(PbProtocol.FriendSearchRequest_30000020, this::search);
		putInvoker(PbProtocol.FriendApplyRequest_30000005, this::apply);
		putInvoker(PbProtocol.FriendApplicationRequest_30000007, this::application);
		putInvoker(PbProtocol.FriendDeleteRequest_30000009, this::delete);
		putInvoker(PbProtocol.FriendBlackRequest_30000010, this::black);
		putInvoker(PbProtocol.FriendGiftRequest_30000012, this::gift);
		putInvoker(PbProtocol.FriendGiftReceiveRequest_30000014, this::giftReceive);
		putInvoker(PbProtocol.FriendApplyPush_30000022, this::remoteApply);
		putInvoker(PbProtocol.FriendAddPush_30000023, this::remoteAdd);
		putInvoker(PbProtocol.FriendDelPush_30000024, this::remoteDelete);
		putInvoker(PbProtocol.FriendGiftPush_30000028, this::remoteGift);

	}

	protected void list(NetClient client, Object message) {

		FriendListRequest_30000001 req = (FriendListRequest_30000001) message;
//		boolean local = req.getLocal();

		FriendListResponse_30000002.Builder response = FriendListResponse_30000002.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		FriendModule friendModule = player.getModule(FriendModule.class);
		List<Friend> allFriends = friendModule.getAllFriends();
		List<String> ids = allFriends.stream().map(r -> r.getFriendId()).map(r -> CacheType.PLAYER_SIMPLE.key(r)).collect(Collectors.toList());
		Future<List<SimplePlayer>> multiGetAsync = RedisLocalCache.getInstance().multiGetAsync(ids);
		multiGetAsync.map(result -> {
			for (int i = 0; i < result.size(); i++) {
				SimplePlayer simplePlayer = result.get(i);
				FriendInfo.Builder friendBuilder = FriendInfo.newBuilder();
				Friend friend = allFriends.get(i);
				friendBuilder.setGift(friend.toFriendGiftInfo());
				friendBuilder.setPlayer(simplePlayer.toSimplePlayerInfo());
				response.addFriends(friendBuilder);
			}
			client.sendProtocol(response.build());
			return null;
		}).onFailure(player::handleFail);
	}

	protected void blackList(NetClient client, Object message) {

		FriendBlackListResponse_30000052.Builder resp = FriendBlackListResponse_30000052.newBuilder();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		FriendModule friendModule = player.getModule(FriendModule.class);
		List<Friend> allFriends = friendModule.getAllBlack();
		List<String> ids = allFriends.stream().map(r -> r.getFriendId()).map(r -> CacheType.PLAYER_SIMPLE.key(r)).collect(Collectors.toList());
		Future<List<SimplePlayer>> multiGetAsync = RedisLocalCache.getInstance().multiGetAsync(ids);
		multiGetAsync.onSuccess(result -> {
			for (int i = 0; i < result.size(); i++) {
				SimplePlayer simplePlayer = result.get(i);
				resp.addPlayers(simplePlayer.toSimplePlayerInfo());
			}
			client.sendProtocol(resp.build());
		}).onFailure(player::handleFail);
	}

	protected void applyList(NetClient client, Object message) {

		FriendApplyListResponse_30000054.Builder resp = FriendApplyListResponse_30000054.newBuilder();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		FriendModule friendModule = player.getModule(FriendModule.class);
		List<String> ids = friendModule
				.getAllApplications()
				.values()
				.stream()
				.map(r -> r.getApplyPlayerId())
				.map(r -> CacheType.PLAYER_SIMPLE.key(r))
				.collect(Collectors.toList());
		Future<List<SimplePlayer>> multiGetAsync = RedisLocalCache.getInstance().multiGetAsync(ids);
		multiGetAsync.map(result -> {
			resp.addAllPlayers(PbBuilder.buildSimplePlayerInfos(result));
			client.sendProtocol(resp.build());
			return null;
		}).onFailure(player::handleFail);
	}

	protected void recommend(NetClient client, Object message) {
		FriendRecommendRequest_30000003 req = (FriendRecommendRequest_30000003) message;
//		boolean refresh = req.getRefresh();
		FriendRecommendResponse_30000004.Builder resp = FriendRecommendResponse_30000004.newBuilder();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		FriendModule friendModule = player.getModule(FriendModule.class);
		Future<List<SimplePlayer>> playersFuture = null;
		if (friendModule.getRefreshCount() >= GlobalConst.FriendFresh) {
			playersFuture = RedisLocalCache
					.getInstance()
					.multiGetAsync(CacheType.PLAYER_SIMPLE, friendModule.getLastRefreshPlayers().stream().map(String::valueOf).toArray(String[]::new));
		} else {
			playersFuture = PlayerManager.getInstance().searchPlayersAsync(player);
		}
		friendModule.setRefreshCount(friendModule.getRefreshCount() + 1);
		playersFuture.map(result -> {
			resp.addAllPlayers(PbBuilder.buildSimplePlayerInfos(result));
			client.sendProtocol(resp.build());
			return null;
		}).onFailure(player::handleFail);

	}
	protected void apply(NetClient client, Object message) {
		FriendApplyRequest_30000005 request = (FriendApplyRequest_30000005) message;
		FriendApplyResponse_30000006.Builder resp = FriendApplyResponse_30000006.newBuilder();

		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		FriendModule myFriendModule = player.getModule(FriendModule.class);

		List<String> friendIdList = request.getPlayerIdsList();
		if (myFriendModule.getApplicationCount() + friendIdList.size() > GlobalConst.FriendApplicationMax) {
			client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
			return;
		}

		myFriendModule.setApplicationCount(myFriendModule.getApplicationCount() + friendIdList.size());

		for (int i = 0; i < friendIdList.size(); i++) {
			Long id = Long.valueOf(friendIdList.get(i));
			if (id == playerId) {
				continue;
			}
			if (myFriendModule.isFriend(id)) {
				continue;
			}
			// 已经申请过了
			if (myFriendModule.getMyApplications().contains(id)) {
				continue;
			}
			if (PlayerManager.getInstance().isOnline(id)) {
				String serverId = PlayerManager.getInstance().getServerId(id);
				FriendApplyPush_30000022 build = FriendApplyPush_30000022
						.newBuilder()
						.setApplyPlayerId(playerId)
						.setPlayerId(id)
						.setApplyPlayerServer(ServerContext.getInstance().getServerId())
						.build();
				VxHolder.requestRemoteServer(serverId, build);
				continue;
			}
//			FriendHelper.receiveApplication(id, playerId, ServerContext.getInstance().getServerId());
		
			// 如果在我的黑名单中，则先从黑名单中删除
			if (myFriendModule.isBlack(id)) {
				myFriendModule.delete(id);
			}
			myFriendModule.addMyApplication(id);

			FriendApplication friendApplication = FriendApplication.valueOf(id, playerId, "");
			DAO.insert(friendApplication);
		}
		client.sendProtocol(resp);

	}
	protected void remoteApply(NetClient client, Object message) {
		FriendApplyPush_30000022 request = (FriendApplyPush_30000022) message;
		long playerId = request.getPlayerId();
		long applyPlayerId = request.getApplyPlayerId();
		String applyPlayerServer = request.getApplyPlayerServer();

		FriendHelper.receiveApplication(playerId, applyPlayerId, applyPlayerServer);
	}
	protected void remoteAdd(NetClient client, Object message) {
		FriendAddPush_30000023 request = (FriendAddPush_30000023) message;
		long playerId = request.getPlayerId();
		long friendId = request.getFriendId();
		String friendServer = request.getFriendServer();
		boolean ret = FriendHelper.addFriend(playerId, friendId, friendServer, Friend.FRIEND);
		if (ret) {
			FriendHelper.removeMyApplication(playerId, friendId);
			FriendHelper.removeApplication(playerId, friendId);
		}
	}

	protected void application(NetClient client, Object message) {

		FriendApplicationRequest_30000007 request = (FriendApplicationRequest_30000007) message;
		FriendApplicationResponse_30000008.Builder builder = FriendApplicationResponse_30000008.newBuilder();

		List<String> friendIdList = request.getPlayerIdsList();
		boolean agree = request.getAgree();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		FriendModule friendModule = player.getModule(FriendModule.class);

		for (int i = 0; i < friendIdList.size(); i++) {
			String idString = friendIdList.get(i);
			long id = Long.valueOf(idString);
			boolean flag = friendModule.applicationDeal(id, agree);
			if (flag) {
				builder.addFriendIds(idString);
			}
		}
		client.sendProtocol(builder.build());

	}

	protected void black(NetClient client, Object message) {

		FriendBlackRequest_30000010 request = (FriendBlackRequest_30000010) message;
		FriendBlackResponse_30000011.Builder resp = FriendBlackResponse_30000011.newBuilder();

		String idStirng = request.getId();
		long friendId = Long.parseLong(idStirng);
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		FriendModule friendModule = player.getModule(FriendModule.class);
		if (!friendModule.isFriend(friendId)) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}

		Friend friend = friendModule.getFriend(friendId);
		// 不在好友里，加入到好友，设置黑名单，否则直接设置黑名单关系
		if (friend != null) {
			friend.setRelation(Friend.BLACK);
			DAO.update(friend);
		} else {
			friendModule.addFriend(friendId, "", Friend.BLACK);
		}

		// 从对方好友列表里删除
//		if (!GameServer.getInstance().isLocalServer(friend.getServerId())) {
//			FriendDelPush_30000024 build = FriendDelPush_30000024.newBuilder().setPlayerId(friend.getFriendId()).setFriendId(playerId)
//					.build();
//			GameClientManager.getInstance().sendToGameServer(friend.getServerId(), build);
//		} else {
//			FriendHelper.deleteFriend(friendId, playerId);
//		}
		if (PlayerManager.getInstance().isOnline(friendId)) {
			String serverId = PlayerManager.getInstance().getServerId(friendId);
			FriendDelPush_30000024 build = FriendDelPush_30000024.newBuilder().setPlayerId(friend.getFriendId()).setFriendId(playerId)
					.build();
			VxHolder.requestRemoteServer(serverId, build);
		} else {
			FriendHelper.deleteFriend(friendId, playerId);
		}

		client.sendProtocol(resp.build());
	}

	protected void gift(NetClient client, Object message) {

		FriendGiftRequest_30000012 request = (FriendGiftRequest_30000012) message;
		FriendGiftResponse_30000013 resp = FriendGiftResponse_30000013.getDefaultInstance();
		ProtocolStringList friendIdList = request.getFriendIdList();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		FriendModule friendModule = player.getModule(FriendModule.class);

		if (friendModule.getSendGiftCount() + friendIdList.size() > GlobalConst.FriendValueGive) {
			client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
			return;
		}
		friendModule.setSendGiftCount(friendModule.getSendGiftCount() + friendIdList.size());

		for (String string : friendIdList) {
			long friendId = Long.parseLong(string);
			Friend friend = friendModule.getFriend(friendId);
			if (!friendModule.isFriend(friendId) || friend.getGift()) {
				continue;
			}
			if (PlayerManager.getInstance().isOnline(friendId)) {
				String serverId = PlayerManager.getInstance().getServerId(friendId);
				FriendGiftPush_30000028 build = FriendGiftPush_30000028.newBuilder().setRecvPlayerId(friend.getFriendId()).setSendPlayerId(playerId).build();
				VxHolder.requestRemoteServer(serverId, build);
			} else {
				Friend friendTarget = new Friend();
				friendTarget.setFriendId(playerId);
				friendTarget.setPlayerId(friendId);
				friendTarget.setGifted(true);

				DAO.updateSelective(friendTarget);
			}

			friend.setGift(true);
			DAO.update(friend);
		}

		client.sendProtocol(FriendGiftResponse_30000013.getDefaultInstance());
	}

	protected void giftReceive(NetClient client, Object message) {
		FriendGiftReceiveRequest_30000014 request = (FriendGiftReceiveRequest_30000014) message;
		FriendGiftReceiveResponse_30000015.Builder resp = FriendGiftReceiveResponse_30000015.newBuilder();
		ProtocolStringList friendIdList = request.getFriendIdList();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		FriendModule friendModule = player.getModule(FriendModule.class);
		if (friendModule.getGiftReceiveCount() + friendIdList.size() > GlobalConst.FriendValueReceive) {
			client.sendProtocol(resp, ErrorMsgEnum.times_limit.getId());
			return;
		}
		friendModule.setGiftReceiveCount(friendModule.getGiftReceiveCount() + friendIdList.size());

		int addCount = 0;
		for (String idString : friendIdList) {
			long id = Long.parseLong(idString);
			Friend friend = friendModule.getFriend(id);
			if (friend == null) {
				continue;
			}
			if (friend.getReceive()) {
				continue;
			}
			if (!friend.getGifted()) {
				continue;
			}
			friend.setReceive(true);
			DAO.update(friend);
			addCount += GlobalConst.FriendValueEverytime;
		}
		List<RewardInfo> resources = PlayerHelper.addResources(player, Asset.FriendshipValue.ID, addCount, OpType.Friend);
		resp.addAllRewards(resources);
		client.sendProtocol(resp.build());
	}

	protected void delete(NetClient client, Object message) {

		FriendDeleteRequest_30000009 request = (FriendDeleteRequest_30000009) message;
		long friendId = Long.valueOf(request.getId());
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		FriendModule friendModule = player.getModule(FriendModule.class);
		Friend friend = friendModule.getFriend(friendId);
		if (friend == null) {
			client.sendProtocol(FriendDeleteResponse_3000000a.getDefaultInstance(), ErrorMsgEnum.player_check_error.getId());
			return;
		}
		if (PlayerManager.getInstance().isOnline(friendId)) {
			String serverId = PlayerManager.getInstance().getServerId(friendId);
			FriendDelPush_30000024 build = FriendDelPush_30000024.newBuilder().setPlayerId(friend.getFriendId()).setFriendId(playerId).build();
			VxHolder.requestRemoteServer(serverId, build);
		} else {
			FriendHelper.deleteFriend(friendId, playerId);
		}
		FriendHelper.deleteFriend(playerId, friendId);

		client.sendProtocol(FriendDeleteResponse_3000000a.getDefaultInstance());
	}
	protected void remoteDelete(NetClient client, Object message) {

		FriendDelPush_30000024 request = (FriendDelPush_30000024) message;
		long playerId = request.getPlayerId();
		long friendId = request.getFriendId();

		FriendHelper.deleteFriend(playerId, friendId);

	}

	protected void remoteGift(NetClient client, Object message) {

		FriendGiftPush_30000028 request = (FriendGiftPush_30000028) message;
		long sendPlayerId = request.getSendPlayerId();
		long recvPlayerId = request.getRecvPlayerId();

		Player player = PlayerManager.getInstance().getPlayer(recvPlayerId);

		FriendModule friendModule = player.getModule(FriendModule.class);
		Friend friend = friendModule.getFriend(sendPlayerId);
		if (friend != null) {
			friend.setGifted(true);
			DAO.update(friend);
		}
	}

}
