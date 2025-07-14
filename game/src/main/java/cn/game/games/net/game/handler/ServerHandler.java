package cn.game.games.net.game.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolStringList;

import cn.game.core.base.ServerContext;
import cn.game.core.net.client.LogoutType;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.bytes.ByteArrayProtocol;
import cn.game.core.net.protocol.object.ByteStringProtocol;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.task.TaskManager;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.GameServerStatus;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.data.remote.DataGameServerInterface;
import cn.game.games.net.game.db.DbTask;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.games.net.game.module.player.IdConstant;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.protocol.generated.config.QuestionnaireConfig;
import cn.game.protocol.generated.manager.QuestionnaireManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ChatMsg;
import cn.game.protocol.protobuf.GmMsg.GmPlayerInfo;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.protocol.protobuf.ServerMsg.CrossGameForwardPush_7d000003;
import cn.game.protocol.protobuf.ServerMsg.DbTaskProto;
import cn.game.protocol.protobuf.ServerMsg.GameCrossBroadcast_7d000008;
import cn.game.protocol.protobuf.ServerMsg.GameCrossForwardPush_7d000002;
import cn.game.protocol.protobuf.ServerMsg.GameCrossPlayerBroadcast_7d000005;
import cn.game.protocol.protobuf.ServerMsg.GameDataPushBatch2_7d00000c;
import cn.game.protocol.protobuf.ServerMsg.GameDataPushBatch_7d00000b;
import cn.game.protocol.protobuf.ServerMsg.GameDataPush_7d00000a;
import cn.game.protocol.protobuf.ServerMsg.GameGmPlayerInfoRequest_7d000050;
import cn.game.protocol.protobuf.ServerMsg.GameGmPlayerInfoResponse_7d000051;
import cn.game.protocol.protobuf.ServerMsg.GameMessageForwardRequest_7d000200;
import cn.game.protocol.protobuf.ServerMsg.GameOpRequest_7d000373;
import cn.game.protocol.protobuf.ServerMsg.GameOpResponse_7d000374;
import cn.game.protocol.protobuf.ServerMsg.GamePlayerLogoutRequest_7d000101;
import cn.game.protocol.protobuf.ServerMsg.GamePlayerLogoutResponse_7d000102;
import cn.game.protocol.protobuf.ServerMsg.GamePlayerOnlinePush_7d000010;
import cn.game.protocol.protobuf.ServerMsg.GamePlayerPush_7d000100;
import cn.game.protocol.protobuf.ServerMsg.GamePlayerRequest_7d000015;
import cn.game.protocol.protobuf.ServerMsg.GamePlayerResponse_7d000016;
import cn.game.protocol.protobuf.ServerMsg.GameStatusChangeRequest_7d000030;
import cn.game.protocol.protobuf.ServerMsg.GameStatusChangeResponse_7d000031;
import cn.game.protocol.protobuf.ServerMsg.GameTestRequest_7d000500;
import cn.game.protocol.protobuf.ServerMsg.GameTestResponse_7d000501;
import cn.game.protocol.protobuf.ServerMsg.LoginGameQuestionnairePush_7d000090;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderShipRequest_7d000022;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderShipResponse_7d000023;
import cn.game.protocol.protobuf.ServerMsg.ServerStatusResponse_7d000902;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.Config;
import cn.game.util.KryoUtils;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;

/**
 * 服务器之间的消息处理器
 */
@Component
public class ServerHandler extends GameBaseHandler {

