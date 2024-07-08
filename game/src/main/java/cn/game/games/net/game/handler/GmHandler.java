package cn.game.games.net.game.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.task.TaskManager;
import cn.game.games.cache.entity.ForbidAccount;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.protobuf.BaseMsg.GoodsInfo;
import cn.game.protocol.protobuf.GmMsg.GmAccountForbidListResponse_77000004;
import cn.game.protocol.protobuf.GmMsg.GmAccountForbidRequest_77000005;
import cn.game.protocol.protobuf.GmMsg.GmAccountForbidResponse_77000006;
import cn.game.protocol.protobuf.GmMsg.GmAccountUnblockRequest_77000007;
import cn.game.protocol.protobuf.GmMsg.GmAccountUnblockResponse_77000008;
import cn.game.protocol.protobuf.GmMsg.GmPlayerLogoutRequest_77000009;
import cn.game.protocol.protobuf.GmMsg.GmPlayerLogouttResponse_7700000a;
import cn.game.protocol.protobuf.GmMsg.GmPlayerMailRequest_77000010;
import cn.game.protocol.protobuf.GmMsg.GmPlayerMailResponse_77000011;
import cn.game.protocol.protobuf.GmMsg.GmPlayerRequest_77000021;
import cn.game.protocol.protobuf.GmMsg.GmPlayerResponse_77000022;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ServerMsg.GameGmPlayerInfoRequest_7d000050;
import cn.game.protocol.protobuf.ServerMsg.GameGmPlayerInfoResponse_7d000051;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;

/**
 * gm处理器
 */
@Component
public class GmHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x77;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.GmShutdownServerRequest_77000001, this::shutdown);
		putInvoker(PbProtocol.GmAccountForbidListRequest_77000003, this::forbidAccountList);
		putInvoker(PbProtocol.GmAccountForbidRequest_77000005, this::forbidAccount);
		putInvoker(PbProtocol.GmAccountUnblockRequest_77000007, this::unblockAccount);
		putInvoker(PbProtocol.GmPlayerLogoutRequest_77000009, this::playerLogout);
		putInvoker(PbProtocol.GmPlayerMailRequest_77000010, this::mail);
		putInvoker(PbProtocol.GmPlayerRequest_77000021, this::playerInfo);

	}

	protected void playerInfo(NetClient client, Object message) {
		GmPlayerRequest_77000021 request = (GmPlayerRequest_77000021) message;
		GmPlayerResponse_77000022.Builder response = GmPlayerResponse_77000022.newBuilder();
		String channel = request.getChannel();
		long playerId = StringUtils.isEmpty(request.getPlayerId()) ? 0 : Long.parseLong(request.getPlayerId());
		String name = request.getName();
		
		if (PlayerManager.getInstance().isOnline(playerId)) {
			Future<Message<GameGmPlayerInfoResponse_7d000051>> respMessage = VxHolder.requestRemoteServer(PlayerManager.getInstance().getServerId(playerId),
					GameGmPlayerInfoRequest_7d000050.newBuilder().setPlayerId(playerId).build());
			respMessage.onComplete(r -> {
				response.setPlayer(r.result().body().getPlayer());
				client.sendProtocol(response);
			});
		} else {
			// 从本服务器载入玩家数据
			client.sendProtocol(response);
		}
	}
	protected void mail(NetClient client, Object message) {
		GmPlayerMailRequest_77000010 request = (GmPlayerMailRequest_77000010) message;
		long playerId = request.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		int id = request.getId();
		String title = request.getTitle();
		String content = request.getContent();
		List<GoodsInfo> attachmentsList = request.getAttachmentsList();
		List<Goods> list = new ArrayList<>();
		for (GoodsInfo goods : attachmentsList) {
			list.add(new Goods(goods.getId(), goods.getCount()));
		}
		MailHelper.sendMail(playerId, id, "", title, content, MailHelper.GM, list);
		client.sendProtocol(GmPlayerMailResponse_77000011.getDefaultInstance());
	}
	private void shutdown(NetClient client, Object message) {
		CompletableFuture.runAsync(()->
		{
			System.exit(0);
		});
	}

	/** 封号列表 */
	private void forbidAccountList(NetClient client, Object message) {
		GmAccountForbidListResponse_77000004.Builder response = GmAccountForbidListResponse_77000004.newBuilder();
		List<ForbidAccount> accounts = PlayerManager.getInstance().getForbidAccount();

		response.addAllAccounts(PbBuilder.buildForbidAccount(accounts));
		client.sendProtocol(response);
	}

	/** 封号 */
	private void forbidAccount(NetClient client, Object message) {
		GmAccountForbidRequest_77000005 request = (GmAccountForbidRequest_77000005) message;
		GmAccountForbidResponse_77000006.Builder response = GmAccountForbidResponse_77000006.newBuilder();
//		long playerId = Long.parseLong(request.getPlayerId());
		String reason = request.getReason();
//		String unblockTime = request.getUnblockTime();

		TaskManager.getInstance().addWorkerTask(() -> {
//			int errorCode = PlayerManager.getInstance().forbidAccount(playerId, reason, unblockTime);
//			client.sendProtocol(response, errorCode);
		});
	}

	/** 解封账号 */
	private void unblockAccount(NetClient client, Object message) {
		GmAccountUnblockRequest_77000007 request = (GmAccountUnblockRequest_77000007) message;
		GmAccountUnblockResponse_77000008.Builder response = GmAccountUnblockResponse_77000008.newBuilder();
//		long playerId = Long.parseLong(request.getPlayerId());

//		int errorCode = PlayerManager.getInstance().unblockAccount(playerId);
//		client.sendProtocol(response, errorCode);
	}
	
	/** 踢玩家下线 */
	private void playerLogout(NetClient client, Object message) {
		GmPlayerLogoutRequest_77000009 request = (GmPlayerLogoutRequest_77000009) message;
		GmPlayerLogouttResponse_7700000a.Builder response = GmPlayerLogouttResponse_7700000a.newBuilder();
		
		long playerId = StringUtils.isEmpty(request.getPlayerId()) ? 0 : Long.parseLong(request.getPlayerId());
		PlayerHelper.addTask(playerId, r -> {
			GameClientManager.getInstance().logout(playerId);
			client.sendProtocol(response);
		});

	}

}
