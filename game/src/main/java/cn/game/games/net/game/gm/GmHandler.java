package cn.game.games.net.game.gm;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import cn.game.games.net.game.module.mail.MailType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.id.IdUtil;
import cn.game.core.net.client.LogoutType;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.ForbidAccount;
import cn.game.games.cache.entity.GmMail;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.data.mapper.GmMailMapper;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.games.net.game.handler.GmBaseHandler;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.TestHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.games.util.DAO;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.BaseMsg.GoodsInfo;
import cn.game.protocol.protobuf.GmMsg;
import cn.game.protocol.protobuf.GmMsg.GmAccountForbidListResponse_77000004;
import cn.game.protocol.protobuf.GmMsg.GmAccountForbidRequest_77000005;
import cn.game.protocol.protobuf.GmMsg.GmAccountForbidResponse_77000006;
import cn.game.protocol.protobuf.GmMsg.GmAccountUnblockRequest_77000007;
import cn.game.protocol.protobuf.GmMsg.GmAccountUnblockResponse_77000008;
import cn.game.protocol.protobuf.GmMsg.GmPlayerDeleteRequest_77000052;
import cn.game.protocol.protobuf.GmMsg.GmPlayerDeleteResponse_77000053;
import cn.game.protocol.protobuf.GmMsg.GmPlayerLogoutRequest_77000009;
import cn.game.protocol.protobuf.GmMsg.GmPlayerLogoutResponse_7700000a;
import cn.game.protocol.protobuf.GmMsg.GmPlayerRenameRequest_77000050;
import cn.game.protocol.protobuf.GmMsg.GmPlayerRenameResponse_77000051;
import cn.game.protocol.protobuf.GmMsg.GmPlayerRequest_77000021;
import cn.game.protocol.protobuf.GmMsg.GmPlayerResponse_77000022;
import cn.game.protocol.protobuf.GmMsg.GmPlayerTDLevelRequest_77000054;
import cn.game.protocol.protobuf.GmMsg.GmPlayerTDLevelResponse_77000055;
import cn.game.protocol.protobuf.GmMsg.GmServerOpRequest_77000030;
import cn.game.protocol.protobuf.GmMsg.GmServerOpResponse_77000031;
import cn.game.protocol.protobuf.GmMsg.GmServerStatusRequest_77000032;
import cn.game.protocol.protobuf.GmMsg.GmServerStatusResponse_77000033;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.protocol.protobuf.ServerMsg.GameOpRequest_7d000373;
import cn.game.protocol.protobuf.ServerMsg.GameStatusChangeRequest_7d000030;
import cn.game.protocol.protobuf.ServerMsg.GameStatusChangeResponse_7d000031;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import cn.game.util.ServerType;
import io.vertx.core.Future;