	@Override
	protected int getModule() {
		return 0x7d;
	}
	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.GameCrossForwardPush_7d000002, this::forward);
		putInvoker(PbProtocol.CrossGameForwardPush_7d000003, this::receive);
		putInvoker(PbProtocol.GameCrossPlayerBroadcast_7d000005, this::broadcastPlayers);
		putInvoker(PbProtocol.GameCrossBroadcast_7d000008, this::broadcast);
		putInvoker(PbProtocol.GamePlayerOnlinePush_7d000010, this::online);
		putInvoker(PbProtocol.GamePlayerPush_7d000100, this::playerPush);
		putInvoker(PbProtocol.ServerStatusRequest_7d000901, this::alive);
		putInvoker(PbProtocol.GameTestRequest_7d000500, this::test);
		putInvoker(PbProtocol.GameDataPush_7d00000a, this::db);
		putInvoker(PbProtocol.GameDataPushBatch_7d00000b, this::dbBatch);
		putInvoker(PbProtocol.GameDataPushBatch2_7d00000c, this::dbBatch2);
		putInvoker(PbProtocol.GamePlayerLogoutRequest_7d000101, this::playerLogout);
		putInvoker(PbProtocol.GamePlayerRequest_7d000015, this::playerRequest);
		putInvoker(PbProtocol.PaymentOrderShipRequest_7d000022, this::ship);
		putInvoker(PbProtocol.GamePlayerPush_7d000011, this::playerPush);
		putInvoker(PbProtocol.GameGmPlayerInfoRequest_7d000050, this::gmPlayer);
		putInvoker(PbProtocol.NotifyGmAddForbidAccountRequest_7d000054, this::gmAddForbidAccount);
		putInvoker(PbProtocol.NotifyGmDelForbidAccountRequest_7d000056, this::gmDelForbidAccount);
		putInvoker(PbProtocol.NotifyRefreshGlobalGmMailRequest_7d000058, this::refreshGlobalGmMail);
		putInvoker(PbProtocol.NotifyAddGlobalGmMailRequest_7d000060, this::ddGlobalGmMail);
		putInvoker(PbProtocol.NotifyDelGlobalGmMailRequest_7d000062, this::delGlobalGmMail);

		putInvoker(PbProtocol.GameMessageForwardRequest_7d000200, this::messageForward);
		putInvoker(PbProtocol.LoginUpdateIOSAccessTokenRequest_7d000074, this::updateIOSAccessToken);
		putInvoker(PbProtocol.ServerObjectTestRequest_7d000033, this::objectMessageTest);
		putInvoker(PbProtocol.LoginGameQuestionnairePush_7d000090, this::questionnairePush);
		putInvoker(PbProtocol.GameStatusChangeRequest_7d000030, this::gameStatusChange);
		putInvoker(PbProtocol.GameOpRequest_7d000373, this::gameOp);
		putInvoker(PbProtocol.GamePlayerEventPush_7d010100, this::playerEvent);


		putInvoker(PbProtocol.NotifyInviteBindAndLvUpRequest_7d000041, this::InviteLvChange);
		putInvoker(PbProtocol.NotifyZongMenMsgToGame_7d000047, this::zongMenMsgNotify);



