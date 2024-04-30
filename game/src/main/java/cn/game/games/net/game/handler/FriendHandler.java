package cn.game.games.net.game.handler;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.protobuf.ProtocolStringList;

import cn.game.core.base.ServerContext;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.task.TaskManager;
import cn.game.games.cache.entity.Friend;
import cn.game.games.cache.entity.FriendApplication;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.op.impl.FriendOp;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.helper.FriendHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.manual.OldErrorMsgEnum;
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.FriendMsg.FriendAddPush_30000023;
import cn.game.protocol.protobuf.FriendMsg.FriendApplicationRequest_30000007;
import cn.game.protocol.protobuf.FriendMsg.FriendApplicationResponse_30000008;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyListResponse_30000054;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyPush_30000022;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyRequest_30000005;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyResponse_30000006;
import cn.game.protocol.protobuf.FriendMsg.FriendAttentionRequest_30000030;
import cn.game.protocol.protobuf.FriendMsg.FriendAttentionResponse_30000031;
import cn.game.protocol.protobuf.FriendMsg.FriendBlackListResponse_30000052;
import cn.game.protocol.protobuf.FriendMsg.FriendBlackRequest_30000010;
import cn.game.protocol.protobuf.FriendMsg.FriendBlackResponse_30000011;
import cn.game.protocol.protobuf.FriendMsg.FriendDelPush_30000024;
import cn.game.protocol.protobuf.FriendMsg.FriendDeleteRequest_30000009;
import cn.game.protocol.protobuf.FriendMsg.FriendDeleteResponse_3000000a;
import cn.game.protocol.protobuf.FriendMsg.FriendGiftRequest_30000016;
import cn.game.protocol.protobuf.FriendMsg.FriendGiftResponse_30000017;
import cn.game.protocol.protobuf.FriendMsg.FriendInfo;
import cn.game.protocol.protobuf.FriendMsg.FriendListRequest_30000001;
import cn.game.protocol.protobuf.FriendMsg.FriendListResponse_30000002;
import cn.game.protocol.protobuf.FriendMsg.FriendRecommendRequest_30000003;
import cn.game.protocol.protobuf.FriendMsg.FriendRecommendResponse_30000004;
import cn.game.protocol.protobuf.FriendMsg.FriendSearchRequest_30000020;
import cn.game.protocol.protobuf.FriendMsg.FriendSearchResponse_30000021;
import cn.game.protocol.protobuf.FriendMsg.FriendshipReceiveRequest_30000014;
import cn.game.protocol.protobuf.FriendMsg.FriendshipReceiveResponse_30000015;
import cn.game.protocol.protobuf.FriendMsg.FriendshipRequest_30000012;
import cn.game.protocol.protobuf.FriendMsg.FriendshipResponse_30000013;
import cn.game.protocol.protobuf.PlayerMsg.PlayerBriefInfoOtherRequest_01000009;

@Component
public class FriendHandler extends BaseHandler {

	private static final Logger log = LoggerFactory.getLogger(FriendHandler.class);

	@Override
	protected int getModule() {
		return 0x30;
	}

	@Override
	protected void inititialize() {

//		putInvoker(PbProtocol.FriendListRequest_30000001, this::list);
//		putInvoker(PbProtocol.FriendBlackListRequest_30000051, this::blackList);
//		putInvoker(PbProtocol.FriendApplyListRequest_30000053, this::applyList);
//		putInvoker(PbProtocol.FriendRecommendRequest_30000003, this::recommend);
//		putInvoker(PbProtocol.FriendSearchRequest_30000020, this::search);
//		putInvoker(PbProtocol.FriendApplyRequest_30000005, this::apply);
//		putInvoker(PbProtocol.FriendApplicationRequest_30000007, this::application);
//		putInvoker(PbProtocol.FriendDeleteRequest_30000009, this::delete);
//		putInvoker(PbProtocol.FriendBlackRequest_30000010, this::black);
//		putInvoker(PbProtocol.FriendshipRequest_30000012, this::friendship);
//		putInvoker(PbProtocol.FriendshipReceiveRequest_30000014, this::friendshipReceive);
//		putInvoker(PbProtocol.FriendGiftRequest_30000016, this::gift);
//		putInvoker(PbProtocol.FriendAttentionRequest_30000030, this::attention);
//		putInvoker(PbProtocol.FriendApplyPush_30000022, this::remoteApply);
//		putInvoker(PbProtocol.FriendAddPush_30000023, this::remoteAdd);
//		putInvoker(PbProtocol.FriendDelPush_30000024, this::remoteDelete);

	}

