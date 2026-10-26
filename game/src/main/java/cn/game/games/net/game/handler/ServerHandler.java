package cn.game.games.net.game.handler;

import cn.game.games.net.game.helper.PlayerWriteGuard;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.WeakHashMap;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolStringList;

import cn.game.core.base.ServerContext;
import cn.game.core.util.AsyncUtils;
import cn.game.core.util.ExceptionHelper;
import cn.game.core.net.client.LogoutType;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.bytes.ByteArrayProtocol;
import cn.game.core.net.protocol.object.ByteStringProtocol;
import cn.game.core.net.protocol.object.ProtobufProtocol;
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
import cn.game.games.net.game.helper.OfflinePlayerInitializationFailure;
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
import cn.game.protocol.protobuf.GuildMsg;
import cn.game.protocol.protobuf.GuildMsg.GuildApplyProcessedPush_40100001;
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
import cn.game.util.Config;
import cn.game.util.KryoUtils;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * 服务器之间的消息处理器
 */
@Component
public class ServerHandler extends GameBaseHandler {

    /** 同一实例的离线发货共用正在初始化的加载结果，不能消费尚未初始化完的驻留对象。 */
    private final ConcurrentHashMap<Long, Future<Player>> paymentLoads = new ConcurrentHashMap<>();
    /** 已观察到加载失败的驻留实例必须隔离；弱键不延长已安全释放玩家的生命周期。 */
    private final Map<Player, Boolean> failedPaymentLoads = Collections.synchronizedMap(new WeakHashMap<>());

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
		putInvoker(PbProtocol.NotifyGuildMsgToGame_7d000047, this::guildMsgNotify);



//		putInvoker(PbProtocol.LoginGameArchiveListRequest_7d000301, this::archiveList);
//		putInvoker(PbProtocol.LoginGameArchiveCreateRequest_7d000303, this::archiveCreate);
	}

	private void guildMsgNotify(NetClient client, Object o) {
		ServerMsg.NotifyGuildMsgToGame_7d000047 req = (ServerMsg.NotifyGuildMsgToGame_7d000047) o;
		int msgId = req.getMsgId();
		Message message = PbProtocol.getInstance().parseFrom(msgId, req.getData());
		req.getPlayerIdList().forEach(pid ->{
			Player player = PlayerManager.getInstance().getPlayer(pid);
			if (player == null) {
				log.error("guildMsgNotify player is null");
				return;
			}
            submitPlayerMutation(player, () -> {
			switch (msgId){
				//玩家 退出 公会
				case PbProtocol.GuildQuitPush_40000024 -> {
					player.getGuildModule().kickGuild((GuildMsg.GuildQuitPush_40000024) message);
				}
				//玩家 加入 公会
				case PbProtocol.GuildJoinPush_40000044 -> player.getGuildModule().joinAndPush((GuildMsg.GuildJoinPush_40000044) message);
				case PbProtocol.ChatMessagePush_31010001 -> {//公会聊天
					guildChat(player,(ChatMsg.ChatMessagePush_31010001) message);
				}
				case PbProtocol.GuildApplyProcessedPush_40100001 -> {
					GuildApplyProcessedPush_40100001 push = (GuildApplyProcessedPush_40100001)message; 
					Player player2 = PlayerManager.getInstance().getPlayer(push.getPlayerId()); 
					if (player2 != null) {
						submitPlayerMutation(player2, () -> player2.getGuildModule().getApplyJoinList().remove(push.getGuildId()))
                                .onFailure(failure -> log.warn("公会申请变化失败 playerId={}", player2.getPlayerId(), failure));
					}else {
						// TODO 离线稍后处理
					}
				}
				default -> {
					log.error(String.format("guildMsgNotify msgId:%d is error",req.getMsgId()));
				}
			}
            }).onFailure(failure -> log.warn("公会推送玩家变化失败 playerId={}", pid, failure));
		});
	}

	private void guildChat(Player notifyPlayer, ChatMsg.ChatMessagePush_31010001 req) {
		notifyPlayer.getGameClient().sendProtocol(req);
	}

	private void playerEvent(NetClient client, Object o) {
		PlayerEvent gameEvent = (PlayerEvent) o;
		long playerId = gameEvent.getSourceId();

	}
	private void InviteLvChange(NetClient client, Object o) {
		ServerMsg.NotifyInviteBindAndLvUpRequest_7d000041 req = (ServerMsg.NotifyInviteBindAndLvUpRequest_7d000041) o;
		Player player = PlayerManager.getInstance().getPlayer(req.getPid());
        if (player != null && player.isOnline()) {
            submitPlayerMutation(player, () -> {
                player.getInviteModule().updateTargetLv(req.getTargetPid(), req.getLv());
                player.getInviteModule().checkRedHot();
            }).onComplete(result -> {
                if (result.failed()) { log.warn("邀请等级变化失败 playerId={}", req.getPid(), result.cause()); }
                client.sendProtocol(ServerMsg.NotifyInviteBindAndLvUpResponse_7d000042.newBuilder().setResult(result.succeeded()).build());
            });
        } else { client.sendProtocol(ServerMsg.NotifyInviteBindAndLvUpResponse_7d000042.newBuilder().setResult(true).build()); }

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
        PlayerHelper.modifyPlayer(playerId, p -> { consumer.accept(p); return true; })
                .onFailure(failure -> log.warn("问卷玩家变更失败 playerId={}", playerId, failure));

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
	
    /** @param client 本次 Login RPC 的连接 @param message 发货请求；加载异步完成后再进入玩家邮箱 */
    protected void ship(NetClient client, Object message) {
        PaymentOrderShipRequest_7d000022 request = (PaymentOrderShipRequest_7d000022) message;
        long playerId = request.getPlayerId();
        log.info("PaymentOrder ship push, playerId={}, uid={}", playerId, request.getUid());
        try {
            Future<Player> loading = paymentLoads.get(playerId);
            Player resident = paymentPlayer(playerId);
            // 加载会先登记对象再回投初始化；重查在途加载，避免使用这个中间态。
            if (loading == null) { loading = paymentLoads.get(playerId); }
            Future<Player> selected = loading != null ? loading : resident != null
                    ? Future.succeededFuture(resident) : sharedPaymentLoad(playerId);
            selected.compose(player -> {
                if (player == null) { return Future.failedFuture("PAYMENT_PLAYER_MISSING"); }
                return submitPayment(playerId, () -> handlePaymentForPlayer(client, player,
                        request.getUid(), request.getSdkOrderId(), player.isOnline()));
            }).onFailure(failure -> {
                log.warn("充值加载或邮箱受理失败 playerId={} uid={}", playerId, request.getUid(), failure);
                replyPayment(client, false, playerId);
            });
        } catch (Exception failure) {
            log.warn("充值投递失败 playerId={} uid={}", playerId, request.getUid(), failure);
            replyPayment(client, false, playerId);
        }
    }

    /** @param playerId 玩家 ID @return 只在本 Handler 内共用的加载结果；不得在玩家邮箱同步等待它 */
    private Future<Player> sharedPaymentLoad(long playerId) {
        Promise<Player> pending = Promise.promise();
        Future<Player> previous = paymentLoads.putIfAbsent(playerId, pending.future());
        if (previous != null) { return previous; }
        try {
            // 初次查找与登记之间可能已有完整驻留对象；仍须在实际执行时核对其身份。
            Player resident = paymentPlayer(playerId);
            Future<Player> load = resident == null ? loadPaymentPlayer(playerId) : Future.succeededFuture(resident);
            load.onComplete(result -> {
                if (result.failed()) { quarantinePaymentLoad(playerId, result.cause()); }
                pending.handle(result);
                paymentLoads.remove(playerId, pending.future());
            });
        } catch (Exception failure) {
            log.warn("离线充值加载启动失败 playerId={}", playerId, failure);
            quarantinePaymentLoad(playerId, failure);
            pending.tryFail(failure);
            paymentLoads.remove(playerId, pending.future());
        }
        return pending.future();
    }

    /** @param playerId 加载失败的玩家 @param failure 失败原因；没有完整初始化证据时不得消费当前驻留实例 */
    private void quarantinePaymentLoad(long playerId, Throwable failure) {
        OfflinePlayerInitializationFailure initialization = ExceptionHelper.findCause(failure, OfflinePlayerInitializationFailure.class);
        Player resident = initialization == null ? null : initialization.player();
        if (resident != null && resident.getPlayerId() == playerId && paymentPlayer(playerId) == resident) {
            failedPaymentLoads.put(resident, true);
            log.warn("充值加载失败后仍有驻留对象，隔离本实例后续发货，需核对初始化与归属 playerId={}", playerId, failure);
        }
    }

    /** @param playerId 玩家 ID @return 本进程权威驻留对象，缺失返回 null */
    protected Player paymentPlayer(long playerId) { return PlayerManager.getInstance().getPlayer(playerId); }

    /** @param playerId 玩家 ID @return 完成模块初始化的离线对象；初始化内部需要玩家邮箱 */
    protected Future<Player> loadPaymentPlayer(long playerId) { return PlayerHelper.loadPlayerFromDb(playerId); }

    /** @param playerId 玩家 ID @param action 发货及保存收尾 @return 实际任务终态，包括开始前拒绝 */
    protected Future<Void> submitPayment(long playerId, Runnable action) {
        return ServerContext.getInstance().getProcessor().processAsync(playerId, action, false);
    }

    /** @param player 原实例；离线加载沿用默认 login 标记，只允许没有真实通道的临时连接 */
    protected void requirePaymentOwner(Player player) {
        if (!singlePaymentStorage()) { throw new IllegalStateException("PAYMENT_REQUIRES_AGGREGATE_STORAGE"); }
        GameClient connection = player.getGameClient();
        if (paymentPlayer(player.getPlayerId()) != player || failedPaymentLoads.containsKey(player) || !player.isActive() || player.isIslogouting()
                || (player.isOnline() && player.isIslogining())
                || (!player.isOnline() && (connection == null || connection.getChannel() != null))) {
            throw new IllegalStateException("PAYMENT_PLAYER_LIFECYCLE_CHANGED playerId=" + player.getPlayerId());
        }
    }

    /** @return 当前存储是否能以一份玩家快照确认本次订单与奖励；独立表不具备该回执 */
    protected boolean singlePaymentStorage() { return ServerContext.getInstance().isSinglePlayerTable(); }
	/**
	 * 统一处理一个玩家的一笔订单（在线/离线通用）
	 */
	protected void handlePaymentForPlayer(NetClient client, Player player, long uid,String sdkOrderId, boolean online) {
	    try {
            requirePaymentOwner(player);
            online = player.isOnline();

            // 凭证只能来自已提交的本批聚合快照，历史 finish 和内存奖励阶段均不足以成功去重。
            PayItem existing = player.getPlayerModule().getPayItems(uid);
            if (existing != null && existing.hasCommittedCompletionProof(player.getPlayerId(), sdkOrderId)) {
                replyPayment(client, true, player.getPlayerId());
                return;
            }
            if (existing != null && existing.getSdkOrderId() != null
                    && !java.util.Objects.equals(existing.getSdkOrderId(), sdkOrderId)) {
                replyPayment(client, false, player.getPlayerId());
                return;
            }
	        PayItem payItem = findAndValidatePayItem(player, uid, online);
	        if (payItem == null) {
                replyPayment(client, false, player.getPlayerId());
	            return;
	        }
            boolean ok;
            try (PlayerWriteGuard.Scope ignored = beginPaymentMutation(player)) {
                payItem.setSdkOrderId(sdkOrderId);
                ok = processPayment(player, payItem, online);
            }
	        if (!ok) {
	            // processPayment 内部已做必要日志
                replyPayment(client, false, player.getPlayerId());
	            return;
	        }

	        persistAndReply(client, player, online, uid);
	    } catch (Exception e) {
	        log.error("ship unexpected error, playerId={}, uid={}", player.getPlayerId(), uid, e);
            player.getPlayerModule().confirmPayCallback(uid, e);
            // 未确认业务及保存终态时保留原对象；错误只回本次 Login RPC，不能发给重连后的购买请求。
            replyPayment(client, false, player.getPlayerId());
	    }
	}

    /** 充值业务变化先结束作用域，再由persistAndReply捕获最终快照。 */
    protected PlayerWriteGuard.Scope beginPaymentMutation(Player player) { return PlayerWriteGuard.begin(player); }

	/**
	 * 查找订单并进行幂等校验
	 */
	protected PayItem findAndValidatePayItem(Player player, long uid, boolean online) {
	    PayItem payItem = player.getPlayerModule().getPayItems(uid);
	    if (payItem == null) {
	        if (online) {
	            log.error("PayItem[{}] ship fail, order not exist (online), playerId={}", uid, player.getPlayerId());
	        } else {
	            log.warn("PayItem[{}] ship fail, order not exist (offline), playerId={}", uid, player.getPlayerId());
	        }
	        return null;
	    }
	    if (payItem.isFinish() || payItem.getCompletionState() != null) {
	        if (online) {
	            log.error("PayItem[{}] ship repeated (online), playerId={}, payItem={}", uid, player.getPlayerId(), payItem);
	        } else {
	            log.warn("PayItem[{}] ship repeated (offline), playerId={}, payItem={}", uid, player.getPlayerId(), payItem);
	        }
	        return null;
	    }
	    return payItem;
	}

    /**
     * 在原玩家邮箱执行完整购买奖励与同步 Charge 链，再标记业务完成。
     * @param player 原权威玩家 @param payItem 未执行订单 @param online 当前在线状态
     * @return 所有必要步骤是否明确完成；异常保留待核对状态，不允许自动重放部分奖励
     */
    protected boolean processPayment(Player player, PayItem payItem, boolean online) {
        payItem.setCompletionState("EXECUTING");
        try {
            boolean completed = online && player.getPlayerModule().execPayCallback(payItem.getOrderId());
            if (!completed) {
                completed = payItem.getPayType().offlinePay(player, payItem);
            }
            if (!completed) { throw new IllegalStateException("PAYMENT_OFFLINE_REWARD_NOT_COMPLETED"); }
            requirePaymentOwner(player);
            player.fireAndHandleEvent(EventTypeEnum.Charge, payItem.getRmb());
            GameLogger.recharge(player, payItem);
            payItem.setCompletionState("REWARDS_COMPLETED");
            payItem.finish();
            payItem.prepareCompletionProof(player.getPlayerId());
            return true;
        } catch (Exception failure) {
            payItem.setCompletionState("REVIEW_REQUIRED");
            log.warn("充值奖励未完整完成，阻断自动重发 playerId={} orderId={}", player.getPlayerId(), payItem.getOrderId(), failure);
            throw failure;
        }
    }

    /** 在所属邮箱内关闭业务作用域后保存；只有本次提交确认且原对象仍有效才回成功。
     * @param client 本次 Login RPC @param player 原权威对象 @param online 业务执行时是否在线 @param uid 本次订单 ID */
	protected void persistAndReply(NetClient client, Player player, boolean online, long uid) {
	    long playerId = player.getPlayerId();
        GameClient originalConnection = player.getGameClient();
        try {
            requirePaymentOwner(player);
            // 保存通知在独立线程完成；仅等待保存，不能等待包含邮箱初始化的离线加载。
            List<Object> results = AsyncUtils.await(savePayment(player));
            if (results == null || results.stream().noneMatch(value -> value instanceof
                    cn.game.games.net.game.helper.PlayerSaveCoordinator.Receipt receipt && receipt.playerId() == playerId)) {
                throw new IllegalStateException("PAYMENT_SAVE_NOT_CONFIRMED playerId=" + playerId);
            }
            requirePaymentOwner(player);
            player.getPlayerModule().getPayItems(uid).confirmCompletionProof();
        } catch (Exception failure) {
            log.warn("充值保存未确认或原实例已失效，保留驻留状态 playerId={}", playerId, failure);
            player.getPlayerModule().confirmPayCallback(uid, failure);
            replyPayment(client, false, playerId);
            return;
        }

        // 提交后的发送和清理失败不改写已确认结果，也不追加第二个相反响应。
        player.getPlayerModule().confirmPayCallback(uid, online ? null
                : new IllegalStateException("PAYMENT_COMPLETED_OFFLINE_WITHOUT_ORIGINAL_RESPONSE"));
        replyPayment(client, true, playerId);
        if (!online && player.getGameClient() == originalConnection) {
            try { cleanupIfOffline(player, false); }
            catch (Exception failure) { log.warn("充值确认后离线清理失败，保留对象 playerId={}", playerId, failure); }
        }
	}

    /** @param player 当前邮箱中的原玩家 @return 本次统一保存通道的结果，禁止用空任务或任意 DAO 成功代替回执 */
    protected Future<List<Object>> savePayment(Player player) { return PlayerHelper.saveClientCache(player.getPlayerId()); }

	/**
	 * 离线玩家处理完后清理缓存
	 */
	protected void cleanupIfOffline(Player player, boolean online) {
        GameClient connection = player.getGameClient();
	    if (!online && !player.isOnline() && !player.isIslogouting() && player.isActive()
                && paymentPlayer(player.getPlayerId()) == player && connection != null && connection.getChannel() == null) {
            if (!removePaymentPlayer(player)) { log.warn("离线玩家仍有未确认变化，保留内存 playerId={}", player.getPlayerId()); }
	    }
	}

    /** @param player 已确认且仍属于本次临时离线连接的玩家 @return 是否按原对象身份安全删除 */
    protected boolean removePaymentPlayer(Player player) { return PlayerHelper.clearPlayer(player); }

    /** @param client 本次 Login RPC @param success 已确定的结果 @param playerId 日志身份；发送异常不补发相反结果 */
    private void replyPayment(NetClient client, boolean success, long playerId) {
        try { sendResult(client, success); }
        catch (Exception failure) { log.warn("充值结果发送失败 playerId={} success={}", playerId, success, failure); }
    }

	/**
	 * 统一发送结果
	 */
	protected void sendResult(NetClient client, boolean success) {
	    PaymentOrderShipResponse_7d000023.Builder resp = PaymentOrderShipResponse_7d000023.newBuilder();
	    resp.setSuccess(success);
	    client.sendProtocol(resp.build());
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
		PlayerHelper.addTask(playerId, () -> {
			dispatch(gameClient, protocol);
		});

		client.sendProtocol(resp.build());
	}

	protected void playerLogout(NetClient client, Object message) {
		GamePlayerLogoutRequest_7d000101 request = (GamePlayerLogoutRequest_7d000101) message;
		long playerId = request.getPlayerId();
        PlayerHelper.submitRecovery(playerId, () -> GameClientManager.getInstance().logout(playerId, LogoutType.ClientRequest))
                .onComplete(result -> client.sendProtocol(result.failed() ? result.cause() : GamePlayerLogoutResponse_7d000102.getDefaultInstance()));

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
					PlayerHelper.submitRecovery(playerId, () -> GameClientManager.getInstance().logout(playerId, LogoutType.LoginOtherServer))
                            .onFailure(failure -> log.warn("本服退出保存失败 playerId={}", playerId, failure));
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