//		putInvoker(PbProtocol.LoginGameArchiveListRequest_7d000301, this::archiveList);
//		putInvoker(PbProtocol.LoginGameArchiveCreateRequest_7d000303, this::archiveCreate);
	}

	private void zongMenMsgNotify(NetClient client, Object o) {
		ServerMsg.NotifyZongMenMsgToGame_7d000047 req = (ServerMsg.NotifyZongMenMsgToGame_7d000047) o;
		int msgId = req.getMsgId();
		Message message = PbProtocol.getInstance().parseFrom(msgId, req.getData());
		req.getPlayerIdList().forEach(pid ->{
			Player player = PlayerManager.getInstance().getPlayer(pid);
			if (player == null) {
				log.error("zongMenMsgNotify player is null");
				return;
			}
			switch (msgId){
				//玩家 退出 宗门
				case PbProtocol.notifyQuitZongMen_40000024 -> {
					player.getZongmenModule().kickZongMen((ZongMenMsg.notifyQuitZongMen_40000024) message);
				}
				//玩家 加入 宗门
				case PbProtocol.notifyJoinZongMen_40000044 -> player.getZongmenModule().joinZongMen((ZongMenMsg.notifyJoinZongMen_40000044) message);
				case PbProtocol.ChatMessagePush_31010001 -> {//宗门聊天
					zongMenChat(player,(ChatMsg.ChatMessagePush_31010001) message);
				}
				default -> {
					log.error(String.format("zongMenMsgNotify msgId:%d is error",req.getMsgId()));
				}
			}
		});
	}

	private void zongMenChat(Player notifyPlayer, ChatMsg.ChatMessagePush_31010001 req) {
		notifyPlayer.getGameClient().sendProtocol(req);
	}

	private void playerEvent(NetClient client, Object o) {
		PlayerEvent gameEvent = (PlayerEvent) o;
		long playerId = gameEvent.getSourceId();

	}
	private void InviteLvChange(NetClient client, Object o) {
		ServerMsg.NotifyInviteBindAndLvUpRequest_7d000041 req = (ServerMsg.NotifyInviteBindAndLvUpRequest_7d000041) o;
		Player player = PlayerManager.getInstance().getPlayer(req.getPid());
		if (player != null && player.isOnline()){
			player.getInviteModule().updateTargetLv(req.getTargetPid(),req.getLv());
			player.getInviteModule().checkRedHot();
		}
		log.info(String.format("InviteLvChange req:%s",req));
		client.sendProtocol(ServerMsg.NotifyInviteBindAndLvUpResponse_7d000042.newBuilder().setResult(true).build());

	}

	private void messageForward(NetClient client, Object o) {
		GameMessageForwardRequest_7d000200 req = (GameMessageForwardRequest_7d000200) o;
		Message from = PbProtocol.getInstance().parseFrom(req.getId(), req.getData());
		ProtobufProtocol protocol = new ProtobufProtocol(req.getId(), from);
		dispatch(client, protocol);
	}
	private void gameStatusChange(NetClient client, Object o) {
		GameStatusChangeRequest_7d000030 req = (GameStatusChangeRequest_7d000030) o;
		GameStatusChangeResponse_7d000031 resp = GameStatusChangeResponse_7d000031.getDefaultInstance();
		int status = req.getStatus();
		int err = 0;
		try {
			GameServerStatus.getInstance().updateServerStatus(ServerContext.getInstance().getServerId(), 0, status);
		} catch (Exception e) {
			log.error("", e);
			err = 1;
		}
		client.sendProtocol(resp, err);
	}

	private void gameOp(NetClient client, Object o) {
		GameOpRequest_7d000373 req = (GameOpRequest_7d000373) o;
		GameOpResponse_7d000374 resp = GameOpResponse_7d000374.getDefaultInstance();
		int opType = req.getOpType();
		if (opType == 1) {
			CompletableFuture.runAsync(() -> System.exit(0));
		} else if (opType == 2) {
			try {
				ProcessBuilder pb = new ProcessBuilder("/bin/sh", "/server/bin/restart_background.sh");
				pb.directory(new File("/server/bin/"));
				Process process = pb.start();
				// 等待脚本执行完成
				int exitCode = process.waitFor();
				log.info("restart.sh exitCode: " + exitCode);
				// 读取脚本的输出
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					log.info(line);
				}

			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void questionnairePush(NetClient client, Object o) {
		LoginGameQuestionnairePush_7d000090 req = (LoginGameQuestionnairePush_7d000090) o;
		long playerId = req.getPlayerId();
		int id = req.getType();
		log.info("questionnairePush playerId={}, id={}", playerId, id);
		QuestionnaireConfig questionnaireConfig = QuestionnaireManager.instance().get(id);

		Consumer<Player> consumer = player -> {
			Set<Integer> idsSet = player.getPlayerModule().getIdsSet(IdConstant.Questionnaire);
			if (idsSet.contains(id)) {
				return;
			}
			if (!PlayerHelper.checkCondition(player, questionnaireConfig.Condition)) {
				return;
			}
			idsSet.add(id);
			MailHelper.sendMail(playerId, questionnaireConfig.MailID, true);
		};
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player != null) {
			consumer.accept(player);
		} else {
			PlayerHelper.loadPlayerFromDb(playerId).onSuccess(offlinePlayer -> {
				if (offlinePlayer != null) {
					consumer.accept(offlinePlayer);
					PlayerHelper.saveClientCache(playerId).onComplete(r -> {
						PlayerManager.getInstance().deletePlayer(playerId);
					});
				}
			}).onFailure(err -> {
				log.error("", err);
			});
		}
	}
	private void objectMessageTest(NetClient client, Object o) {
		ItemModule itemModule = (ItemModule) o;
		log.info(itemModule.toString());
		log.info(itemModule.getId_items().toString());
		// 返回数据，针对服务器之间的消息通讯，不需要msgId
		client.sendProtocol(itemModule);
	}
	private void updateIOSAccessToken(NetClient client, Object o) {
		ServerMsg.LoginUpdateIOSAccessTokenRequest_7d000074 req = (ServerMsg.LoginUpdateIOSAccessTokenRequest_7d000074) o;
		Config.wechatAccessToken = req.getAccessToken();
		log.info(String.format("updateIOSAccessToken:%s", req.getAccessToken()));
	}

	private void delGlobalGmMail(NetClient client, Object o) {
		ServerMsg.NotifyDelGlobalGmMailRequest_7d000062 req = (ServerMsg.NotifyDelGlobalGmMailRequest_7d000062)o;
		MailHelper.removeGlobalMail(req.getDelGmMailId());
		log.info("delGlobalGmMail: ", req.getDelGmMailId());
		ServerMsg.NotifyDelGlobalGmMailResponse_7d000063.Builder res = ServerMsg.NotifyDelGlobalGmMailResponse_7d000063.newBuilder().setResult(true);
		client.sendProtocol(res.build());
	}

	private void ddGlobalGmMail(NetClient client, Object o) {
		ServerMsg.NotifyAddGlobalGmMailRequest_7d000060 req = (ServerMsg.NotifyAddGlobalGmMailRequest_7d000060)o;
		MailHelper.addGlobalMail(req.getAddGmMailId());
		log.info("ddGlobalGmMail: ", req.getAddGmMailId());
		ServerMsg.NotifyAddGlobalGmMailResponse_7d000061.Builder res = ServerMsg.NotifyAddGlobalGmMailResponse_7d000061.newBuilder().setResult(true);
		client.sendProtocol(res.build());
	}

	private void refreshGlobalGmMail(NetClient client, Object o) {
		ServerMsg.NotifyRefreshGlobalGmMailResponse_7d000059.Builder res = ServerMsg.NotifyRefreshGlobalGmMailResponse_7d000059.newBuilder().setResult(true);
		MailHelper.initLoadGlobalMail();
		log.info("refreshGlobalGmMail");
		client.sendProtocol(res.build());
	}

	private void gmDelForbidAccount(NetClient client, Object o) {
		ServerMsg.NotifyGmDelForbidAccountRequest_7d000056 req = (ServerMsg.NotifyGmDelForbidAccountRequest_7d000056)o;
		req.getPidsList().forEach(delPid ->{
			PlayerManager.getInstance().unblockAccount(delPid);
			log.info(String.format("gmDelForbidAccount pid=%d", delPid));
		});
		ServerMsg.NotifyGmDelForbidAccountResponse_7d000057.Builder res = ServerMsg.NotifyGmDelForbidAccountResponse_7d000057.newBuilder().setResult(true);
		client.sendProtocol(res.build());
	}

	private void gmAddForbidAccount(NetClient client, Object o) {
		ServerMsg.NotifyGmAddForbidAccountRequest_7d000054 req = (ServerMsg.NotifyGmAddForbidAccountRequest_7d000054)o;
		req.getPidsList().forEach(addPid ->{
					PlayerManager.getInstance().forbidAccount(addPid, req.getReason(), req.getTimer()+"",req.getType());
					log.info(String.format("gmAddForbidAccount pid=%d, reason=%s, timer=%s", addPid, req.getReason(), req.getTimer()));
		});
		ServerMsg.NotifyGmAddForbidAccountResponse_7d000055.Builder res = ServerMsg.NotifyGmAddForbidAccountResponse_7d000055.newBuilder().setResult(true);
		client.sendProtocol(res.build());
	}

	protected void gmPlayer(NetClient client, Object message) {
		GameGmPlayerInfoRequest_7d000050 request = (GameGmPlayerInfoRequest_7d000050) message;
		GameGmPlayerInfoResponse_7d000051.Builder resp = GameGmPlayerInfoResponse_7d000051.newBuilder();
		long playerId = request.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player == null) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.not_online.getId());
			return;
		}
		GmPlayerInfo gmProto = player.toGmProto();
		resp.setPlayer(gmProto);
		client.sendProtocol(resp.build());
	}
	protected void ship(NetClient client, Object message) {
		PaymentOrderShipRequest_7d000022 request = (PaymentOrderShipRequest_7d000022) message;
		PaymentOrderShipResponse_7d000023.Builder resp = PaymentOrderShipResponse_7d000023.newBuilder();
		long playerId = request.getPlayerId();
		long uid = request.getUid(); 
		log.info("PaymentOrder ship push, playerId={}, uid={}", playerId, uid);
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player != null && player.isIslogouting()) {
			log.error(String.format("充值失败:%b",player.isIslogouting()));
			resp.setSuccess(false);
			client.sendProtocol(resp.build());
			return;
		}
		// 离线玩家单独处理 调用 payItem.getPayType().offlinePay(player,payItem); 处理
		if (player == null) {
			PlayerHelper.loadPlayerFromDb(playerId).map(offlinePlayer -> {
				if (offlinePlayer != null){
					PayItem payItem = offlinePlayer.getPlayerModule().getPayItems(uid);
					if (payItem == null || payItem.isFinish()) {
						log.warn("PayItem offline ship fail : " + payItem);
						resp.setSuccess(false);
						client.sendProtocol(resp.build());
						return null;
					}
					payItem.getPayType().offlinePay(offlinePlayer,payItem);
					payItem.finish();
					offlinePlayer.handleEvent(EventTypeEnum.Charge, payItem.getRmb());
					GameLogger.recharge(offlinePlayer, payItem);
					// 支付后先实时保存数据到数据库
					PlayerHelper.saveClientCache(playerId).onSuccess(rr -> {
						resp.setSuccess(true);
						client.sendProtocol(resp.build());
					}).onFailure(t -> {
						resp.setSuccess(false);
						client.sendProtocol(resp.build());
					});
					PlayerManager.getInstance().deletePlayer(playerId);
				}
				return offlinePlayer;
			}).onFailure((err)->{
				log.error("PayItem offline ship fail : ", err);
				resp.setSuccess(false);
				client.sendProtocol(resp.build());
			});
		} else {
			PlayerHelper.addTask(playerId, r -> {
				PayItem payItem = player.getPlayerModule().getPayItems(uid);
				if (payItem == null || payItem.isFinish()) {
					log.error("PayItem online ship fail : " + payItem);
					resp.setSuccess(false);
					client.sendProtocol(resp.build());
					return;
				}
				// 这里只是通知支付后的后续操作，不过一般也不会失败
				if (!player.getPlayerModule().execPayCallback(uid)){//玩家重新登录之前的订单 没有callback 需要走离线补单逻辑
					payItem.getPayType().offlinePay(player,payItem);
				}
				payItem.finish();
				player.handleEvent(EventTypeEnum.Charge, payItem.getRmb());
				GameLogger.recharge(player, payItem);
				// 支付后先实时保存数据到数据库
				PlayerHelper.saveClientCache(playerId).onSuccess(rr -> {
					resp.setSuccess(true);
					client.sendProtocol(resp.build());
				}).onFailure(t -> {
					t.printStackTrace();
					log.error(String.format("充值失败:%s",t.getMessage() ));
					player.handleFail(t);
					resp.setSuccess(false);
					client.sendProtocol(resp.build());
				});
			});
		}
	}
	protected void playerRequest(NetClient client, Object message) {
		GamePlayerRequest_7d000015 request = (GamePlayerRequest_7d000015) message;
		GamePlayerResponse_7d000016.Builder resp = GamePlayerResponse_7d000016.newBuilder();
		long playerId = request.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player == null || player.isIslogouting()) {
			resp.setErrorCode(ErrorMsgEnum.not_online.getId());
			client.sendProtocol(resp.build());
			return;
		}
		ProtobufProtocol protocol = new ProtobufProtocol(
				PbProtocol.getInstance().getMsgId("GamePlayerRequest_7d000015"), request, -1);
		GameClient gameClient = GameClientManager.getInstance().getGameClientByPlayer(playerId);
		PlayerHelper.addTask(playerId, v -> {
			dispatch(gameClient, protocol);
		});

		client.sendProtocol(resp.build());
	}

	protected void playerLogout(NetClient client, Object message) {
		GamePlayerLogoutRequest_7d000101 request = (GamePlayerLogoutRequest_7d000101) message;
		long playerId = request.getPlayerId();
		PlayerHelper.addTask(playerId, v -> {
			Future<?> logout = GameClientManager.getInstance().logout(playerId, LogoutType.ClientRequest);
			logout.onComplete(r -> {
				Throwable cause = r.cause();
				client.sendProtocol(cause != null ? cause : GamePlayerLogoutResponse_7d000102.getDefaultInstance());
			});
		});

	}

	protected void db(NetClient client, Object message) {
		GameDataPush_7d00000a request = (GameDataPush_7d00000a) message;
		String mapperClass = request.getMapperClass();
		String method = request.getMethod();
		Object arg = KryoUtils.deserializeClassAndObject(request.getArg().toByteArray());
		Class<?> clazz = null;
		try {
			clazz = Class.forName(mapperClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		DataGameServerInterface dataGameServerInterface = SpringContextLoader.getContext()
				.getBean(DataGameServerInterface.class);
		dataGameServerInterface.exec(clazz, method, arg);

	}

	protected void dbBatch(NetClient client, Object message) {
		GameDataPushBatch_7d00000b request = (GameDataPushBatch_7d00000b) message;
		List<DbTaskProto> dbTasksList = request.getDbTasksList();
		List<DbTask> list = new ArrayList<>();

		for (DbTaskProto proto : dbTasksList) {
			String mapperClass = proto.getMapperClass();
			Class<?> clazz = null;
			try {
				clazz = Class.forName(mapperClass);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			ByteString arg = proto.getArg();
			Object obj = KryoUtils.deserializeClassAndObject(arg.toByteArray());
			DbTask dbTask = new DbTask(clazz, proto.getMethod(), obj);
			list.add(dbTask);
		}

		DataGameServerInterface dataGameServerInterface = SpringContextLoader.getContext()
				.getBean(DataGameServerInterface.class);
		Object ret = dataGameServerInterface.execMutiTasks(list);

		client.sendProtocol(ret);

	}

	protected void dbBatch2(NetClient client, Object message) {
		GameDataPushBatch2_7d00000c request = (GameDataPushBatch2_7d00000c) message;
		List<DbTask> list = (List<DbTask>) KryoUtils.deserializeClassAndObject(request.getArg().toByteArray());

		DataGameServerInterface dataGameServerInterface = SpringContextLoader.getContext()
				.getBean(DataGameServerInterface.class);
		Object ret = dataGameServerInterface.execMutiTasks(list);

		client.sendProtocol(ret);

	}

	/**
	protected void archiveList(NetClient client, Object message) {
		LoginGameArchiveListRequest_7d000301 req = (LoginGameArchiveListRequest_7d000301) message;
		LoginGameArchiveListResponse_7d000302.Builder resp = LoginGameArchiveListResponse_7d000302.newBuilder();
		long userId = req.getUserId();
	
		GameServer.getInstance().getDataGameServerInterface().execAsync(PlayerMapper.class, "selectPlayersByUid", userId).onSuccess(
				p -> {
					List<Player> list = (List<Player>) p;
					resp.addAllArchives(PbBuilder.buildPlayerArchiveInfos(list));
					client.sendProtocol(resp.build());
	
				}).onFailure(p -> {
	//					log.error("pc player session  " + sessionId + " login error ", p);
	//					client.sendProtocol(resp.build(), ErrorMsgEnum.unknown.getId());
				});
	}
	protected void archiveCreate(NetClient client, Object message) {
		LoginGameArchiveCreateRequest_7d000303 req = (LoginGameArchiveCreateRequest_7d000303) message;
		String name = req.getName();
	
	}
	*/
	protected void alive(NetClient client, Object message) {

		int onlineCount = GameClientManager.getInstance().getOnlineCount();
		client.sendProtocol(ServerStatusResponse_7d000902.newBuilder().setOnline(onlineCount).build());

	}

	protected void test(NetClient client, Object message) {
		GameTestRequest_7d000500 req = (GameTestRequest_7d000500) message;
		int id = req.getId();
		long arg = 333;
		VxHolder.vertx.runOnContext(v -> {
			// xxx logic
			// 返回结果
			client.sendProtocol(GameTestResponse_7d000501.newBuilder().setId(id + 100));
		});
	}

	protected void playerPush(NetClient client, Object message) {
		GamePlayerPush_7d000100 req = (GamePlayerPush_7d000100) message;
		int id = req.getId();
		long playerId = req.getPlayerId();
		ByteString data = req.getData();
		int errorCode = req.getErrorCode();

		GameClient gameClient = GameClientManager.getInstance().getGameClientByPlayer(playerId);
		if (gameClient != null) {
			gameClient.sendProtocol(id, 0, data.toByteArray(), errorCode, false);
		}
	}

	protected void online(NetClient client, Object message) {
		GamePlayerOnlinePush_7d000010 req = (GamePlayerOnlinePush_7d000010) message;
		long playerId = req.getPlayerId();
		String serverId = req.getServerId();
		if (serverId.equals(ServerContext.getInstance().getServerId())) {
			return ; 
		}
		boolean online = req.getOnline();
		if (online) {
			Player oldPlayer = PlayerManager.getInstance().getPlayer(playerId);
			if (oldPlayer != null) {
				log.warn("player online in multi server, player id: " + playerId + ", old server id: " + serverId + ", new server id: "
						+ ServerContext.getInstance().getServerId());
				// 需要通知新登录的服务器，退出当前player
				Future<GamePlayerLogoutResponse_7d000102> requestRemoteServer = VxHolder.requestRemoteServer(serverId,
						GamePlayerLogoutRequest_7d000101.newBuilder().setPlayerId(playerId).build());
				requestRemoteServer.onFailure(ee -> {
					PlayerHelper.addTask(playerId, r -> {
						log.warn("multi player found, notify other fail, logout current " + playerId) ; 
						GameClientManager.getInstance().logout(playerId, LogoutType.LoginOtherServer);
					});
				}).onSuccess(r -> {
					TaskManager.getInstance().scheduleGeneral(() -> {
						// 重新广播在线信息
						VxHolder.broadcastRemoteServer(ServerType.Game,
								GamePlayerOnlinePush_7d000010.newBuilder().setOnline(true)
										.setServerId(ServerContext.getInstance().getServerId()).build());
					}, 5000);
				});
			} else {
				PlayerManager.getInstance().online(playerId, serverId);
			}
		} else {
			PlayerManager.getInstance().offline(playerId);
		}
	}

	protected void broadcast(NetClient client, Object message) {
		GameCrossBroadcast_7d000008 req = (GameCrossBroadcast_7d000008) message;
		ProtocolStringList serverIdList = req.getServerIdList();

		int id = req.getId();
		ByteString data = req.getData();

		if (serverIdList.isEmpty()) {
			VxHolder.broadcastRemoteServer(ServerType.Game, new ByteArrayProtocol(id, data.toByteArray()));
		} else {
			for (String server : serverIdList) {
				VxHolder.requestRemoteServer(server, new ByteStringProtocol(id, data));
			}
		}

	}

	protected void broadcastPlayers(NetClient client, Object message) {
		GameCrossPlayerBroadcast_7d000005 req = (GameCrossPlayerBroadcast_7d000005) message;
		ProtocolStringList serverIdList = req.getServerIdList();
		List<Long> playerIdList = req.getPlayerIdList();

		int id = req.getId();
		ByteString data = req.getData();
		int errorCode = req.getErrorCode();

		for (int i = 0; i < playerIdList.size(); i++) {

			CrossGameForwardPush_7d000003.Builder resp = CrossGameForwardPush_7d000003.newBuilder();
			resp.setId(id);
			resp.setData(data);
			resp.setErrorCode(errorCode);
			resp.setPlayerId(playerIdList.get(i));

			VxHolder.requestRemoteServer(serverIdList.get(i), resp.build());
		}

	}

	protected void forward(NetClient client, Object message) {

		GameCrossForwardPush_7d000002 req = (GameCrossForwardPush_7d000002) message;
		int id = req.getId();
		String serverId = req.getServerId();
		long playerId = req.getPlayerId();
		ByteString data = req.getData();
		int errorCode = req.getErrorCode();

		CrossGameForwardPush_7d000003.Builder resp = CrossGameForwardPush_7d000003.newBuilder();
		resp.setId(id);
		resp.setData(data);
		resp.setErrorCode(errorCode);
		resp.setPlayerId(playerId);

		VxHolder.requestRemoteServer(serverId, resp.build());

	}

	protected void receive(NetClient client, Object message) {

		CrossGameForwardPush_7d000003 req = (CrossGameForwardPush_7d000003) message;
		int id = req.getId();
		long playerId = req.getPlayerId();
		ByteString data = req.getData();
		int errorCode = req.getErrorCode();

		GameClient gameClient = GameClientManager.getInstance().getGameClientByPlayer(playerId);
		if (gameClient != null) {
			gameClient.sendProtocol(id, 0, data.toByteArray(), errorCode, false);
		}

	}

}
