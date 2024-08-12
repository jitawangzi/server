package cn.game.games.net.game.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.task.TaskManager;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.GameServerStatus;
import cn.game.games.core.SimplePlayer;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.exception.LogicException;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.player.VarConstant;
import cn.game.games.util.AddressUtil;
import cn.game.games.util.DAO;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.RandomNameConfig;
import cn.game.protocol.generated.manager.HeadBoxManager;
import cn.game.protocol.generated.manager.HeadPortraitManager;
import cn.game.protocol.generated.manager.RandomNameManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.protocol.protobuf.PlayerMsg.PlayerBriefInfoOtherRequest_01000009;
import cn.game.protocol.protobuf.PlayerMsg.PlayerBriefInfoOtherResponse_0100000a;
import cn.game.protocol.protobuf.PlayerMsg.PlayerBriefInfoRequest_01000007;
import cn.game.protocol.protobuf.PlayerMsg.PlayerBriefInfoResponse_01000008;
import cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxRequest_01000042;
import cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxResponse_01000043;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import cn.game.protocol.protobuf.PlayerMsg.PlayerGenderRequest_01000017;
import cn.game.protocol.protobuf.PlayerMsg.PlayerGenderResponse_01000018;
import cn.game.protocol.protobuf.PlayerMsg.PlayerGuideRequest_01000060;
import cn.game.protocol.protobuf.PlayerMsg.PlayerGuideResponse_01000061;
import cn.game.protocol.protobuf.PlayerMsg.PlayerHeadFrameRequest_01000015;
import cn.game.protocol.protobuf.PlayerMsg.PlayerHeadFrameResponse_01000016;
import cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013;
import cn.game.protocol.protobuf.PlayerMsg.PlayerHeadResponse_01000014;
import cn.game.protocol.protobuf.PlayerMsg.PlayerHeartbeatResponse_01000006;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLoginRequest_01000001;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLoginResponse_01000002;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLogoutResponse_01000004;
import cn.game.protocol.protobuf.PlayerMsg.PlayerNameRequest_01000011;
import cn.game.protocol.protobuf.PlayerMsg.PlayerNameResponse_01000012;
import cn.game.protocol.protobuf.PlayerMsg.PlayerPatrolInfoRequest_01000070;
import cn.game.protocol.protobuf.PlayerMsg.PlayerPatrolInfoResponse_01000071;
import cn.game.protocol.protobuf.PlayerMsg.PlayerReconnecRequest_01000065;
import cn.game.protocol.protobuf.PlayerMsg.PlayerReconnecResponse_01000066;
import cn.game.protocol.protobuf.PlayerMsg.PlayerShowRequest_01000039;
import cn.game.protocol.protobuf.PlayerMsg.PlayerShowResponse_0100003a;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.ServerMsg.GamePlayerLogoutRequest_7d000101;
import cn.game.protocol.protobuf.ServerMsg.LoginPlayerUidRequest_7d000018;
import cn.game.protocol.protobuf.ServerMsg.LoginPlayerUidResponse_7d000019;
import cn.game.util.ConversionUtil;
import cn.game.util.DateUtil;
import cn.game.util.ObjUtil;
import cn.game.util.RedissonUtil;
import cn.game.util.Rnd;
import cn.game.util.ServerType;
import cn.game.util.TreeWordFilter;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;

/**
 * 用户处理器
 */
@Component
public class PlayerHandler extends BaseHandler {

	private static final Logger levellog = LoggerFactory.getLogger("levelLog");
	private static final Logger loginlog = LoggerFactory.getLogger("loginLog");

