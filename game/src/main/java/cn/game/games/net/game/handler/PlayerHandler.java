package cn.game.games.net.game.handler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.exception.LogicException;
import cn.game.core.net.client.LogoutType;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Item;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.cache.entity.ShopItem;
import cn.game.games.core.GameServerStatus;
import cn.game.games.core.SimplePlayer;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.manager.PlayerNameManager;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.battle.DaoHeartBattle;
import cn.game.games.net.game.module.battle.ShiLuoZhenJingBattle;
import cn.game.games.net.game.module.battle.WorldBossBattle;
import cn.game.games.net.game.module.mail.MailModule;
import cn.game.games.net.game.module.player.IdConstant;
import cn.game.games.net.game.module.player.PlayerModule;
import cn.game.games.net.game.module.player.VarConstant;
import cn.game.games.net.game.module.player.pointreward.PointRewardModule;
import cn.game.games.net.game.module.player.pointreward.PointRewardType;
import cn.game.games.net.game.module.shop.ShopModule;
import cn.game.games.util.AddressUtil;
import cn.game.games.util.DAO;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeishiConfig;
import cn.game.protocol.generated.config.QuestionnaireConfig;
import cn.game.protocol.generated.config.WorldBossRewardConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.HeishiManager;
import cn.game.protocol.generated.manager.QuestionnaireManager;
import cn.game.protocol.generated.manager.WorldBossRewardManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAssetDataRequest_01000200;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAssetDataResponse_01000201;
import cn.game.protocol.protobuf.PlayerMsg.PlayerBriefInfoOtherRequest_01000009;
import cn.game.protocol.protobuf.PlayerMsg.PlayerBriefInfoRequest_01000007;
import cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxRequest_01000042;
import cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxResponse_01000043;
import cn.game.protocol.protobuf.PlayerMsg.PlayerGenderRequest_01000017;
import cn.game.protocol.protobuf.PlayerMsg.PlayerGenderResponse_01000018;
import cn.game.protocol.protobuf.PlayerMsg.PlayerGuideRequest_01000060;
import cn.game.protocol.protobuf.PlayerMsg.PlayerGuideResponse_01000061;
import cn.game.protocol.protobuf.PlayerMsg.PlayerHeadFrameRequest_01000015;
import cn.game.protocol.protobuf.PlayerMsg.PlayerHeadFrameResponse_01000016;
import cn.game.protocol.protobuf.PlayerMsg.PlayerHeadRequest_01000013;
import cn.game.protocol.protobuf.PlayerMsg.PlayerHeadResponse_01000014;
import cn.game.protocol.protobuf.PlayerMsg.PlayerHeartbeatResponse_01000006;
import cn.game.protocol.protobuf.PlayerMsg.PlayerImageRequest_01000019;
import cn.game.protocol.protobuf.PlayerMsg.PlayerImageResponse_0100001a;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLoginRequest_01000001;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLoginResponse_01000002;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLogoutResponse_01000004;
import cn.game.protocol.protobuf.PlayerMsg.PlayerNameRequest_01000011;
import cn.game.protocol.protobuf.PlayerMsg.PlayerNameResponse_01000012;
import cn.game.protocol.protobuf.PlayerMsg.PlayerPatrolInfoRequest_01000070;
import cn.game.protocol.protobuf.PlayerMsg.PlayerPatrolInfoResponse_01000071;
import cn.game.protocol.protobuf.PlayerMsg.PlayerQuestionnaireRequest_01000300;
import cn.game.protocol.protobuf.PlayerMsg.PlayerQuestionnaireResponse_01000301;
import cn.game.protocol.protobuf.PlayerMsg.PlayerQuestionnaireRewardRequest_01000302;
import cn.game.protocol.protobuf.PlayerMsg.PlayerQuestionnaireRewardResponse_01000303;
import cn.game.protocol.protobuf.PlayerMsg.PlayerReconnecRequest_01000065;
import cn.game.protocol.protobuf.PlayerMsg.PlayerReconnecResponse_01000066;
import cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointRequest_01000075;
import cn.game.protocol.protobuf.PlayerMsg.PlayerRedPointResponse_01000076;
import cn.game.protocol.protobuf.PlayerMsg.PlayerSearchRequest_0100000b;
import cn.game.protocol.protobuf.PlayerMsg.PlayerSearchResponse_0100000c;
import cn.game.protocol.protobuf.PlayerMsg.PlayerShowRequest_01000039;
import cn.game.protocol.protobuf.PlayerMsg.PlayerShowResponse_0100003a;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.ServerMsg.GamePlayerLogoutRequest_7d000101;
import cn.game.protocol.protobuf.ServerMsg.LoginPlayerUidRequest_7d000018;
import cn.game.protocol.protobuf.ServerMsg.LoginPlayerUidResponse_7d000019;
import cn.game.util.BinarySearchUtil;
import cn.game.util.ConversionUtil;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;
import cn.game.util.ObjUtil;
import cn.game.util.RedisUtil;
import cn.game.util.ServerType;
import io.vertx.core.Future;