/** gm处理器 */
@Component
public class GmHandler extends GmBaseHandler {

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
		putInvoker(PbProtocol.GmPlayerRequest_77000021, this::playerInfo);
		putInvoker(PbProtocol.GmMailServerSendRequest_77000048, this::gmSendMail);
		putInvoker(PbProtocol.GmMailListRequest_77000042, this::selectGmMailList);
		putInvoker(PbProtocol.GmMailCheckRequest_77000044, this::checkMail);
		putInvoker(PbProtocol.GmMailDeleteRequest_77000046, this::delGmMail);
		putInvoker(PbProtocol.GmServerStatusRequest_77000032, this::serverStatus);
		putInvoker(PbProtocol.GmServerOpRequest_77000030, this::serverOp);
		putInvoker(PbProtocol.GmPlayerRenameRequest_77000050, this::rename);
		putInvoker(PbProtocol.GmPlayerDeleteRequest_77000052, this::playerDelete);
		putInvoker(PbProtocol.GmPlayerTDLevelRequest_77000054, this::tdLv);
	}

	private void tdLv(NetClient client, Object o) {
		GmPlayerTDLevelRequest_77000054 req = (GmPlayerTDLevelRequest_77000054) o;
		GmPlayerTDLevelResponse_77000055 resp = GmPlayerTDLevelResponse_77000055.getDefaultInstance();
		if (!GmSecurityPolicy.isActionAllowed(GmSecurityPolicy.currentRunMode(), GmSecurityTier.OPS_REPAIR)) {
			client.sendProtocol(resp, ErrorMsgEnum.production_gm_not_allow.getId());
			return;
		}
		long playerId;
		try {
			playerId = Long.parseLong(req.getPlayerId());
		} catch (NumberFormatException e) {
			log.warn("0x77 tdLv playerId格式非法: playerId={}", req.getPlayerId(), e);
			client.sendProtocol(resp, ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
		int level = req.getLevel();
		if (level <= 0) {
			client.sendProtocol(resp, ErrorMsgEnum.gm_cmd_param.getId());
			return;
		}

		PlayerHelper.modifyPlayer(playerId, player -> GmDispatcher.updateHeavenlyDaoLevel(player, level))
				.onComplete(ar -> {
					if (ar.succeeded()) {
						client.sendProtocol(resp);
					} else {
						log.error("0x77 tdLv修改失败: playerId={}, level={}", playerId, level, ar.cause());
						client.sendProtocol(resp, ErrorMsgEnum.unknown.getId());
					}
				});
	}

	private void playerDelete(NetClient client, Object o) {
		GmPlayerDeleteRequest_77000052 req = (GmPlayerDeleteRequest_77000052) o;
		GmPlayerDeleteResponse_77000053 resp = GmPlayerDeleteResponse_77000053.getDefaultInstance();

		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			log.warn("生产环境禁止通过0x77协议删除玩家数据: playerId={}", req.getPlayerId());
			client.sendProtocol(resp, ErrorMsgEnum.production_gm_not_allow.getId());
			return;
		}
		long playerId;
		try {
			playerId = Long.parseLong(req.getPlayerId().trim());
		} catch (Exception e) {
			log.warn("0x77 playerDelete playerId 非法: {}", req.getPlayerId(), e);
			client.sendProtocol(resp, ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
		TestHelper.deletePlayerData(playerId);
		client.sendProtocol(resp);

	}

	private void rename(NetClient client, Object o) {
		GmPlayerRenameRequest_77000050 req = (GmPlayerRenameRequest_77000050) o;
		GmPlayerRenameResponse_77000051 resp = GmPlayerRenameResponse_77000051.getDefaultInstance();
		String playerIdString = req.getPlayerId();
		String newName = req.getName();
		long playerId;
		try {
			playerId = Long.parseLong(playerIdString.trim());
		} catch (Exception e) {
			log.warn("0x77 rename playerId 非法: {}", playerIdString, e);
			client.sendProtocol(resp, ErrorMsgEnum.request_parameter_error.getId());
			return;
		}
		Player me = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		GameServerInterface gameServerInterface = GameServer.getInstance().getGameServerInterface(DistributedObjectType.PLAYER, playerId);
		Future<?> renameFuture = gameServerInterface.rename(playerId, newName);
		renameFuture.onSuccess(r -> {
			client.sendProtocol(resp);

		}).onFailure(e -> me.handleFail(e));
	}

	private void serverStatus(NetClient client, Object o) {
		GmServerStatusRequest_77000032 req = (GmServerStatusRequest_77000032) o;
		GmServerStatusResponse_77000033 resp = GmServerStatusResponse_77000033.getDefaultInstance();
		String serverId = req.getServerId();
		int status = req.getStatus();
		if (status <= 0 || status > 2 || StringUtils.isEmpty(serverId)) {
			client.sendProtocol(resp, ErrorMsgEnum.request_parameter_error.ID);
			return;
		}
		Future<GameStatusChangeResponse_7d000031> requestRemoteServer = VxHolder.requestRemoteServer(serverId,
				GameStatusChangeRequest_7d000030.newBuilder().setStatus(status).build());

		requestRemoteServer.onSuccess(r -> {
			client.sendProtocol(resp);
		}).onFailure(r -> {
			client.sendProtocol(resp, ErrorMsgEnum.unknown.ID);
		});
	}

	private void serverOp(NetClient client, Object o) {

		GmServerOpRequest_77000030 req = (GmServerOpRequest_77000030) o;
		String serverId = req.getServerId();
		int opType = req.getOpType();
		VxHolder.requestRemoteServer(serverId, GameOpRequest_7d000373.newBuilder().setOpType(opType).build());

		client.sendProtocol(GmServerOpResponse_77000031.getDefaultInstance());
	}

	private void gmSendMail(NetClient client, Object o) {
		GmMsg.GmMailServerSendRequest_77000048 req = (GmMsg.GmMailServerSendRequest_77000048) o;
		GmMsg.GmMailServerSendResponse_77000049.Builder res = GmMsg.GmMailServerSendResponse_77000049.newBuilder();
		String title = req.getTitle();
		String content = req.getContent();
		List<GoodsInfo> attachmentsList = req.getAttachmentsList();
		List<Goods> list = new ArrayList<>();
		for (GoodsInfo goods : attachmentsList) {
			Goods g = new Goods();
			g.setId(goods.getId());
			g.setCount(goods.getCount());
			list.add(g);
		}
		Player gmSendPlayer = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (title.isEmpty() || content.isEmpty()) {
			sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.request_parameter_null, "发送邮件");
			return;
		}
		GmMail gmMail = new GmMail();
		gmMail.setId(IdUtil.getId());
		gmMail.setTitle(title);
		gmMail.setContext(content);
		gmMail.setOptFlag((byte) 0);
		gmMail.setCreateTime(new Date());
		gmMail.setSendName(
				String.format("%s:%s:%d", gmSendPlayer.getAccount().accountId, gmSendPlayer.getData().getName(), client.getPlayerId()));
		if (!list.isEmpty()) {
			gmMail.setAttachment(JsonUtil.toJsonStringWithType(list));
		}
		// 全服邮件
		if (req.getPlayerIdsCount() == 0) {
			if (req.getSendEndTime() <= req.getSendStartTime() || req.getLevelEnd() <= req.getLevelStart()
					|| (req.getTimeCheckType() != 0 && req.getTimeCheckType() != 1)) {
				sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.request_parameter_null, "发送邮件");
				return;
			}
			gmMail.setServerids(req.getServerIdList().toString());

			gmMail.setSendStartTimer(DateUtil.getTimeByPattern(new Date(req.getSendStartTime() * 1000L), DateUtil.pattern_en));
			gmMail.setSendEndTimer(DateUtil.getTimeByPattern(new Date(req.getSendEndTime() * 1000L), DateUtil.pattern_en));
			gmMail.setMinLevel(req.getLevelStart());
			gmMail.setMaxLevel(req.getLevelEnd());
			gmMail.setOptFlag((byte) 0);
			gmMail.setTimeCheckType((byte) req.getTimeCheckType());
			gmMail.setMailopttype((byte) 1);
			if (req.getType() == 1) {
				gmMail.setMailopttype((byte) 2);
			}
		} else {
			gmMail.setMailopttype((byte) 0);
			gmMail.setPids(req.getPlayerIdsList().toString());
		}
		DAO.insert(gmMail).onSuccess(r -> {
			sendAndRecordOpt(client, req, res.build(), "发送邮件");
		}).onFailure(e -> {
			e.printStackTrace();
			sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.unknown, "发送邮件");
		});
	}

	private void selectGmMailList(NetClient client, Object o) {
		GmMsg.GmMailListRequest_77000042 req = (GmMsg.GmMailListRequest_77000042) o;
		GmMsg.GmMailResponse_77000043.Builder res = GmMsg.GmMailResponse_77000043.newBuilder();
		int page = req.getPageNum();
		int size = req.getPageSize();
		if (page < 1 || size < 1) {
			sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.request_parameter_null, "查询邮件");
			return;
		}
		Object[] params = { req.getStartTime() == 0 ? null : new java.sql.Date(req.getStartTime() * 1000L),
				req.getEndTime() == 0 ? null : new java.sql.Date(req.getEndTime() * 1000L),
				req.getType() == 0 ? null
						: (req.getType() == 1 ? 0 : req.getType() == 3 ? 2
								: 1), req.getTitle() == null ? null : req.getTitle(),
				req.getContent() == null ? null : req.getContent(), req.getStatus() == 0 ? null : (req.getStatus() == 1 ? 0 : 2),
				(page - 1) * size, size };
		DAO.execute(GmMailMapper.class, "selectGmMailList", params).onSuccess(result -> {
			List<GmMail> list = (List<GmMail>) result;
			if (list != null) {
				list.forEach(gmMail -> {
					try {
						if (gmMail.getMailopttype() <= 2) {
							res.addMails(GmHelper.toGmMailPb(gmMail));
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				});
			}
			sendAndRecordOpt(client, req, res.build(), "查询邮件");
		}).onFailure(e -> {
			e.printStackTrace();
			sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.unknown, "查询邮件");
		});
	}

	private void checkMail(NetClient client, Object o) {
		GmMsg.GmMailCheckRequest_77000044 req = (GmMsg.GmMailCheckRequest_77000044) o;
		GmMsg.GmMailCheckResponse_77000045.Builder res = GmMsg.GmMailCheckResponse_77000045.newBuilder();
		if (req.getUidCount() <= 0) {
			sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.request_parameter_null, "审核邮件");
			return;
		}
		req.getUidList().forEach(mailId -> {
			DAO.execute(GmMailMapper.class, MapperConstant.selectByPrimaryKey, Long.parseLong(mailId)).onSuccess(r -> {
				if (r == null) {
					return;
				}
				GmMail gmMail = (GmMail) r;
				if (gmMail.getApprovalTimer() != null) {
					sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.request_parameter_null, "审核邮件");
					return;
				}
				gmMail.setApprovalTimer(DateUtil.getStringDate());
				gmMail.setOptFlag((byte) 1);
				List<Goods> attachment = GmHelper.getAttachment(gmMail);
				DAO.update(gmMail);

				// 个人邮件
				if (gmMail.getPids() != null) {
					String[] pids = gmMail.getPids().replace("[", "").replace("]", "").trim().split(",");
					for (String pid : pids) {
						MailHelper.sendMail(Long.parseLong(pid.trim()), 0, null, "系统管理员", gmMail.getTitle(), gmMail.getContext(),
								MailType.System.getValue(), attachment, true);
					}
				} else { // 全服邮件
					MailHelper.addGlobalMail(gmMail);
					// 通知其他节点 添加新的全服邮件
					VxHolder.broadcastRemoteServer(ServerType.Game,
							ServerMsg.NotifyAddGlobalGmMailRequest_7d000060.newBuilder().setAddGmMailId(gmMail.getId()).build());
//                            GameServer.getInstance().getCrossGameServerInterfaceSync().notifyBroadcastAddGlobalGmMail(gmMail.getId());
				}
				sendAndRecordOpt(client, req, res.build(), "审核邮件");
			}).onFailure(e -> {
				e.printStackTrace();
				sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.unknown, "审核邮件");
			});
		});
	}

	private void delGmMail(NetClient client, Object o) {
		GmMsg.GmMailDeleteRequest_77000046 req = (GmMsg.GmMailDeleteRequest_77000046) o;
		GmMsg.GmMailDeleteResponse_77000047.Builder res = GmMsg.GmMailDeleteResponse_77000047.newBuilder();
		if (req.getUidCount() <= 0) {
			sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.request_parameter_null, "删除邮件");
			return;
		}
		List<Integer> mailIds = new ArrayList<>(req.getUidCount());
		for (String rawMailId : req.getUidList()) {
			if (StringUtils.isEmpty(rawMailId)) {
				sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.request_parameter_error, "删除邮件");
				return;
			}
			try {
				mailIds.add(Integer.parseInt(rawMailId.trim()));
			} catch (NumberFormatException e) {
				log.warn("0x77 delGmMail mailId 非法: {}", rawMailId, e);
				sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.request_parameter_error, "删除邮件");
				return;
			}
		}
		List<Future<?>> futures = new ArrayList<>(mailIds.size());
		for (int mailId : mailIds) {
			Future<?> future = DAO.execute(GmMailMapper.class, MapperConstant.deleteByPrimaryKey, mailId).onSuccess(r -> {
				if (r != null && MailHelper.removeGlobalMail(mailId)) {
					// 该邮件是全服邮件, 通知其他节点删除该邮件
					VxHolder.broadcastRemoteServer(ServerType.Game,
							ServerMsg.NotifyDelGlobalGmMailRequest_7d000062.newBuilder().setDelGmMailId(mailId).build());
				}
			});
			futures.add(future);
		}
		Future.all(futures)
				.onSuccess(v -> sendAndRecordOpt(client, req, res.build(), "删除邮件"))
				.onFailure(e -> {
					log.error("0x77 delGmMail 删除邮件失败: mailIds={}", mailIds, e);
					sendAndRecordOpt(client, req, res.build(), ErrorMsgEnum.unknown, "删除邮件");
				});
	}

	protected void playerInfo(NetClient client, Object message) {
		GmPlayerRequest_77000021 request = (GmPlayerRequest_77000021) message;
		GmPlayerResponse_77000022.Builder response = GmPlayerResponse_77000022.newBuilder();
		String channel = request.getChannel();
		String name = request.getName();
		long pid = 0L;
		if (!request.getPlayerIdBytes().isEmpty()) {
			try {
				pid = Long.parseLong(request.getPlayerId().trim());
			} catch (Exception e) {
				log.warn("0x77 playerInfo playerId 非法: {}", request.getPlayerId(), e);
				sendAndRecordOpt(client, request, response.build(), ErrorMsgEnum.request_parameter_error, "查询玩家ID非法");
				return;
			}
		}
		SimplePlayer searchPlayer = searchPlayerForInfo(name, pid);
		if (searchPlayer != null) {
			response.setPlayer(searchPlayer.toGmPlayerInfo());
		}
		sendAndRecordOpt(client, request, response.build(), "查询玩家");
	}

	/**
	 * 查询玩家简略信息（预留可测试覆写钩子，默认调用PlayerHelper.searchPlayer）
	 *
	 * @param name 玩家名称
	 * @param playerId 玩家ID
	 * @return 简略玩家信息
	 * @author shuyongqiang
	 */
	protected SimplePlayer searchPlayerForInfo(String name, long playerId) {
		return PlayerHelper.searchPlayer(name, playerId);
	}

	private void sendAndRecordOpt(NetClient client, Message request, Message response, String optMsg) {
		sendAndRecordOpt(client, request, response, null, optMsg);
	}

	/**
	 * 发送协议并记录操作日志。 如果提供了错误消息枚举，则发送带有错误ID的响应协议； 否则，只发送响应协议。 然后创建一个操作日志对象，设置相关属性，并将其插入数据库。
	 *
	 * @param client 网络客户端实例
	 * @param request 发送的请求消息
	 * @param response 接收到的响应消息
	 * @param errMsg 错误消息枚举，如果为null则表示没有错误
	 * @param optMsg 操作消息，可以为null
	 */
	private void sendAndRecordOpt(NetClient client, Message request, Message response, ErrorMsgEnum errMsg, String optMsg) {
		if (errMsg != null) {
			client.sendProtocol(response, errMsg.getId());
		} else {
			client.sendProtocol(response);
		}
		String responseStr = TextFormat.printer().escapingNonAscii(false).printToString(response);
		String result = errMsg == null ? responseStr : errMsg.getDesc();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		if (VxHolder.vertx == null || player == null || player.getAccount() == null) {
			return;
		}
		String requestStr = TextFormat.printer().escapingNonAscii(false).printToString(request);
		ServerMsg.GmOptRecordRequest_7d000052.Builder req = ServerMsg.GmOptRecordRequest_7d000052.newBuilder();
		req.setOptmsg(optMsg == null ? "null" : optMsg)
				.setOptParam(requestStr)
				.setOptPid(player.getAccount().accountId + ":" + client.getPlayerId() + ":" + player.getData().getName())
				.setOptResult(result);
		VxHolder.requestRemoteServer(ServerType.Login, req.build()).onComplete(r -> {
		}).onFailure(e -> log.error("记录0x77 GM操作日志失败", e));
	}

	private void shutdown(NetClient client, Object message) {
		CompletableFuture.runAsync(() -> {
			log.warn("收到0x77 GM停服指令: operatorPlayerId={}, runMode={}", client != null ? client.getPlayerId() : 0L, GmSecurityPolicy.currentRunMode());
			System.exit(0);
		});
	}

	/** 封号列表 */
	private void forbidAccountList(NetClient client, Object message) {
		GmMsg.GmAccountForbidListRequest_77000003 request = (GmMsg.GmAccountForbidListRequest_77000003) message;
		GmAccountForbidListResponse_77000004.Builder response = GmAccountForbidListResponse_77000004.newBuilder();
		List<ForbidAccount> accounts = PlayerManager.getInstance()
				.getForbidAccount()
				.stream()
				.filter(forbidAccount -> forbidAccount.getType() == request.getType())
				.collect(Collectors.toList());
		response.addAllAccounts(PbBuilder.buildForbidAccount(accounts));
		client.sendProtocol(response);
	}

	/** 封号 */
	private void forbidAccount(NetClient client, Object message) {
		GmAccountForbidRequest_77000005 request = (GmAccountForbidRequest_77000005) message;
		GmAccountForbidResponse_77000006.Builder response = GmAccountForbidResponse_77000006.newBuilder();
		String reason = request.getReason();
		int type = request.getType();
		long unblockTime;
		if (request.getEndTime() < 0) {// 永久封号，封禁100年
			unblockTime = DateUtil.DAY_SECONDS * 365 * 100;
		} else {
			unblockTime = request.getEndTime();
		}
		if (request.getPlayerIdCount() <= 0) {
			sendAndRecordOpt(client, request, response.build(), ErrorMsgEnum.request_parameter_null, "封号");
			return;
		}
		List<Long> targetPids = new ArrayList<>(request.getPlayerIdCount());
		for (String rawPlayerId : request.getPlayerIdList()) {
			if (StringUtils.isEmpty(rawPlayerId)) {
				sendAndRecordOpt(client, request, response.build(), ErrorMsgEnum.request_parameter_error, "封号");
				return;
			}
			try {
				targetPids.add(Long.parseLong(rawPlayerId.trim()));
			} catch (NumberFormatException e) {
				log.warn("0x77 forbidAccount playerId 非法: {}", rawPlayerId, e);
				sendAndRecordOpt(client, request, response.build(), ErrorMsgEnum.request_parameter_error, "封号");
				return;
			}
		}
		List<Long> pids = new ArrayList<>();
		VxHolder.vertx.executeBlocking(() -> {
			boolean allSucceeded = true;
			for (long playerId : targetPids) {
				ForbidAccount forbidAccount = PlayerManager.getInstance()
						.forbidAccount(playerId, reason, unblockTime * 1000L + "", type);
				if (forbidAccount != null) {
					pids.add(forbidAccount.getPlayerId());
					if (request.getEndTime() < 0) {// 希望修复永久封停不可用问题，同时对账号附带清榜效果
						RankService.getInstance().removeRankAsync(playerId);
					}
				} else {
					allSucceeded = false;
				}
				if (PlayerManager.getInstance().isForbidAccount(playerId)) {
					Player optPlayer = PlayerManager.getInstance().getPlayer(playerId);
					if (optPlayer != null) {
						GameClientManager.getInstance().logout(playerId, LogoutType.GMKick);
					}
				}
			}
			// 通知其他game节点添加封号记录
			if (!pids.isEmpty()) {
				VxHolder.broadcastRemoteServer(ServerType.Game,
						ServerMsg.NotifyGmAddForbidAccountRequest_7d000054.newBuilder()
								.setReason(request.getReason())
								.setTimer(unblockTime * 1000L)
								.addAllPids(pids)
								.setType(request.getType())
								.build());
			}
			return allSucceeded;
		}).onSuccess(allSucceeded -> {
			if (Boolean.TRUE.equals(allSucceeded)) {
				sendAndRecordOpt(client, request, response.build(), "封号");
			} else {
				sendAndRecordOpt(client, request, response.build(), ErrorMsgEnum.unknown, reason);
			}
		}).onFailure(e -> {
			log.error("0x77 forbidAccount 执行失败: pids={}", targetPids, e);
			sendAndRecordOpt(client, request, response.build(), ErrorMsgEnum.unknown, reason);
		});
	}

	/** 解封账号 */
	private void unblockAccount(NetClient client, Object message) {
		GmAccountUnblockRequest_77000007 request = (GmAccountUnblockRequest_77000007) message;
		GmAccountUnblockResponse_77000008.Builder response = GmAccountUnblockResponse_77000008.newBuilder();
		if (request.getPlayerIdCount() <= 0) {
			sendAndRecordOpt(client, request, response.build(), ErrorMsgEnum.request_parameter_null, "解封账号");
			return;
		}
		List<Long> pids = new ArrayList<>(request.getPlayerIdCount());
		for (String rawPlayerId : request.getPlayerIdList()) {
			if (StringUtils.isEmpty(rawPlayerId)) {
				sendAndRecordOpt(client, request, response.build(), ErrorMsgEnum.request_parameter_error, "解封账号");
				return;
			}
			try {
				pids.add(Long.parseLong(rawPlayerId.trim()));
			} catch (NumberFormatException e) {
				log.warn("0x77 unblockAccount playerId 非法: {}", rawPlayerId, e);
				sendAndRecordOpt(client, request, response.build(), ErrorMsgEnum.request_parameter_error, "解封账号");
				return;
			}
		}
		for (long pid : pids) {
			PlayerManager.getInstance().unblockAccount(pid);
		}
		// 通知其他game节点删除封号记录
		if (!pids.isEmpty()) {
			VxHolder.broadcastRemoteServer(ServerType.Game,
					ServerMsg.NotifyGmDelForbidAccountRequest_7d000056.newBuilder().addAllPids(pids).build());
		}
		sendAndRecordOpt(client, request, response.build(), "解封账号");
	}

	/** 踢玩家下线 */
	private void playerLogout(NetClient client, Object message) {
		GmPlayerLogoutRequest_77000009 request = (GmPlayerLogoutRequest_77000009) message;
		GmPlayerLogoutResponse_7700000a.Builder response = GmPlayerLogoutResponse_7700000a.newBuilder();

		if (StringUtils.isEmpty(request.getPlayerId())) {
			client.sendProtocol(response, ErrorMsgEnum.request_parameter_null.ID);
			return;
		}
		long playerId;
		try {
			playerId = Long.parseLong(request.getPlayerId().trim());
		} catch (NumberFormatException e) {
			log.warn("0x77 playerLogout playerId 非法: {}", request.getPlayerId(), e);
			client.sendProtocol(response, ErrorMsgEnum.request_parameter_error.ID);
			return;
		}
		if (playerId <= 0) {
			client.sendProtocol(response, ErrorMsgEnum.request_parameter_error.ID);
			return;
		}
		PlayerHelper.submitRecovery(playerId, () -> GameClientManager.getInstance().logout(playerId, LogoutType.GMKick))
                .onSuccess(result -> client.sendProtocol(response))
                .onFailure(failure -> { log.warn("GM退出保存失败 playerId={}", playerId, failure);
                    client.sendProtocol(response, ErrorMsgEnum.unknown.ID); });
	}
}