	@Override
	protected int getModule() {
		return 0x01;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.PlayerLoginRequest_01000001, this::login);
		putInvoker(PbProtocol.PlayerLogoutRequest_01000003, this::logout);
//		putInvoker(PbProtocol.PlayerCreateRequest_01000003, this::createPlayer);
		putInvoker(PbProtocol.PlayerHeartbeatRequest_01000005, this::heartbeat);
//		putInvoker(PbProtocol.PlayerPowerRequest_01000020, this::ap);
		putInvoker(PbProtocol.PlayerBriefInfoRequest_01000007, this::getPlayerBriefInfo);
		putInvoker(PbProtocol.PlayerBriefInfoOtherRequest_01000009, this::getPlayerOtherBriefInfo);
		putInvoker(PbProtocol.PlayerShowRequest_01000039, this::show);
//		putInvoker(PbProtocol.PlayerSpiritReceiveRequest_01000024, this::spiritReceive);
//		putInvoker(PbProtocol.BuffAddRequest_01000110, this::addBuff);

//		putInvoker(PbProtocol.ServerPlayerLoginRequest_01000001_01000051, this::pcLogin);
//		putInvoker(PbProtocol.PlayerCreateRequest_01000053, this::pcCreate);
//		putInvoker(PbProtocol.PlayerPlayerLoginRequest_01000001_01000055, this::pcChoose);
		putInvoker(PbProtocol.PlayerReconnecRequest_01000065, this::reconnect);
		putInvoker(PbProtocol.PlayerNameRequest_01000011, this::rename);
		putInvoker(PbProtocol.PlayerHeadRequest_01000013, this::head);
		putInvoker(PbProtocol.PlayerHeadFrameRequest_01000015, this::headFrame);
		putInvoker(PbProtocol.PlayerGenderRequest_01000017, this::gender);
//		putInvoker(PbProtocol.ItemUseRequest_01000050, this::useItem);
		putInvoker(PbProtocol.PlayerCloudBoxRequest_01000042, this::cloudBox);
		putInvoker(PbProtocol.PlayerGuideRequest_01000060, this::guide);
		putInvoker(PbProtocol.PlayerPatrolInfoRequest_01000070, this::patrolInfo);
//		putInvoker(PbProtocol.PlayerDeleteRequest_01000070, this::delete);
	}

	private void guide(NetClient client, Object message) {
		PlayerGuideRequest_01000060 request = (PlayerGuideRequest_01000060) message;
		PlayerGuideResponse_01000061.Builder resp = PlayerGuideResponse_01000061.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		PlayerModule playerModule = player.getPlayerModule();
		Map<Integer, Integer> guideMap = playerModule.getGuideMap();
		guideMap.put(request.getType(), request.getStep());
		client.sendProtocol(resp);
	}

	private void patrolInfo(NetClient client, Object message) {
		PlayerPatrolInfoRequest_01000070 request = (PlayerPatrolInfoRequest_01000070) message;
		PlayerPatrolInfoResponse_01000071.Builder resp = PlayerPatrolInfoResponse_01000071.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		ChapterModule chapterModule = player.getChapterModule();
		resp.setPatrol(chapterModule.buildPatrolInfo());
		client.sendProtocol(resp);
	}
	private void cloudBox(NetClient client, Object message) {
		PlayerCloudBoxRequest_01000042 request = (PlayerCloudBoxRequest_01000042) message;
		PlayerCloudBoxResponse_01000043.Builder resp = PlayerCloudBoxResponse_01000043.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		PlayerModule playerModule = player.getPlayerModule();
		List<Goods> cloudBox = playerModule.getCloudBox();
		if (cloudBox == null || cloudBox.isEmpty()) {
			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		List<RewardInfo> goods = PlayerHelper.addGoods(player, cloudBox, OpType.CloudBox);
		resp.addAllRewards(goods);
		client.sendProtocol(resp);
		playerModule.setCloudBox(null);
	}

//	private void useItem(NetClient client, Object message) {
//		ItemUseRequest_01000050 request = (ItemUseRequest_01000050) message;
//		ItemUseResponse_01000051.Builder resp = ItemUseResponse_01000051.newBuilder();
//		int id = request.getId();
//		int param = request.getParam();
//
//		ItemConfig item = ItemManager.instance().get(id);
//		if (item == null) {
//			client.sendProtocol(resp, ErrorMsgEnum.config_data_not_found.getId());
//			return;
//		}
//		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
//		ItemModule itemModule = player.getItemModule();
//		if (!itemModule.isEnough(id)) {
//			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
//			return;
//		}
//		List<RewardInfo> ret = new ArrayList<>();
//		if (item.ItemType == 4) {
//			HeroConfig heroConfig = HeroManager.instance().get(param);
//			if (heroConfig.InitialQuality != item.Para) {
//				client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.getId());
//				return;
//			}
////			List<RewardInfo> reward = player.getHeroModule().addReward(param, 1, OpType.None);
////			ret.addAll(reward);
//		} else {
//			client.sendProtocol(resp, ErrorMsgEnum.player_check_error.getId());
//			return;
//		}
//
//		itemModule.del(id, 1);
//
//		resp.addAllReward(ret);
//		client.sendProtocol(resp);
//	}

//	private void delete(NetClient client, Object message) {
//		PlayerDeleteRequest_01000070 req = (PlayerDeleteRequest_01000070) message;
//		PlayerDeleteResponse_01000071.Builder resp = PlayerDeleteResponse_01000071.newBuilder();
//		long playerId = req.getId();
//		boolean hasCache = PlayerManager.getInstance().hasCache(playerId);
//		if (hasCache) {
//			PlayerManager.getInstance().logoutCache(playerId, false);
//		}
//		// TODO 暂时删player表，后续删除其他表
//		DAO.delete(PlayerMapper.class, playerId);
//		client.sendProtocol(resp.build());
//	}

//	private void pcLogin(NetClient client, Object message) {
//		ServerPlayerLoginRequest_01000001_01000051 req = (ServerPlayerLoginRequest_01000001_01000051) message;
//		String passportSessionId = req.getSessionId();
//		ServerLoginResponse_01000052.Builder resp = ServerLoginResponse_01000052.newBuilder();
//
//		GameClient oldGameClient = GameClientManager.getInstance().getGameClient(passportSessionId);
//		GameClient newGameClient = (GameClient) client;
//		// 防止前端登录两次,只用新sessionId
//		String sessionId2 = newGameClient.getSessionId();
//		if (sessionId2 != null) {
//			GameClientManager.getInstance().removeGameClientSession(newGameClient);
//		}
//		newGameClient.setSessionId(passportSessionId);
//
//		if (oldGameClient != null) {// 在线，重连,还是老的sessionId
//			newGameClient.copyClintLoign(oldGameClient);
//			GameClientManager.getInstance().removeGameClient(oldGameClient);
//		}
//		GameClientManager.getInstance().addGameClientSession(newGameClient);
//
//		Future<Long> uidFuture = GameServer.getInstance().getLoginGameServerInterface()
//				.getUid2(passportSessionId);
//		uidFuture.compose(uid -> GameServer.getInstance().getDataGameServerInterface().execAsync(PlayerMapper.class,
//				"selectPlayersByUid", uid)).onSuccess(p -> {
//					List<Player> list = (List<Player>) p;
//					resp.addAllArchives(PbBuilder.buildPlayerArchiveInfos(list));
//					resp.setTime(System.currentTimeMillis() + "");
//					client.sendProtocol(resp.build());
//
//				}).onFailure(p -> {
//					log.error("pc player session  " + passportSessionId + " login error ", p);
//					client.sendProtocol(resp.build(), ErrorMsgEnum.unknown.getId());
//				});
//
//	}

//	private void pcCreate(NetClient client, Object message) {
//		PlayerCreateRequest_01000053 req = (PlayerCreateRequest_01000053) message;
//		PlayerCreateResponse_01000054.Builder resp = PlayerCreateResponse_01000054.newBuilder();
//		String sessionId = req.getSessionId();
//		String name = req.getName();
//
//		Future<Long> uidFuture = GameServer.getInstance().getLoginGameServerInterface()
//				.getUid2(sessionId);
//		uidFuture.compose(uid -> GameServer.getInstance().getDataGameServerInterface().execAsync(PlayerMapper.class,
//				"selectPlayersByUid", uid)).onSuccess(p -> {
//					List<Player> list = (List<Player>) p;
//					for (Player player : list) {
//						if (player.getData().getName().equalsIgnoreCase(name)) {
//							client.sendProtocol(resp.build(), ErrorMsgEnum.player_name_repeat.getId());
//							return;
//						}
//					}
//					createPlayer(client, message);
//
//				}).onFailure(p -> {
//					log.error("pc player session  " + sessionId + " login error ", p);
//					client.sendProtocol(resp.build(), ErrorMsgEnum.unknown.getId());
//				});
//
//	}

//	private void pcChoose(NetClient client, Object message) {
//		PlayerPlayerLoginRequest_01000001_01000055 req = (PlayerPlayerLoginRequest_01000001_01000055) message;
//		PlayerLoginResponse_01000056.Builder resp = PlayerLoginResponse_01000056.newBuilder();
//		String passportSessionId = req.getSessionId();
//		long id = req.getId();
//		boolean hasCache = PlayerManager.getInstance().hasCache(id);
//		client.setPlayerId(id);
//		if (hasCache) {
//
//			Player player = PlayerManager.getInstance().getPlayer(id);
////			player.getData().setLoginDate(DateUtil.getStringDate());
////			DAO.update(PlayerMapper.class, player);
//
//			resp.setPlayerInfo(PbBuilder.buildPlayerInfo(player));
//			resp.setConfigFileVersion(PlayerHelper.getServerConfigVersion());
//
//			client.sendProtocol(resp.build());
//			GameClientManager.getInstance().addGameClientPlayer((GameClient) client);
//			return;
//		}
//
//		// 查看有没有其他的存档，如果有的话退出。
//		Future<?> loginFutrue = loginFutrue(client, passportSessionId, id);
//
//		Future<Long> uidFuture = GameServer.getInstance().getLoginGameServerInterface()
//				.getUid2(passportSessionId);
//		uidFuture.compose(uid -> GameServer.getInstance().getDataGameServerInterface().execAsync(PlayerMapper.class,
//				"selectPlayersByUid", uid)).onSuccess(p -> {
//					List<Player> list = (List<Player>) p;
//					for (Player player : list) {
//						if (player.getData().getPlayerId() != id) {
//							if (PlayerManager.getInstance().hasCache(player.getData().getPlayerId())) {
//								PlayerManager.getInstance().logoutCache(player.getData().getPlayerId());
//							}
//						}
//					}
//				}).compose(u -> loginFutrue);
//	}

	private void reconnect(NetClient client, Object message) {
		PlayerReconnecRequest_01000065 req = (PlayerReconnecRequest_01000065) message;
		PlayerReconnecResponse_01000066.Builder resp = PlayerReconnecResponse_01000066.newBuilder();
		String passportSessionId = req.getSessionId();
		long id = req.getPlayerId();
//		boolean hasCache = PlayerManager.getInstance().hasCache(id); 
//		if (hasCache) {
//			Player player = PlayerManager.getInstance().getPlayer(id); 
//			resp.setPlayerInfo(PbBuilder.buildPlayerInfo(player)) ; 
//			resp.setTime(System.currentTimeMillis()+"") ; 
//		}
		GameClient oldGameClient = GameClientManager.getInstance().getGameClient(passportSessionId);
		GameClient newGameClient = (GameClient) client;
		int errorCode = 0;
		if (oldGameClient != null && oldGameClient.getPlayerId() > 0) {// 在线，重连,还是老的sessionId
			newGameClient.setSessionId(passportSessionId);
			newGameClient.copy(oldGameClient);

			GameClientManager.getInstance().removeGameClient(oldGameClient);

			GameClientManager.getInstance().addGameClientSession(newGameClient);
			GameClientManager.getInstance().addGameClientPlayer(newGameClient);

			Player p = PlayerManager.getInstance().getPlayer(newGameClient.getPlayerId());
			PlayerHelper.refresh(p);
//			p.setLoginDate(DateUtil.getStringDate());
//			DAO.update(PlayerMapper.class, p);
			resp.setPlayerInfo(PbBuilder.buildPlayerInfo(p));
			resp.setTime(System.currentTimeMillis() + "");
		} else {
			errorCode = ErrorMsgEnum.reconnect_fail.getId();
		}
		client.sendProtocol(resp.build(), errorCode);
	}

	/**
	 * 添加事件buff
	 * 
	 * @param netClient
	 * @param message
	 */
	/*private void addBuff(NetClient netClient, Object message) {
		BuffMsg.BuffAddRequest_01000110 req = (BuffMsg.BuffAddRequest_01000110) message;
		BuffMsg.BuffAddResponse_01000111.Builder resp = BuffMsg.BuffAddResponse_01000111.newBuilder();
		long playerId = netClient.getPlayerId();
		int id = req.getId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		EventOptionConfig config = EventOptionManager.getInstance().getEventOptionConfig(id);
		int eventId = config.getEventId();
				PlayerExt playerExt = player.getExt();
				List<Integer> eventIdList = playerExt.getEventIdList();
				if (!eventIdList.contains(eventId)) {
					netClient.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
					return;
				}
		//添加buff
		List<Buff> buffs = PlayerHelper.chooseEventOption(player, eventId, id, true);
		if (buffs == null) {
			netClient.sendProtocol(resp, ErrorMsgEnum.resource_not_enough.getId());
			return;
		}
				playerExt.removeEventId(eventId);
				PlayerExt update = PlayerExt.valueOf(playerId);
				update.setEventIds(playerExt.getEventIds());
				DAO.updateSelective(update);
		// 添加事件
		Map<Integer, Integer> addResources = new HashMap<>();
		for (Buff buff : buffs) {
			OldBuffConfig buffConfig = OldBuffManager.getInstance().getBuffConfig(buff.getBuffId());
			EffectEnum effectType = buffConfig.getEffectType();
			if (effectType == EffectEnum.AddOrDelGoods) {
				addResources.put(buffConfig.getIdParam(), buffConfig.getNumParam());
			}
		}
		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.ExploreGetResources, addResources));
	
		netClient.sendProtocol(resp);
	}*/
/**
	private void spiritReceive(NetClient client, Object message) {
		PlayerSpiritReceiveResponse_01000025.Builder resp = PlayerSpiritReceiveResponse_01000025.newBuilder();
		long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		// 体力领取时间段
		List<String> spiritReceiveTime = GlobalConst.spiritReceiveTime;
		boolean ret = false;
		for (int i = 0; i < spiritReceiveTime.size(); i++) {
			String[] split = spiritReceiveTime.get(i).split("~");
			if (split == null || split.length < 2) {
				client.sendProtocol(resp, ErrorMsgEnum.config_data_not_found.getId());
				return;
			}
			LocalTime localDate = LocalTime.now();
			LocalTime start = LocalTime.of(Integer.parseInt(split[0].split(":")[0]),
					Integer.parseInt(split[0].split(":")[1]), 0);
			LocalTime end = LocalTime.of(Integer.parseInt(split[1].split(":")[0]),
					Integer.parseInt(split[1].split(":")[1]), 0);
			if (localDate.isAfter(start) && localDate.isBefore(end)) {
				ret = true;
				boolean one = ByteHelp.isOne(player.getData().getSpiritReceiveInfo(), i);
				if (one) {
					client.sendProtocol(resp, ErrorMsgEnum.reward_have_received.getId());
					return;
				}
				// 领取体力
				player.getData().setSpiritReceiveInfo((byte) ByteHelp.modifyBit(player.getData().getSpiritReceiveInfo(), i));
				PlayerHelper.addResources(player.getData().getPlayerId(), ResourceEnum.Brawn.getId(), GlobalConst.spiritReceive);
				client.sendProtocol(resp);
			}

		}
		if (!ret) {
			client.sendProtocol(resp, ErrorMsgEnum.spirit_receivetime_notfit.getId());
			return;
		}
	}
	*/

	protected void heartbeat(NetClient client, Object message) {

		PlayerHeartbeatResponse_01000006.Builder resp = PlayerHeartbeatResponse_01000006.newBuilder();
		resp.setTime(System.currentTimeMillis() + "");
		client.sendProtocol(resp.build());
	}

	protected void getPlayerBriefInfo(NetClient client, Object message) {
		PlayerBriefInfoRequest_01000007 request = (PlayerBriefInfoRequest_01000007) message;
		List<Long> playerIds = ConversionUtil.toLongList(request.getPlayerIdsList());

		TaskManager.getInstance().addWorkerTask(() -> {
			PlayerBriefInfoResponse_01000008.Builder response = PlayerBriefInfoResponse_01000008.newBuilder();
			List<SimplePlayer> sPlayerInfos = PlayerManager.getInstance().getAndLoadSimplePlayers(playerIds);
			// 发送协议
			response.addAllPlayers(PbBuilder.buildSimplePlayerInfos(sPlayerInfos));
			client.sendProtocol(response);
		});
	}

	protected void getPlayerOtherBriefInfo(NetClient client, Object message) {
		PlayerBriefInfoOtherRequest_01000009 request = (PlayerBriefInfoOtherRequest_01000009) message;
		List<String> playerStringIds = request.getPlayerIdsList();
		List<String> servers = new ArrayList<>(request.getServerIdsList());
		List<Long> playerIds = ConversionUtil.toLongList(playerStringIds);
		TaskManager.getInstance().addWorkerTask(() -> {
			PlayerBriefInfoOtherResponse_0100000a.Builder response = PlayerBriefInfoOtherResponse_0100000a.newBuilder();
			int error = 0;
			try {
				List<SimplePlayer> sPlayerInfos = PlayerManager.getInstance().getAndLoadSimplePlayers(playerIds,
						servers);
				response.addAllPlayers(PbBuilder.buildSimplePlayerInfos(sPlayerInfos));
			} catch (Exception e) {
				e.printStackTrace();
				error = ErrorMsgEnum.player_not_found.getId();
			}
			client.sendProtocol(response.build(), error);
		});
	}

	protected void show(NetClient client, Object message) {
		PlayerShowRequest_01000039 request = (PlayerShowRequest_01000039) message;
		String serverId = request.getServerId();
		long id = Long.parseLong(request.getPlayerId());
		TaskManager.getInstance().addWorkerTask(() -> {
			PlayerShowResponse_0100003a.Builder response = PlayerShowResponse_0100003a.newBuilder();
			int error = 0;
			try {
				SimplePlayer simplePlayer = PlayerManager.getInstance().getAndLoadSimplePlayer(id, serverId);
				response.setPlayer(PbBuilder.buildPlayerShowInfo(simplePlayer));
			} catch (Exception e) {
				e.printStackTrace();
				error = ErrorMsgEnum.player_not_found.getId();
			}
			client.sendProtocol(response.build(), error);
		});
	}

	protected void head(NetClient client, Object message) {

		PlayerHeadRequest_01000013 request = (PlayerHeadRequest_01000013) message;
		PlayerHeadResponse_01000014.Builder resp = PlayerHeadResponse_01000014.newBuilder();
		int head = request.getHead();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		player.getData().setHead(head);
		client.sendProtocol(resp);
	}

	protected void headFrame(NetClient client, Object message) {

		PlayerHeadFrameRequest_01000015 request = (PlayerHeadFrameRequest_01000015) message;
		PlayerHeadFrameResponse_01000016.Builder resp = PlayerHeadFrameResponse_01000016.newBuilder();
		int headFrame = request.getHeadFrame();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		player.getData().setHeadFrame(headFrame);
		client.sendProtocol(resp);
	}
	protected void rename(NetClient client, Object message) {

		PlayerNameRequest_01000011 request = (PlayerNameRequest_01000011) message;
		PlayerNameResponse_01000012.Builder resp = PlayerNameResponse_01000012.newBuilder();
		String newName = request.getName();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		String oldName = player.getData().getName();
		if (StringUtils.isEmpty(newName)) {
			client.sendProtocol(resp, ErrorMsgEnum.unknown.getId());
			return;
		}
		int var = player.getVarModule().getVar(VarConstant.RANAME_COUNT);
		if (var > 0) {
			// 检查消耗的资源TODO
//			player.isEnough(var, var); 
		}

		boolean check = TreeWordFilter.check(newName);
		if (!check) {
			client.sendProtocol(resp, ErrorMsgEnum.player_name_illegal.getId());
			return;
		}
//		Player ofName = PlayerManager.getInstance().getOfName(newName);
//		if (ofName != null) {
//			client.sendProtocol(resp, ErrorMsgEnum.player_name_repeat.getId());
//			return;
//		}
		if (var == 0) {
			player.getVarModule().incrVar(VarConstant.RANAME_COUNT);
		}
		player.getData().setName(newName);
		client.sendProtocol(resp);
	}

	protected void gender(NetClient client, Object message) {

		PlayerGenderRequest_01000017 request = (PlayerGenderRequest_01000017) message;
		PlayerGenderResponse_01000018.Builder resp = PlayerGenderResponse_01000018.newBuilder();
		boolean isMan = request.getIsMan();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		int var = player.getVarModule().getVar(VarConstant.GENDER_COUNT);
		if (var > 0) {
			// 检查消耗的资源TODO
//			player.isEnough(var, var); 
		}
		if (var == 0) {
			player.getVarModule().incrVar(VarConstant.GENDER_COUNT);
		}
		player.getData().setGender(isMan);
		client.sendProtocol(resp);
	}
	protected void login(NetClient client, Object message) {

		PlayerMsg.PlayerLoginRequest_01000001 req = (PlayerLoginRequest_01000001) message;
		String passportSessionId = req.getSessionId();
//		String serverId = req.getServerId();
		boolean reconnect = req.getReconnect();
		log.info("passportSessionId : " + passportSessionId + " start login");
//		if (true) {
//			client.sendProtocol(PlayerLoginResponse_01000002.newBuilder()
//					.setTime(System.currentTimeMillis() + "张三*#@sd+李四"));
//			return;
//		}
		Handler<Integer> failHandler = errorCode -> {
			log.error("player session  " + passportSessionId + " login error ");
			client.sendProtocol(PlayerLoginResponse_01000002.getDefaultInstance(), errorCode);
		} ; 
		int canLogin = GameServerStatus.getInstance().canLogin(req.getVerstion());
		if (canLogin > 0) {
//			failHandler.handle(canLogin);
//			return;
		}
		GameClient oldGameClient = GameClientManager.getInstance().getGameClient(passportSessionId);
		GameClient newGameClient = (GameClient) client;
		newGameClient.setSessionId(passportSessionId);
		boolean isReconnect = PlayerHelper.reconnect(newGameClient, reconnect, oldGameClient == null ? 0 : oldGameClient.getPlayerId());
		if (isReconnect) {
			return;
		}
		if (reconnect) {
			client.sendProtocol(PlayerLoginResponse_01000002.getDefaultInstance(), ErrorMsgEnum.reconnect_fail.getId());
			GameClientManager.getInstance().removeGameClient(newGameClient);
			return;
		}
		// 新session,重新登陆
		final AtomicLong uid = new AtomicLong();
		StringBuffer accountId = new StringBuffer();
		StringBuffer deviceId = new StringBuffer();
		Future<Message<LoginPlayerUidResponse_7d000019>> uidFuture = VxHolder.requestRemoteServer(
				ServerType.Login,
				LoginPlayerUidRequest_7d000018.newBuilder().setPassportSessionId(passportSessionId).build());
		uidFuture.compose(r -> {
			uid.set(r.body().getUid());
			accountId.append(r.body().getAccountId());
			deviceId.append(r.body().getDeviceId());
			return DAO.execute(PlayerDataMapper.class,
					MapperConstant.selectByPrimaryKey, uid.get());

		}).onSuccess(p -> {
			if (p == null) {
				// 创建角色
				RFuture<Boolean> playerLockFuture = PlayerHelper.trySetServerId(uid.get());
				playerLockFuture.onComplete((v,throwable) -> {
					if (v) {
						Account account = new Account(req);
						account.accountId = accountId.toString();
						account.deviceId = deviceId.toString();

						createPlayer(account, client, uid.longValue(), null, true, 0, false, true);
					}else {
						failHandler.handle(ErrorMsgEnum.player_lock.getId());
						log.error("create player error  ", throwable);
					}
				});
				return;

			} // 老账号登陆
			PlayerData dbPlayer = (PlayerData) p;
			long playerId = dbPlayer.getPlayerId();
			client.setPlayerId(playerId);

			// 被封账号,限制登录
			boolean checkUnlock = PlayerManager.getInstance().checkUnlock(playerId);
			if (!checkUnlock) {
				client.sendProtocol(PlayerLoginResponse_01000002.getDefaultInstance(),
						ErrorMsgEnum.login_forbidden.getId());
				GameClientManager.getInstance().removeGameClient(newGameClient);
				return;
			}

			if (PlayerHelper.reconnect(newGameClient, reconnect, playerId)) {
				return;
			}

			// 看看在没在其他服务器
			RFuture<String> serverIdFutrue = RedissonUtil.getAsync(CacheType.PLAYER_SERVER_ID.key(uid.get()));
			serverIdFutrue.onComplete((serverId, e) -> {
				if (e != null) {
					failHandler.handle(ErrorMsgEnum.redis_fail.getId());
					return;
				}
				// 在其他服务器，通知其他服务器退出
				if (serverId != null && !serverId.equalsIgnoreCase(ServerContext.getInstance().getServerId())) {
					Future<Message<Object>> requestRemoteServer = VxHolder.requestRemoteServer(serverId,
							GamePlayerLogoutRequest_7d000101.newBuilder().setPlayerId(uid.get()).build());
					requestRemoteServer.onFailure(ee -> {
						log.error("player login , request to server : " + serverId + " failed ", ee);
						failHandler.handle(ErrorMsgEnum.request_remote_server.getId());
					}).onSuccess(r -> {
						Account account = new Account(req);
						// load from db
						PlayerHelper.startLoadPlayerFromDb(newGameClient, dbPlayer, account);
					});
				} else {
					// 玩家没在其他服务器，在本服务器加载数据
					Account account = new Account(req);
					// load from db
					PlayerHelper.startLoadPlayerFromDb(newGameClient, dbPlayer, account);
				}
			});
		}).onFailure(p -> {
			int failCode = 1;
			if (p instanceof ReplyException) {
				failCode = ((ReplyException) p).failureCode();
			}
			failHandler.handle(failCode);
			log.error(" get uid or select player error ", p);
		});
	}

//	/**
			public void handleLogin(PlayerLoginRequest_01000001 req, GameClient client) {
				String passportSessionId = req.getSessionId();
			    boolean reconnect = req.getReconnect();
		
				checkLoginStatus(req.getVerstion(), client)
			        .compose(v -> getPlayerUid(passportSessionId))
			        .compose(uidResponse -> loadOrCreatePlayer(uidResponse, req, client))
			        .compose(player -> handleExistingPlayer(player, req, client, reconnect))
			        .onComplete(ar -> {
			            if (ar.failed()) {
			                handleLoginFailure(ar.cause(), client);
			            }
			        });
			}
		
			private Future<Void> checkLoginStatus(String version, GameClient client) {
				int canLogin = GameServerStatus.getInstance().canLogin(version);
//				if (canLogin > 0) {
//					return Future.failedFuture(canLogin + "");
//				}
				return Future.succeededFuture();
			}
		
			private Future<LoginPlayerUidResponse_7d000019> getPlayerUid(String passportSessionId) {
				return VxHolder.requestRemoteServer(ServerType.Login, LoginPlayerUidRequest_7d000018.newBuilder().setPassportSessionId(passportSessionId).build())
						.map(message -> (LoginPlayerUidResponse_7d000019) message.body());
			}
		
			private Future<PlayerData> loadOrCreatePlayer(LoginPlayerUidResponse_7d000019 uidResponse, PlayerLoginRequest_01000001 req, GameClient client) {
				long uid = uidResponse.getUid();
				client.setPlayerId(uid);
		
				return DAO.execute(PlayerDataMapper.class, MapperConstant.selectByPrimaryKey, uid).compose(result -> {
					if (result == null) {
						return createNewPlayer(uidResponse, req, client);
					}
					return Future.succeededFuture((PlayerData) result);
				});
			}
		
			private Future<PlayerData> createNewPlayer(LoginPlayerUidResponse_7d000019 uidResponse, PlayerLoginRequest_01000001 req, GameClient client) {
				return toVertxFuture(PlayerHelper.trySetServerId(uidResponse.getUid())).compose(locked -> {
					if (!locked) {
						return Future.failedFuture(new LogicException(ErrorMsgEnum.player_lock.getId()));
					}
					Account account = new Account(req);
					account.accountId = uidResponse.getAccountId();
					account.deviceId = uidResponse.getDeviceId();
					// 确保这个方法返回 Future<PlayerData>
					return createPlayer(account, client, uidResponse.getUid(), null, true, 0, false, true);
				});
			}
		
			private Future<Void> handleExistingPlayer(PlayerData player, PlayerLoginRequest_01000001 req, GameClient client, boolean reconnect) {
				long playerId = player.getPlayerId();
				client.setPlayerId(playerId);
		
				return checkPlayerUnlock(playerId).compose(v -> checkReconnect(client, reconnect, playerId)).compose(v -> checkOtherServer(player, req, client));
			}
		
			private Future<Void> checkPlayerUnlock(long playerId) {
				boolean checkUnlock = PlayerManager.getInstance().checkUnlock(playerId);
				if (!checkUnlock) {
					return Future.failedFuture(new LogicException(ErrorMsgEnum.login_forbidden.getId()));
				}
				return Future.succeededFuture();
			}
		
			private Future<Void> checkReconnect(GameClient client, boolean reconnect, long playerId) {
	//				GameClient oldGameClient = GameClientManager.getInstance().getGameClientByPlayer(playerId);
				if (PlayerHelper.reconnect(client, reconnect, playerId)) {
					return Future.failedFuture(new LogicException(ErrorMsgEnum.reconnect_fail.getId()));
				}
				return Future.succeededFuture();
			}
		
			private Future<Void> checkOtherServer(PlayerData player, PlayerLoginRequest_01000001 req, GameClient client) {
				return toVertxFuture(RedissonUtil.<String>getAsync(CacheType.PLAYER_SERVER_ID.key(player.getPlayerId()))).compose(serverId -> {
					if (serverId != null && !serverId.equalsIgnoreCase(ServerContext.getInstance().getServerId())) {
						return notifyOtherServerLogout(serverId, player.getPlayerId()).compose(v -> loadPlayerFromDb(player, new Account(req), client));
					}
					return loadPlayerFromDb(player, new Account(req), client);
				});
			}
		
			private Future<Void> notifyOtherServerLogout(String serverId, long playerId) {
				return VxHolder.requestRemoteServer(serverId, GamePlayerLogoutRequest_7d000101.newBuilder().setPlayerId(playerId).build()).mapEmpty();
			}
		
			private Future<Void> loadPlayerFromDb(PlayerData player, Account account, GameClient client) {
				return PlayerHelper.startLoadPlayerFromDb(client, player, account).mapEmpty();
			}
		
			private void handleLoginFailure(Throwable throwable, GameClient client) {
				int errorCode = (throwable instanceof LogicException) ? ((LogicException) throwable).getErrorCode() : ErrorMsgEnum.unknown.getId();
				log.error("Player login error", throwable);
				client.sendProtocol(PlayerLoginResponse_01000002.getDefaultInstance(), errorCode);
			}
		
			private <T> Future<T> toVertxFuture(RFuture<T> rFuture) {
				Promise<T> promise = Promise.promise();
				rFuture.onComplete((result, throwable) -> {
					if (throwable != null) {
						promise.fail(throwable);
					} else {
						promise.complete(result);
					}
				});
				return promise.future();
			}
			
//			**/

	protected void logout(NetClient client, Object message) {
//		PlayerMsg.PlayerLogoutRequest_01000003 req = (PlayerLogoutRequest_01000003) message;
//		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		// 保存数据
		Future<?> logout = GameClientManager.getInstance().logout((GameClient) client);
		logout.onComplete(r -> {
			client.sendProtocol(PlayerLogoutResponse_01000004.getDefaultInstance());
		});
	}

	/*public void createPlayer(NetClient client, Object message) {
		PlayerCreateRequest_01000053 create = (PlayerCreateRequest_01000053) message;
		PlayerCreateResponse_01000054.Builder builder = PlayerCreateResponse_01000054.newBuilder();
	
	//		String name = create.getName();
		String sessionId = create.getSessionId();
		Player player = new Player();
		PlayerExt playerExt = new PlayerExt();
	
		long id = GameServer.getInstance().nextPlayerId();
		if (id == 0) {
			log.error("创建角色数量到达限制：" + sessionId);
			client.sendProtocol(builder, ErrorMsgEnum.unknown.getId());
			return;
		}
		// TODO check 敏感词
		// if (!TreeWordFilter.check(create.getName())) {
		// log.error("名字包含敏感词：" + create.getName());
		// builder.setResult(false);
		// client.sendProtocol(builder);
		// return;
		// }
	
	//		Player ofName = PlayerManager.getInstance().getOfName(create.getName());
	//		if (ofName != null) {
	//			log.error(" 名字重复了：" + create.getName());
	//			client.sendProtocol(builder, ErrorMsgEnum.player_name_repeat.getId());
	//			return;
	//		}
		// player.getData().setSeq(seq) ;
		player.getData().setPlayerId(id);
	//		player.getData().setGender(create.getIsMan());
		player.getData().setCreateDate(DateUtil.getStringDate());
		player.getData().setLevel(1);
		player.getData().setName(create.getName());
	//		player.getData().setName(id + "");
		player.getData().setFirstEnterGame((byte) 1);
		player.getData().setLoginDate(DateUtil.getStringDate());
		player.getData().setTrainingRewardTimes(GlobalConst.routineTrainingRewardNum);
		player.getData().setVipExpTotal(0);
		player.getData().setVipLevel(1); // 好感度默认1级
		player.getData().setPowerRecoverTime(System.currentTimeMillis());
		player.getData().setRefreshDay(DateUtil.getDay(0));
		player.getData().setPower(PlayerHelper.energyMax(player));// 根据配表确定初始好感度
		playerExt.setPlayerId(id);
		player.getData().setExploreEquipInherit(false);
		player.getData().setDay(1);
	
		ObjUtil.setDefaultValue(player);
		ObjUtil.setDefaultValue(playerExt);
	
	//		User user = null;
	//		List<UserTag> tags = null;
		Future<Long> uidFuture = GameServer.getInstance().getLoginGameServerInterface()
				.getUid2(sessionId);
		uidFuture.compose(uid -> {
			player.getData().setUid(uid);
			return GameServer.getInstance().getDataGameServerInterface().execAsync(PlayerMapper.class,
					MapperConstant.insert, player);
		}).compose(r -> GameServer.getInstance().getDataGameServerInterface().execAsync(PlayerExtMapper.class,
				MapperConstant.insert, playerExt)).compose(r -> {
					List<DbTask> dbTasks = new ArrayList<>();
					dbTasks.add(new DbTask(UserMapper.class, MapperConstant.selectByPrimaryKey, player.getData().getUid()));
					dbTasks.add(new DbTask(UserTagMapper.class, MapperConstant.selectByUid, player.getData().getUid()));
					return GameServer.getInstance().getDataGameServerInterface().execAsync(dbTasks);
				}).onSuccess(list -> {
					try {
						User user = (User) list.get(0);
						List<UserTag> tags = (List<UserTag>) list.get(1);
						UserOp userOp = PlayerCacheFactory.getCache(player.getData().getPlayerId(), UserOp.class);
						userOp.initLoadData(player.getData().getUid(), user, tags);
						// 成功
						client.setPlayerId(id);
						GameClientManager.getInstance().addGameClientPlayer((GameClient) client);
	//				GameClientManager.getInstance().addGameClientSession((GameClient) client);
	
						PlayerManager.getInstance().initAdd(player);
						PlayerManager.getInstance().initAdd(playerExt);
	
						PlayerHelper.initPlayerData(player);
						EventOp eventOp = PlayerCacheFactory.getCache(player.getData().getPlayerId(), EventOp.class);
						player.handleEvent(new GameEvent(EventTypeEnum.Login));
	
						PlayerHelper.initAfterLogin(player);
						// 初始的资源
						PlayerHelper.addResources(player.getData().getPlayerId(), GlobalConst.initItems);
	
						// 插入storeData表
						StoreOp storeOp = PlayerCacheFactory.getCache(player.getData().getPlayerId(), StoreOp.class);
						storeOp.insertStoreData();
	
						builder.setPlayerInfo(PbBuilder.buildPlayerInfo(player));
						//						builder.setArchive(PbBuilder.buildPlayerArchiveInfo(player));
						builder.setTime(System.currentTimeMillis() + "");
						builder.setConfigFileVersion(PlayerHelper.getServerConfigVersion());
						client.sendProtocol(builder.build());
	
						levellog.info("opType[levelUp]playerId[{}]newLevel[{}]", player.getData().getPlayerId(),
								player.getData().getLevel());
						loginlog.info("opType[gameLogin]playerId[{}]isCreate[{}]isLogin[{}]onlineTime[{}]",
								player.getData().getPlayerId(), true, true, 0);
					} catch (Exception e) {
						log.error("", e);
					}
	
				}).onFailure(r -> {
					client.sendProtocol(builder, ErrorMsgEnum.unknown.getId());
					log.error("", r);
				});
	}*/

	public Future<PlayerData> createPlayer(Account account, NetClient client, long uid, String name, boolean isMan, int head,
			boolean isPc, boolean autoCreate) {
		PlayerData playerData = new PlayerData();

		long id = uid;
//		if (id == 0) {
//			log.error("创建角色数量到达限制：" + passportSessionId);
//			client.sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), ErrorMsgEnum.unknown.getId());
//			return;
//		}
		// TODO check 敏感词
		// if (!TreeWordFilter.check(create.getName())) {
		// log.error("名字包含敏感词：" + create.getName());
		// builder.setResult(false);
		// client.sendProtocol(builder);
		// return;
		// }

//		Player ofName = PlayerManager.getInstance().getOfName(create.getName());
//		if (ofName != null) {
//			log.error(" 名字重复了：" + create.getName());
//			client.sendProtocol(builder, ErrorMsgEnum.player_name_repeat.getId());
//			return;
//		}
		// player.getData().setSeq(seq) ;
		playerData.setPlayerId(id);
		playerData.setUid(uid);
		playerData.setGender(isMan);
		playerData.setCreateDate(DateUtil.getStringDate());
//		playerData.setLevel(1);
//		playerData.getHotData().getLevelMap().setValue(Asset.playerExp.ID, 1);
//		player.getData().setName(create.getName());
		// 随机一个名字
		name = randomName();
//		if (StringUtils.isEmpty(name)) {
//			playerData.setName(id + "");
//		} else {
//			playerData.setName(name);
//		}
		playerData.setName(name);
		playerData.setAccountId(account.accountId);
		playerData.setDeviceId(account.deviceId);
		playerData.setHead(Rnd.randomOne(HeadPortraitManager.instance().list()).ID);
		playerData.setHeadFrame(Rnd.randomOne(HeadBoxManager.instance().list()).ID);
		playerData.setRegion(AddressUtil.getCityInfo(client.getIp()));
		playerData.setLoginDate(DateUtil.getStringDate());
		playerData.setVipExpTotal(0);
		playerData.setVipLevel(1); // 好感度默认1级
		playerData.setRefreshDay(DateUtil.getDay(0));
		playerData.setModules("[]");

		ObjUtil.setDefaultValue(playerData);

		Promise<PlayerData> promise = Promise.promise();
		DAO.execute(PlayerDataMapper.class, MapperConstant.insert, playerData).onSuccess(r -> {
			try {
//				User user = (User) list.get(0);
//				List<UserTag> tags = (List<UserTag>) list.get(1);
				// 成功
				client.setPlayerId(id);
				GameClientManager.getInstance().addGameClientPlayer((GameClient) client);
				GameClientManager.getInstance().addGameClientSession((GameClient) client);

				Player player = new Player(playerData);
				player.setGameClient((GameClient) client);
				player.setAccount(account);

				PlayerManager.getInstance().initAdd(player);

				// 初始的资源
				PlayerHelper.addResources(player, GlobalConst.initItems, OpType.Init);

				PlayerHelper.initNewPlayerData(player);

				player.handleEvent(EventTypeEnum.Login);

				PlayerHelper.initAfterLogin(player);

				if (autoCreate) {
					PlayerLoginResponse_01000002.Builder builder = PlayerLoginResponse_01000002.newBuilder();
					builder.setInfo(PbBuilder.buildPlayerInfo(player));
					client.sendProtocol(builder.build());

				} else {
//					if (isPc) {
//						PCPlayerCreateResponse_01000054.Builder builder = PCPlayerCreateResponse_01000054.newBuilder();
//						builder.setPlayerInfo(PbBuilder.buildPlayerInfo(player));
//						builder.setTime(System.currentTimeMillis() + "");
//						client.sendProtocol(builder.build());
//
//					} else {
//						PlayerCreateResponse_01000004.Builder builder = PlayerCreateResponse_01000004.newBuilder();
//						builder.setInfo(PbBuilder.buildPlayerInfo(player));
//						client.sendProtocol(builder.build());
//					}
				}

				levellog.info("opType[levelUp]playerId[{}]newLevel[{}]", playerData.getPlayerId(),
						playerData.getLevel());
				loginlog.info("opType[gameLogin]playerId[{}]isCreate[{}]isLogin[{}]onlineTime[{}]",
						playerData.getPlayerId(), true, true, 0);
				promise.complete(playerData);
			} catch (Exception e) {
				log.error(uid + " 初始化失败", e);
				PlayerManager.getInstance().deletePlayer(id);
				client.sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), ErrorMsgEnum.unknown.getId());
				promise.fail(e);
			}

		}).onFailure(e -> {
			client.sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), ErrorMsgEnum.unknown.getId());
			log.error("", e);
			promise.fail(e);
		});
		return promise.future();
	}

	private String randomName() {
		List<RandomNameConfig> list = RandomNameManager.instance().list();
		RandomNameConfig randomOne = Rnd.randomOne(list);
		String xing = randomOne.Familyname;
		String name1;
		String name2;
		boolean isMan = Rnd.nextBoolean();
		if (isMan) {
			name1 = Rnd.randomOne(list).MenName1;
//			name2 = Rnd.randomOne(list).MenName2;
		} else {
			name1 = Rnd.randomOne(list).WomenName1;
//			name2 = Rnd.randomOne(list).WomenName2;
		}
		return xing + name1;
	}
}