	protected void list(NetClient client, Object message) {

		FriendListRequest_30000001 req = (FriendListRequest_30000001) message;
		boolean local = req.getLocal();

		FriendListResponse_30000002.Builder response = FriendListResponse_30000002.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		FriendOp friendOp = player.getModule(FriendOp.class);
		Collection<Friend> allFriends = friendOp.getAllFriends();
		TaskManager.getInstance().addWorkerTask(() -> {

			try {
				for (Friend friend : allFriends) {
					if (local && !GameServer.getInstance().isLocalServer(friend.getServerId())) {
						continue;
					}
					if (!local && GameServer.getInstance().isLocalServer(friend.getServerId())) {
						continue;
					}

					FriendInfo friendBaseInfo = PbBuilder.buildFriendInfo(friend);
					if (friendOp.isFriend(friend)) {
						response.addFriends(friendBaseInfo);
					}
				}
			} catch (Exception e) {
				log.error("", e);
			}
			client.sendProtocol(response);
		});

	}
	protected void blackList(NetClient client, Object message) {

		FriendBlackListResponse_30000052.Builder resp = FriendBlackListResponse_30000052.newBuilder();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		FriendOp friendOp = player.getModule(FriendOp.class);
		Collection<Friend> allFriends = friendOp.getAllFriends();

		TaskManager.getInstance().addWorkerTask(() -> {

			try {
				for (Friend friend : allFriends) {

					FriendInfo friendBaseInfo = PbBuilder.buildFriendInfo(friend);
					if (friend.getRelation() == Friend.BLACK) {
						resp.addPlayers(friendBaseInfo);
					}
				}
			} catch (Exception e) {
				log.error("", e);
			}
			client.sendProtocol(resp.build());
		});

	}
	protected void applyList(NetClient client, Object message) {

		FriendApplyListResponse_30000054.Builder resp = FriendApplyListResponse_30000054.newBuilder();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		FriendOp friendOp = player.getModule(FriendOp.class);
		Collection<Friend> allFriends = friendOp.getAllFriends();

		TaskManager.getInstance().addWorkerTask(() -> {

			try {
				for (FriendApplication application : friendOp.getAllApplications().values()) {

					SimplePlayerInfo simplePlayerInfo = PbBuilder.buildSimplePlayerInfo(application.getApplyPlayerId(),
							application
							.getApplyPlayerServer());
					resp.addPlayers(simplePlayerInfo);
				}
			} catch (Exception e) {
				log.error("", e);
			}
			client.sendProtocol(resp.build());
		});

	}

	protected void recommend(NetClient client, Object message) {
		FriendRecommendRequest_30000003 req = (FriendRecommendRequest_30000003) message;
		boolean refresh = req.getRefresh();

		int maxTime = 5000;

		FriendRecommendResponse_30000004.Builder builder = FriendRecommendResponse_30000004.newBuilder();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		FriendOp friendOp = player.getModule(FriendOp.class);
		long lastRefreshTime = friendOp.getLastRefreshTime();
		if (lastRefreshTime > 0 && (System.currentTimeMillis() - lastRefreshTime) < maxTime) {
			List<SimplePlayer> lastRefreshPlayers = friendOp.getLastRefreshPlayers();
			builder.addAllFriends(PbBuilder.buildSimplePlayerInfos(lastRefreshPlayers));
			if (refresh) {
				builder.setNextFreshTime((int) ((lastRefreshTime + maxTime) / 1000));
			}
			client.sendProtocol(builder.build());
			return;
		}

		TaskManager.getInstance().addWorkerTask(() -> {
			List<SimplePlayer> players = PlayerManager.getInstance().searchPlayers(client.getPlayerId());
			for (SimplePlayer p : players) {

				builder.addFriends(PbBuilder.buildSimplePlayerInfo(p));
			}
			client.sendProtocol(builder.build());
		});

	}