/**
 * 用户处理器
 */
@Component
public class PlayerHandler extends BaseHandler {

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
		putInvoker(PbProtocol.PlayerImageRequest_01000019, this::image);
		putInvoker(PbProtocol.PlayerHeadFrameRequest_01000015, this::headFrame);
		putInvoker(PbProtocol.PlayerGenderRequest_01000017, this::gender);
//		putInvoker(PbProtocol.ItemUseRequest_01000050, this::useItem);
		putInvoker(PbProtocol.PlayerCloudBoxRequest_01000042, this::cloudBox);
		putInvoker(PbProtocol.PlayerGuideRequest_01000060, this::guide);
		putInvoker(PbProtocol.PlayerPatrolInfoRequest_01000070, this::patrolInfo);
		putInvoker(PbProtocol.PlayerRedPointRequest_01000075, this::red);
		putInvoker(PbProtocol.PlayerSearchRequest_0100000b, this::searchPlayer);
		putInvoker(PbProtocol.PlayerAssetDataRequest_01000200, this::assetData);
		putInvoker(PbProtocol.PlayerQuestionnaireRequest_01000300, this::questionnaireInfo);
		putInvoker(PbProtocol.PlayerQuestionnaireRewardRequest_01000302, this::questionnaireReward);
//		putInvoker(PbProtocol.PlayerDeleteRequest_01000070, this::delete);
	}

	private void questionnaireReward(NetClient client, Object message) {
		PlayerQuestionnaireRewardRequest_01000302 request = (PlayerQuestionnaireRewardRequest_01000302) message;
		PlayerQuestionnaireRewardResponse_01000303.Builder response = PlayerQuestionnaireRewardResponse_01000303.newBuilder();
		int id = request.getId();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		Set<Integer> idsSet = player.getPlayerModule().getIdsSet(IdConstant.Questionnaire);
		if (idsSet.contains(id)) {
			client.sendProtocol(response, ErrorMsgEnum.repeat_request.getId());
			return;
		}
		QuestionnaireConfig questionnaireConfig = QuestionnaireManager.instance().get(id);
		if (!PlayerHelper.checkCondition(player, questionnaireConfig.Condition)) {
			client.sendProtocol(response, ErrorMsgEnum.condition_check_error.getId());
			return;
		}
		idsSet.add(id);
		response.addAllRewards(PlayerHelper.addResources(player, questionnaireConfig.Reward, OpType.Questionnaire));
		client.sendProtocol(response.build());
	}
	private void questionnaireInfo(NetClient client, Object message) {
		PlayerQuestionnaireRequest_01000300 request = (PlayerQuestionnaireRequest_01000300) message;
		PlayerQuestionnaireResponse_01000301.Builder response = PlayerQuestionnaireResponse_01000301.newBuilder();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		Collection<QuestionnaireConfig> list = QuestionnaireManager.instance().list();
		Set<Integer> idsSet = player.getPlayerModule().getIdsSet(IdConstant.Questionnaire);
		for (QuestionnaireConfig questionnaireConfig : list) {
			if (PlayerHelper.checkCondition(player, questionnaireConfig.Condition)) {
				if (idsSet.contains(questionnaireConfig.ID)) {
					response.putQuestionnaire(questionnaireConfig.ID, true);
				} else {
					response.putQuestionnaire(questionnaireConfig.ID, false);
				}
			}
		}
		client.sendProtocol(response.build());
	}
	private void assetData(NetClient client, Object message) {
		PlayerAssetDataRequest_01000200 request = (PlayerAssetDataRequest_01000200) message;
		PlayerAssetDataResponse_01000201.Builder response = PlayerAssetDataResponse_01000201.newBuilder();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		response.putAllAssets(player.getCurrencyModule().getCurrencyMap().getMap());

		for (Item item : player.getItemModule().list()) {
			response.addItems(item.toItemInfo());
		}
		client.sendProtocol(response.build());
	}
	private void searchPlayer(NetClient client, Object message) {
		PlayerSearchRequest_0100000b request = (PlayerSearchRequest_0100000b) message;
		PlayerSearchResponse_0100000c.Builder resp = PlayerSearchResponse_0100000c.newBuilder();
		String playerName = request.getPlayerName();
		long playerId = StringUtils.isEmpty(request.getPlayerId()) ? 0 : Long.parseLong(request.getPlayerId());
		Future<SimplePlayer> future = PlayerHelper.seachPlayer(playerName, playerId);
		future.onSuccess(r -> {
			SimplePlayer simplePlayer = (SimplePlayer) r;
			if (simplePlayer == null) {
				client.sendProtocol(resp, ErrorMsgEnum.player_not_found.getId());
				return;
			}
			resp.setPlayer(simplePlayer.toSimplePlayerInfo());
			client.sendProtocol(resp);
		}).onFailure(t -> {
			client.sendProtocol(resp, ErrorMsgEnum.player_not_found.getId());
		});
	}

	private void red(NetClient client, Object message) {
		PlayerRedPointRequest_01000075 request = (PlayerRedPointRequest_01000075) message;
		PlayerRedPointResponse_01000076.Builder resp = PlayerRedPointResponse_01000076.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		List<Integer> typeList = request.getTypeList();
		List<Boolean> redList = new ArrayList<>();
		for (Integer i : typeList) {
			redList.add(false);
		}
		int errorCode = 0;
		for (int i = 0; i < typeList.size(); i++) {
			int type = typeList.get(i);
			InitialUI func = InitialUI.get(type);
			boolean ret = false;
			if (player.isFuncOpen(func)) {
				switch (func) {
				case DaoXinLLiLian: {
					ChapterModule chapterModule = player.getChapterModule();
					DaoHeartBattle daoHeartBattle = chapterModule.getBattle(DungeonTypeEnum.DaoHeart);
					if (daoHeartBattle == null) {
						continue;
					}
					List<Integer> rewardBattleIds = daoHeartBattle.getRewardBattleIds();
					int completeBattleId = daoHeartBattle.getCompleteBattleId();
					if (completeBattleId == 0) {
						continue;
					}
					BattleConfig battleConfig = BattleManager.instance().get(completeBattleId);

					while (battleConfig != null) {
						if (!rewardBattleIds.contains(battleConfig.ID)) {
							ret = true;
							break;
						}
						battleConfig = BattleManager.instance().getNullable(battleConfig.preBattle);
					}
					break;
				}
				case XinMoShiLian: {
					ChapterModule chapterModule = player.getChapterModule();
					DaoHeartBattle daoHeartBattle = chapterModule.getBattle(DungeonTypeEnum.XinMo);
					if (daoHeartBattle == null) {
						continue;
					}
					List<Integer> rewardBattleIds = daoHeartBattle.getRewardBattleIds();
					int completeBattleId = daoHeartBattle.getCompleteBattleId();
					if (completeBattleId == 0) {
						continue;
					}
					BattleConfig battleConfig = BattleManager.instance().get(completeBattleId);

					while (battleConfig != null) {
						if (!rewardBattleIds.contains(battleConfig.ID)) {
							ret = true;
							break;
						}
						battleConfig = BattleManager.instance().getNullable(battleConfig.preBattle);
					}
					break;
				}
				case YaoWangBiePao: {
					ChapterModule chapterModule = player.getChapterModule();
					DaoHeartBattle daoHeartBattle = chapterModule.getBattle(DungeonTypeEnum.YaoWang);
					if (daoHeartBattle == null) {
						continue;
					}
					List<Integer> rewardBattleIds = daoHeartBattle.getRewardBattleIds();
					int completeBattleId = daoHeartBattle.getCompleteBattleId();
					if (completeBattleId == 0) {
						continue;
					}
					BattleConfig battleConfig = BattleManager.instance().get(completeBattleId);

					while (battleConfig != null) {
						if (!rewardBattleIds.contains(battleConfig.ID)) {
							ret = true;
							break;
						}
						battleConfig = BattleManager.instance().getNullable(battleConfig.preBattle);
					}
					break;
				}
				case ShiLuoZhenJing: {
					ChapterModule chapterModule = player.getModule(ChapterModule.class);
					ShiLuoZhenJingBattle battle = chapterModule.getBattle(DungeonTypeEnum.ShiLuoZhenJing);

					if (!battle.isCanReward()) {
						continue;
					}
					if (battle.isHistoryMaxReward()) {
						continue;
					}
					if (battle.getHistoryMaxBattleId() == 0) {
						continue;
					}
//					int startBattleId = battle.getStartBattleId();
//
//					if (startBattleId == 0) {
//						continue;
//					}
					ret = true;
					break;
				}
				case WorldBoss: {
					ChapterModule chapterModule = player.getModule(ChapterModule.class);
					WorldBossBattle battle = chapterModule.getBattle(DungeonTypeEnum.WorldBoss);
					long maxDamageToday = battle.getMaxDamageToday();
					List<WorldBossRewardConfig> list = WorldBossRewardManager.instance().list();
					int canRewardIndex = BinarySearchUtil.findIndexLastLessThanOrEqual(list, maxDamageToday, r -> r.BoxCondition);

					if (canRewardIndex < 0) {
						continue;
					}
					WorldBossRewardConfig rewardConfig = list.get(canRewardIndex);
					if (battle.getRewardId() >= rewardConfig.ID) {
						continue;
					}
					int rewardIndex = BinarySearchUtil.findElementIndexByField(list, battle.getRewardId(), r -> r.ID, (r1, r2) -> r1 - r2);
					if (rewardIndex != canRewardIndex) {
						ret = true;
					}
					break;
				}
				case SpiritBattle: {
					PointRewardModule pointRewardModule = player.getPointRewardModule();
					ret = pointRewardModule.canReward(PointRewardType.LingPo, 0, 0, -1);
					break;
				}
				case NightmareRealm: {
					ChapterModule chapterModule = player.getChapterModule();
					DaoHeartBattle daoHeartBattle = chapterModule.getBattle(type);

					ret = false;
					break;
				}
				case Letter: {
					MailModule module = player.getMailModule();
					ret = module.hasNoRead();
					break;
				}
				case CardBook: {

					ret = false;
					break;
				}
				case Shop: {
					if (!player.isFuncOpen(InitialUI.Shop)) {
						continue;
					}
					ShopModule shopModule = player.getShopModule();
					IntMapWrapper heishiRefreshTimesMap = shopModule.getHeishiRefreshTimesMap();
//					int heishiRefreshTimes = heishiRefreshTimesMap.getValue(2);
//
//					int freeFreshMaxTimes = GlobalConst.HeishiFreeRefresh;
//					int welfareValue = player.getWelfareValue(WelfareTypeEnum.StoreRefresh);
//					freeFreshMaxTimes += welfareValue;
//
//					ret = freeFreshMaxTimes > heishiRefreshTimes;
//					if (!ret) {
						List<HeishiConfig> heishiList = HeishiManager.instance().getShopIDTypeList(2, 1);
						if (typeList != null) {
							for (HeishiConfig heishiConfig : heishiList) {
								ShopItem shopItem = shopModule.getShopItem(2, heishiConfig.Item);
								if (shopItem.getItemBuyTimes() == 0) {
									ret = true;
									break;
								}
							}
						}
//					}
					break;
				}
				default:
					errorCode = ErrorMsgEnum.red_point_not_support.getId();
					break; 
				}
			}
			redList.set(i, ret);
//			redList.add(ret);
		}
		resp.addAllIsRed(redList);
		client.sendProtocol(resp, errorCode);
	}

	private void guide(NetClient client, Object message) {
		PlayerGuideRequest_01000060 request = (PlayerGuideRequest_01000060) message;
		PlayerGuideResponse_01000061.Builder resp = PlayerGuideResponse_01000061.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		PlayerModule playerModule = player.getPlayerModule();
		Map<Integer, Integer> guideMap = playerModule.getGuideMap();
		guideMap.put(request.getType(), request.getStep());
		client.sendProtocol(resp);
		GameLogger.newstages(player, request.getType(), request.getStep());
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

			GameClientManager.getInstance().removeGameClient(oldGameClient, LogoutType.Reconnect);

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
	}

	protected void getPlayerOtherBriefInfo(NetClient client, Object message) {
		PlayerBriefInfoOtherRequest_01000009 request = (PlayerBriefInfoOtherRequest_01000009) message;
		List<String> playerStringIds = request.getPlayerIdsList();
		List<String> servers = new ArrayList<>(request.getServerIdsList());
		List<Long> playerIds = ConversionUtil.toLongList(playerStringIds);
	}

	protected void show(NetClient client, Object message) {
		PlayerShowRequest_01000039 request = (PlayerShowRequest_01000039) message;
		PlayerShowResponse_0100003a.Builder resp = PlayerShowResponse_0100003a.newBuilder();
		String playerName = request.getPlayerName();
		long playerId = StringUtils.isEmpty(request.getPlayerId()) ? 0 : Long.parseLong(request.getPlayerId());
		PlayerNameManager.getInstance().getPlayerId(playerName).compose(r -> {
			long searchPlayerId = 0;
			if (r == null) {
				searchPlayerId = playerId;
			} else {
				searchPlayerId = r;
			}
			return RedisLocalCache.getInstance().getAsync(CacheType.PLAYER_SIMPLE.key(searchPlayerId));
		}).onSuccess(r -> {
			SimplePlayer simplePlayer = (SimplePlayer) r;
			if (simplePlayer == null) {
				client.sendProtocol(resp, ErrorMsgEnum.player_not_found.getId());
				return;
			}
			resp.setPlayer(simplePlayer.toShowInfo());
			client.sendProtocol(resp);
		}).onFailure(t -> {
			client.sendProtocol(resp, ErrorMsgEnum.player_not_found.getId());
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

	protected void image(NetClient client, Object message) {

		PlayerImageRequest_01000019 request = (PlayerImageRequest_01000019) message;
		PlayerImageResponse_0100001a resp = PlayerImageResponse_0100001a.getDefaultInstance();
		int image = request.getImage();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		player.getData().setImage(image);
		client.sendProtocol(resp);
	}

	protected void rename(NetClient client, Object message) {

		PlayerNameRequest_01000011 request = (PlayerNameRequest_01000011) message;
		PlayerNameResponse_01000012.Builder resp = PlayerNameResponse_01000012.newBuilder();
		String newName = request.getName();
		if (StringUtils.isBlank(newName)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.request_parameter_error.ID);
			return;
		}
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		String oldName = player.getData().getName();

		int renameCount = player.getVarModule().getVar(VarConstant.RANAME_COUNT);
		int[] cost = renameCount >= GlobalConst.PlayerName.length - 1 ? GlobalConst.PlayerName[GlobalConst.PlayerName.length - 1]
				: GlobalConst.PlayerName[renameCount];
		if (!PlayerHelper.isEnough(player, cost)) {
			client.sendProtocol(resp.build(), ErrorMsgEnum.resource_not_enough.ID);
			return;
		}

		Future<Boolean> checkFuture = PlayerHelper.checkContextData(player, newName);
		checkFuture.compose(b -> {
			if (!b) {
				return Future.failedFuture(new LogicException(ErrorMsgEnum.player_name_illegal.ID));
			}
			return Future.fromCompletionStage(PlayerNameManager.getInstance().tryCreateUser(newName));
		}).map(r -> {
			if (!r) {
				throw new LogicException(ErrorMsgEnum.player_name_repeat.ID);
			}
			PlayerNameManager.getInstance()
					.saveName2Id(newName, player.getData().getPlayerId())
					.thenCompose(rr -> PlayerNameManager.getInstance().removeName(oldName));

			PlayerHelper.delResources(player, cost, OpType.Rename);
			player.getVarModule().incrVar(VarConstant.RANAME_COUNT);

			player.getData().setName(newName);
			client.sendProtocol(resp);
			return null;
		}).onFailure(r -> player.handleFail(resp.build(), r));
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
		int canLogin = GameServerStatus.getInstance().canLogin(req.getVerstion());
		if (canLogin > 0) {
//			handleLoginFailure(null, ErrorMsgEnum.version_mismatch.ID, (GameClient) client, passportSessionId);
//			return;
		}
		GameClient oldGameClient = GameClientManager.getInstance().getGameClient(passportSessionId);
		GameClient newGameClient = (GameClient) client;

		Account account = new Account(req);
		Future<LoginPlayerUidResponse_7d000019> playerUid = getPlayerUid(passportSessionId);
		Future<Long> uidFuture =  playerUid.compose(r -> checkPlayerUnlock(r)).map(r -> {
			account.accountId = r.getAccountId();
			account.deviceId = r.getDeviceId();
			return r.getUid();
		});
		uidFuture.map(uid -> {
			newGameClient.setSessionId(passportSessionId);
			if (PlayerManager.getInstance().isForbidAccount(newGameClient.getPlayerId())) {
				client.sendProtocol(PlayerLoginResponse_01000002.getDefaultInstance(), ErrorMsgEnum.login_fail_player_is_forbid.getId());
				return null;
			}
			if (reconnect) { // 客户端主动重连
				boolean isReallyReconnect = PlayerHelper.reconnect(newGameClient, reconnect,
						oldGameClient == null ? 0 : oldGameClient.getPlayerId(), account);
				if (!isReallyReconnect) {
					client.sendProtocol(PlayerLoginResponse_01000002.getDefaultInstance(), ErrorMsgEnum.reconnect_fail.getId());
					GameClientManager.getInstance().removeGameClient(newGameClient, LogoutType.WrongReconnection);
				}
			} else {
				// 客户端新登陆
				boolean isReallyReconnect = PlayerHelper.reconnect(newGameClient, reconnect, uid, account);
				if (!isReallyReconnect) {
					loadOrCreatePlayerData(uid,account, newGameClient)
							.compose(playerData -> handlePlayerData(playerData,  account, newGameClient))
//							.compose(PlayerHelper::saveSimplePlayer)
							.onSuccess(r -> handleLoginSuccess(newGameClient, r))
							.onFailure(t -> handleLoginFailure(t, 0, newGameClient, passportSessionId));
				}
			}
			return null;
		}).onFailure(t -> handleLoginFailure(t, 0, newGameClient, passportSessionId));
	}

	private Future<LoginPlayerUidResponse_7d000019> getPlayerUid(String passportSessionId) {
		return VxHolder.requestRemoteServer(ServerType.Login, LoginPlayerUidRequest_7d000018.newBuilder().setPassportSessionId(passportSessionId).build())
				.map(message -> (LoginPlayerUidResponse_7d000019) message);
	}

	private Future<PlayerData> loadOrCreatePlayerData(long playerId, Account account, GameClient client) {
		return PlayerHelper.getPlayerDistributedLock(playerId)
				.compose(r -> DAO.execute(PlayerDataMapper.class, MapperConstant.selectByPrimaryKey, playerId))
				.compose(playerData -> {
					if (playerData == null) {
						return createNewPlayer(playerId, account, client);
					}
					return Future.succeededFuture((PlayerData) playerData);
				});
	}

	private Future<PlayerData> createNewPlayer(long playerId, Account account, GameClient client) {
		return PlayerNameManager.getInstance()
				.createUserName()
				.compose(name -> createPlayerData(account, client, playerId, name, true, 0))
				.compose(PlayerNameManager.getInstance()::saveName2Id);
	}

	private Future<Player> handleExistingPlayer(PlayerData player, Account account, GameClient client) {
		return checkOtherServer(player.getPlayerId()).compose(r -> loadPlayerFromDb(player, account, client));
	}

	private Future<Player> handlePlayerData(PlayerData playerData, Account account, GameClient client) {
		if (playerData.isNew()) {
			Player player = PlayerHelper.createPlayer(playerData, account, client);
			return PlayerHelper.initPlayerData(player)
					.compose(PlayerHelper::savePlayerToDb)
					.compose(PlayerHelper::saveSimplePlayer);
		}
		return handleExistingPlayer(playerData, account, client);
	}

	private Future<LoginPlayerUidResponse_7d000019> checkPlayerUnlock(LoginPlayerUidResponse_7d000019 uidResponse) {
		boolean checkUnlock = PlayerManager.getInstance().checkUnlock(uidResponse.getUid());
		if (!checkUnlock) {
			return Future.failedFuture(ErrorMsgEnum.login_forbidden.getId() + "");
		}
		return Future.succeededFuture(uidResponse);
	}

	/** 
	 * 如果玩家在其他服务器，先通知其他服务器退出该玩家。 
	 * @param playerId
	 * @return
	 */
	private Future<Void> checkOtherServer(long playerId) {
		return VxHolder.toVertxFuture(RedisUtil.<String>getAsync(CacheType.PLAYER_SERVER_ID.key(playerId))).compose(serverId -> {
			if (serverId != null && !serverId.equalsIgnoreCase(ServerContext.getInstance().getServerId())) {
				return notifyOtherServerLogout(serverId, playerId);
			}
			return Future.succeededFuture();
		});
	}

	private Future<Void> notifyOtherServerLogout(String serverId, long playerId) {
		return VxHolder.requestRemoteServer(serverId, GamePlayerLogoutRequest_7d000101.newBuilder().setPlayerId(playerId).build()).mapEmpty();
	}

	private Future<Player> loadPlayerFromDb(PlayerData player, Account account, GameClient client) {
		return PlayerHelper.startLoadPlayerFromDb(client, player, account);
	}

	private void handleLoginFailure(Throwable throwable, int errorCode, GameClient client, String passportSessionId) {

		log.error("player session  " + passportSessionId + " login error ", throwable);
		if (errorCode <= 0 && StringUtils.isNumeric(throwable.getMessage())) {
			errorCode = Integer.parseInt(throwable.getMessage());
		}
		client.sendProtocol(PlayerLoginResponse_01000002.getDefaultInstance(), errorCode);
		GameClientManager.getInstance().removeGameClient((GameClient) client, LogoutType.ClientLoginFail);
		if (client.getPlayerId() > 0) {
			Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
			if (player != null) {
				PlayerData data = player.getData();
				if (data != null && data.isNew()) {
					// 新创建的角色失败了，回收名字
					PlayerNameManager.getInstance().removeName(data.getName());
				}
			}
			PlayerHelper.clearPlayer(client.getPlayerId());
		}
	}

	private void handleLoginSuccess(GameClient client, Player player) {
		PlayerLoginResponse_01000002.Builder builder = PlayerLoginResponse_01000002.newBuilder();
		builder.setInfo(PbBuilder.buildPlayerInfo(player));
		client.sendProtocol(builder.build());
		GameClientManager.getInstance().broadcastOnlineToOtherServer(player.getPlayerId(), true, null);
	}

	protected void logout(NetClient client, Object message) {
//		PlayerMsg.PlayerLogoutRequest_01000003 req = (PlayerLogoutRequest_01000003) message;
//		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		// 保存数据
		Future<?> logout = GameClientManager.getInstance().logout((GameClient) client, LogoutType.ClientRequest);
		logout.onComplete(r -> {
			client.sendProtocol(PlayerLogoutResponse_01000004.getDefaultInstance());
		});
		if (ServerContext.getInstance().getRunMode().isPressure()) {
			log.warn(" client PlayerLogoutRequest_01000003 : " + client.getPlayerId());
		}
	}

	public Future<PlayerData> createPlayerData(Account account, NetClient client, long uid, String name, boolean isMan, int head) {
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
		// 这里先按照开服时间来设置区服，后续会改成按人数。
//		String openTime = GameServerStatus.getInstance().getServerInfo().getServerOpenTime();
		long timeMillis = System.currentTimeMillis();
		String[] serverAllocationTimes = GameServerStatus.getInstance().getServerInfo().getServerAllocationTimes();
		int day = 0;
		if (serverAllocationTimes != null && serverAllocationTimes.length > 0) {
			day = serverAllocationTimes.length;
			for (int i = 0; i < serverAllocationTimes.length; i++) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime dateTime = LocalDateTime.parse(serverAllocationTimes[i], formatter);
				long epochMilli = DateUtil.toEpochMilli(dateTime);
				if (timeMillis <= epochMilli) {
					day = i;
					break;
				}
			}
		}
		day++;
		playerData.setServerId("server" + day);

		playerData.setPlayerId(id);
		playerData.setUid(uid);
		playerData.setGender(isMan);
		playerData.setCreateDate(DateUtil.getStringDate());
//		player.getData().setName(create.getName());
		// 随机一个名字
//		name = randomName();
//		if (StringUtils.isEmpty(name)) {
//			playerData.setName(id + "");
//		} else {
//			playerData.setName(name);
//		}
		playerData.setName(name);
		playerData.setAccountId(account.accountId);
		playerData.setDeviceId(account.deviceId);
//		playerData.setHead(Rnd.randomOne(HeadPortraitManager.instance().list()).ID);
//		playerData.setHeadFrame(Rnd.randomOne(HeadBoxManager.instance().list()).ID);
//		playerData.setImage(Rnd.randomOne(HeadBoxManager.instance().list()).ID);
		playerData.setRegion(AddressUtil.getCityInfo(client.getIp()));
		playerData.setLoginDate(DateUtil.getStringDate());
		playerData.setVipExpTotal(0);// 废弃待删除
		playerData.setVipLevel(1); // 废弃待删除
		playerData.setRefreshDay(DateUtil.getDay(0));
		playerData.setRefreshFiveDay(DateUtil.getDay(5));
		playerData.setRefreshWeek(DateUtil.getWeek());
		playerData.setRefreshMonth(DateUtil.getMonth());
		playerData.setNew(true);
		playerData.setModules("[]");

		ObjUtil.setDefaultValue(playerData);
		/*	
		// 这里先不插入数据库，等完全初始化之后后再插入,防止初始化失败，造成数据不一致
		Promise<PlayerData> promise = Promise.promise();
			DAO.execute(PlayerDataMapper.class, MapperConstant.insert, playerData).onSuccess(r -> promise.complete(playerData)).onFailure(t -> promise.fail(t));
				return promise.future();*/
		return Future.succeededFuture(playerData);
	}

}