	protected void search(NetClient client, Object message) {
		FriendSearchRequest_30000020 request = (FriendSearchRequest_30000020) message;

		String server = request.getServerId();
		long targetId = Long.parseLong(request.getPlayerId());

		TaskManager.getInstance().addWorkerTask(() -> {
			FriendSearchResponse_30000021.Builder response = FriendSearchResponse_30000021.newBuilder();
			long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);
			SimplePlayer simplePlayer = null ; 
			try {
				if (GameServer.getInstance().isLocalServer(server)) {
					if (FriendHelper.isBlack(playerId, targetId) || FriendHelper.isBlack(targetId, playerId)) {
						client.sendProtocol(PlayerBriefInfoOtherRequest_01000009.getDefaultInstance());
						return;
					}
					simplePlayer = PlayerManager.getInstance().getAndLoadSimplePlayer(targetId); 
				} else {
					simplePlayer = GameServer.getInstance().getCrossGameServerInterfaceSync().searchFriendPlayer(targetId, playerId,
							server);

				}
			} catch (Exception e) {
				log.error("", e);
			}
			int error = 0;
			if (simplePlayer == null) {
				error = OldErrorMsgEnum.player_not_found.getId();
			}
			response.setPlayer(PbBuilder.buildSimplePlayerInfo(simplePlayer));
			client.sendProtocol(response.build(), error);
		});

		FriendSearchResponse_30000021.Builder resp = FriendSearchResponse_30000021.newBuilder();

	}
	protected void apply(NetClient client, Object message) {
		FriendApplyRequest_30000005 request = (FriendApplyRequest_30000005) message;
		FriendApplyResponse_30000006.Builder resp = FriendApplyResponse_30000006.newBuilder();

		long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);
		List<String> friendIdList = request.getFriendIdsList();
		ProtocolStringList serverIdsList = request.getServerIdsList();
		for (int i = 0; i < friendIdList.size(); i++) {
			Long id = Long.valueOf(friendIdList.get(i));
			String serverId = serverIdsList.get(i);
			if (id == playerId) {
				continue;
			}
			if (!GameServer.getInstance().isLocalServer(serverId)) {

				FriendApplyPush_30000022 build = FriendApplyPush_30000022.newBuilder().setApplyPlayerId(playerId).setPlayerId(id)
						.setApplyPlayerServer(ServerContext.getInstance().getServerId()).build();
				GameClientManager.getInstance().sendToGameServer(serverId, build);
				continue;
			}

			// 如果在我的黑名单中，则先从黑名单中删除
			FriendOp myFriendOp = player.getModule(FriendOp.class);
			if (myFriendOp.isBlack(id)) {
				myFriendOp.delete(id);
			}

			FriendHelper.receiveApplication(id, playerId, ServerContext.getInstance().getServerId());

			myFriendOp.addMyApplication(id);

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

		TaskManager.getInstance().addWorkerTask(() -> {
			boolean ret = FriendHelper.addFriend(playerId, friendId, friendServer, Friend.FRIEND);

			if (ret) {
				FriendHelper.removeMyApplication(playerId, friendId);
				FriendHelper.removeApplication(playerId, friendId);
			}
		});
	}

	protected void application(NetClient client, Object message) {

		FriendApplicationRequest_30000007 request = (FriendApplicationRequest_30000007) message;
		FriendApplicationResponse_30000008.Builder builder = FriendApplicationResponse_30000008.newBuilder();

		List<String> friendIdList = request.getFriendIdList();
		boolean agree = request.getAgree();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		FriendOp friendOp = player.getModule(FriendOp.class);

		TaskManager.getInstance().addWorkerTask(() -> {

			for (int i = 0; i < friendIdList.size(); i++) {
				String idString = friendIdList.get(i);
				long id = Long.valueOf(idString);
				boolean flag = friendOp.applicationDeal(id, agree);
				if (flag) {
					builder.addFriendIds(idString);
				}
			}

			client.sendProtocol(builder.build());
		});

	}

	protected void black(NetClient client, Object message) {

		FriendBlackRequest_30000010 request = (FriendBlackRequest_30000010) message;
		FriendBlackResponse_30000011.Builder resp = FriendBlackResponse_30000011.newBuilder();

		String idStirng = request.getId();
		long friendId = Long.parseLong(idStirng);
		long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);
		String serverId = request.getServerId(); 

		FriendOp friendOp = player.getModule(FriendOp.class);
//		if (!friendOp.isFriend(friendId)) {
//			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
//			return;
//		}

		Friend friend = friendOp.getFriend(friendId);
		// 不在好友里，加入到好友，设置黑名单，否则直接设置黑名单关系
		if (friend != null) {
			friend.setRelation(Friend.BLACK);
			FriendHelper.update(friend);
		} else {
			friendOp.addFriend(friendId, serverId, Friend.BLACK);
		}

		// 从对方好友列表里删除
		if (!GameServer.getInstance().isLocalServer(friend.getServerId())) {
			FriendDelPush_30000024 build = FriendDelPush_30000024.newBuilder().setPlayerId(friend.getFriendId()).setFriendId(playerId)
					.build();
			GameClientManager.getInstance().sendToGameServer(friend.getServerId(), build);
		} else {
			FriendHelper.deleteFriend(friendId, playerId);
		}
		client.sendProtocol(resp.build());
	}

	protected void friendship(NetClient client, Object message) {

		FriendshipRequest_30000012 request = (FriendshipRequest_30000012) message;
		ProtocolStringList friendIdList = request.getFriendIdList();
		long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);
		FriendOp friendOp = player.getModule(FriendOp.class);
		for (String string : friendIdList) {
			long friendId = Long.parseLong(string);
			Friend friend = friendOp.getFriend(friendId);
			if (!friendOp.isFriend(friend) || friend.getGift()) {
				continue;
			}
			Friend friendTarget;
			if (PlayerManager.getInstance().hasCache(friendId)) {
				FriendOp targetFriendOp = player.getModule(FriendOp.class);
				friendTarget = targetFriendOp.getFriend(playerId);
				if (friendTarget != null) {
					friendTarget.setGifted(true);
					FriendHelper.update(friendTarget);
				}
			} else {
				friendTarget = new Friend();
				friendTarget.setFriendId(playerId);
				friendTarget.setPlayerId(friendId);
				friendTarget.setGifted(true);
				
				FriendHelper.update(friendTarget);
//				DAO.execute(FriendMapper.class,
//						MapperConstant.updateByPrimaryKeySelective, friendTarget);
			}
			friend.setGift(true);
			FriendHelper.update(friend);

		}

		client.sendProtocol(FriendshipResponse_30000013.getDefaultInstance());

	}

	protected void friendshipReceive(NetClient client, Object message) {
		FriendshipReceiveRequest_30000014 request = (FriendshipReceiveRequest_30000014) message;
		long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);
		for (String idString : request.getFriendIdList()) {
			long id = Long.parseLong(idString);
			FriendOp friendOp = player.getModule(FriendOp.class);

			Friend friend = friendOp.getFriend(id);
			if (friend == null) {
				continue;
			}
			if (friend.getReceive()) {
				continue;
			}
			friend.setReceive(true);
			FriendHelper.update(friend);

		}

		client.sendProtocol(FriendshipReceiveResponse_30000015.getDefaultInstance());

	}

	protected void gift(NetClient client, Object message) {
		FriendGiftRequest_30000016 request = (FriendGiftRequest_30000016) message;

		client.sendProtocol(FriendGiftResponse_30000017.getDefaultInstance());
	}

	protected void delete(NetClient client, Object message) {

		FriendDeleteRequest_30000009 request = (FriendDeleteRequest_30000009) message;
		long friendId = Long.valueOf(request.getId());
		long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);
		FriendOp friendOp = player.getModule(FriendOp.class);
		Friend friend = friendOp.getFriend(friendId);
		if (friend == null) {
			client.sendProtocol(FriendDeleteResponse_3000000a.getDefaultInstance());
			return;
		}
		if (!GameServer.getInstance().isLocalServer(friend.getServerId())) {
			FriendDelPush_30000024 build = FriendDelPush_30000024.newBuilder().setPlayerId(friend.getFriendId()).setFriendId(playerId)
					.build();
			GameClientManager.getInstance().sendToGameServer(friend.getServerId(), build);
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
	protected void attention(NetClient client, Object message) {

		FriendAttentionRequest_30000030 req = (FriendAttentionRequest_30000030) message;
		FriendAttentionResponse_30000031 resp = FriendAttentionResponse_30000031.getDefaultInstance();

		long friendId = Long.parseLong(req.getId());
		boolean attention = req.getAttention();

		long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);

		FriendOp friendOp = player.getModule(FriendOp.class);
		if (!friendOp.isFriend(friendId)) {

			client.sendProtocol(resp, OldErrorMsgEnum.player_check_error.getId());
			return;
		}
		Friend friend = friendOp.getFriend(friendId);
		if (attention) {
			friend.setRelation(Friend.ATTENTION);
		} else {
			friend.setRelation(Friend.FRIEND);
		}
		FriendHelper.update(friend);
		client.sendProtocol(resp);

	}
	


}
