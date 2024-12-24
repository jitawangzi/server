package cn.game.games.net.game.module.pvp;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.SimplePlayer;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.NPCConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.enume.WelfareTypeEnum;
import cn.game.protocol.generated.manager.NPCManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.util.GameUtil;
import io.vertx.core.Future;

/**
 * @ClassName OfflineBattleHandler
 *
 * @description: 大道争锋 离线PVP玩法
 * @author: ly
 * @create: 2024-09-07 15:20 @Version 1.0
 */
public class OfflineBattleHandler {

  /** 每次挑战花费1张挑战券（ItemID=205023） */
  static final int DA_DAO_TICK_ITEM_ID = 205023;

  /** 目标列表刷新 请求 */
  public static void searchTargetList(NetClient client, Object o) {
    BattleMsg.BattlePvPTargetListResponse_13000112.Builder res =
        BattleMsg.BattlePvPTargetListResponse_13000112.newBuilder();
    BattleMsg.BattlePvPTargetListRequest_13000111 req =
        (BattleMsg.BattlePvPTargetListRequest_13000111) o;
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    OfflineBattleModule module = player.getOfflineBattleModule();
    if (!player.isFuncOpen(InitialUI.AvenueBattle)) {
      client.sendProtocol(res, ErrorMsgEnum.func_not_open.ID);
      return;
    }
    if (!module.isPlay()) {
      client.sendProtocol(res, ErrorMsgEnum.da_dao_not_play.ID);
      return;
    }

    int totalRefreshNum = module.freeRefreshNum + module.costRefreshNUm;
    if (totalRefreshNum >= GlobalConst.DaDaoFreeCnt1 + GlobalConst.DaDaoPayCnt.length) {
      client.sendProtocol(res, ErrorMsgEnum.da_dao_refresh_is_max.ID);
      return;
    }
    if (module.freeRefreshNum >= GlobalConst.DaDaoFreeCnt1) {
      if (req.getUseCost()) {
        int[] cost = GlobalConst.DaDaoPayCnt[module.costRefreshNUm];
        if (!PlayerHelper.delResources(player, cost[0], cost[1], OpType.DA_DAO_Buy, true)) {
          client.sendProtocol(res, ErrorMsgEnum.resource_not_enough.ID);
          return;
        }
        module.costRefreshNUm++;
      } else {
        client.sendProtocol(res, ErrorMsgEnum.da_dao_free_refresh_is_max.ID);
      }
    } else {
      module.freeRefreshNum++;
    }
    List<Integer> scoreList = new CopyOnWriteArrayList<>();
    module
        .searchTargetList(req.getRefreshFlag(), scoreList)
        .onSuccess(
            result -> {
              result.forEach(
                  simplePlayer -> {
                    res.addTargetList(simplePlayer.toSimplePlayerInfo());
                  });
              res.addAllScoreList(scoreList);
              res.setRefreshNum(module.costRefreshNUm);
              res.setFreeRefreshNum(module.freeRefreshNum);
              client.sendProtocol(res);
            })
        .onFailure(
            err -> {
              System.out.println(err.getMessage());
              client.sendProtocol(res, ErrorMsgEnum.unknown.ID);
            });
  }

  /** 开始战斗请求 */
  public static void startBattle(NetClient client, Object o) {
    BattleMsg.BattlePvPStartResponse_13000114.Builder res =
        BattleMsg.BattlePvPStartResponse_13000114.newBuilder();
    BattleMsg.BattlePvPStartRequest_13000113 req = (BattleMsg.BattlePvPStartRequest_13000113) o;
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    OfflineBattleModule module = player.getOfflineBattleModule();
    if (!player.isFuncOpen(InitialUI.AvenueBattle)) {
      client.sendProtocol(res, ErrorMsgEnum.func_not_open.ID);
      return;
    }
    if (!module.isPlay()) {
      client.sendProtocol(res, ErrorMsgEnum.da_dao_not_play.ID);
      return;
    }
    if (!PlayerHelper.isEnough(player, DA_DAO_TICK_ITEM_ID, 1)) {
      client.sendProtocol(res, ErrorMsgEnum.da_dao_play_num_not_enough.ID);
      return;
    }
    if (module.getInBattlePlayer() != null) {
      client.sendProtocol(res, ErrorMsgEnum.da_dao_in_battle.ID);
      return;
    }
    boolean match =
        module.matchRefreshTargetList.stream()
            .anyMatch(
                matchPid -> {
                  return matchPid == Long.parseLong(req.getTargetId());
                });
    if (!match) {
      client.sendProtocol(res, ErrorMsgEnum.da_dao_not_found_target_Player.ID);
      return;
    }
    res.setSelfAttrs(player.getAttrModule().buildBattleAttrs());
    Future<SimplePlayer> targetPlayerFuture =
        module.getTargetPlayer(Long.parseLong(req.getTargetId()));
    targetPlayerFuture.onSuccess(
        targetPlayer -> {
          if (targetPlayer != null) {
            try {
              res.setTargetAttrs(targetPlayer.getPlayerBattleAttrs());
              BattleMsg.BattleLineupInfo.Builder targetLineup =
                  BattleMsg.BattleLineupInfo.newBuilder();
              targetLineup.setBattleType(DungeonTypeEnum.CHAPTER_TYPE_DA_DAO.getId());
              targetPlayer
                  .getLineupMaps()
                  .get(DungeonTypeEnum.CHAPTER_TYPE_DA_DAO.getId())
                  .forEach(
                      (k, v) -> {
                        targetLineup.addLineups(
                            BattleMsg.LineupInfo.newBuilder()
                                .setSeq(k)
                                .addAllHeroUid(
                                    v.stream().map(h -> h.getId() + "").collect(toList()))
                                .addAllHeroId(
                                    v.stream().map(h -> h.getConfigId()).collect(toList()))
                                .build());
                        res.setTargetLineupInfo(targetLineup.build());
                      });
            } catch (Exception e) {
              e.printStackTrace();
              module.setInBattlePlayer(null);
              throw e;
            }
            res.setTargetSecretscriptInfo(
                targetPlayer.toSecretscriptPbInfo(DungeonTypeEnum.CHAPTER_TYPE_DA_DAO));
            module
                .getRankList(player.getPlayerId(), targetPlayer.id)
                .whenComplete(
                    (rankResultList, action) -> {
                      GameLogger.pvpfight(
                          player,
                          true,
                          rankResultList.get(0),
                          0,
                          DungeonTypeEnum.CHAPTER_TYPE_DA_DAO.getId(),
                          targetPlayer,
                          rankResultList.get(1),
                          0,
                          0,
                          0,
                          false);
                    });
          }
          client.sendProtocol(res);
        });
  }

  /** 结算战斗请求 */
  public static void endBattle(NetClient client, Object o) {
    BattleMsg.BattlePvPEndResponse_13000116.Builder res =
        BattleMsg.BattlePvPEndResponse_13000116.newBuilder();
    BattleMsg.BattlePvPEndRequest_13000115 req = (BattleMsg.BattlePvPEndRequest_13000115) o;
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    if (!player.isFuncOpen(InitialUI.AvenueBattle)) {
      client.sendProtocol(res, ErrorMsgEnum.func_not_open.ID);
      return;
    }
    OfflineBattleModule module = player.getOfflineBattleModule();
    if (!module.isPlay()) {
      client.sendProtocol(res, ErrorMsgEnum.da_dao_not_play.ID);
      return;
    }
    if (!PlayerHelper.isEnough(player, DA_DAO_TICK_ITEM_ID, 1)) {
      client.sendProtocol(res, ErrorMsgEnum.da_dao_play_num_not_enough.ID);
      return;
    }
    module.playNum++;
	player.handleEvent(EventTypeEnum.ParticipatePVPStart);

    final SimplePlayer targetPlayer = module.getTargetPlayer(req.getTargetId());
    PlayerHelper.delResources(player, DA_DAO_TICK_ITEM_ID, 1, OpType.DA_DAO_JOIN, true);
    CompletableFuture<List<Integer>> rankListFuture =
        module.getRankList(player.getPlayerId(), Long.parseLong(req.getTargetId()));
    Future<Void> updateFuture =
        module.updateScore(req.getWin(), Long.parseLong(req.getTargetId()), res);
    rankListFuture.thenCombine(
        updateFuture.toCompletionStage(),
        (rankResultList, v) -> {
          final int selfRank = rankResultList.get(0);
          final int targetRank = rankResultList.get(1);
          if (req.getWin()) {
            res.addAllRewards(
                PlayerHelper.addResources(
                    player,
                    GameUtil.arrayAddition(
                        GlobalConst.DaDaoChallengeCoin,
                        player.getWelfareValue(WelfareTypeEnum.DadaoFriendAddition)),
                    OpType.DA_DAO_WIN));
          }
          if (module.playNum <= GlobalConst.DaDaoBrawlPoint.length) {
            res.addAllRewards(
                PlayerHelper.addResources(
                    player,
                    GameUtil.arrayAddition(
                        GlobalConst.DaDaoBrawlPoint[module.playNum - 1],
                        player.getWelfareValue(WelfareTypeEnum.DadaoFriendAddition)),
                    OpType.DA_DAO_JOIN));
          }
          client.sendProtocol(res);
          module.setInBattlePlayer(null);
          module
              .getRankList(player.getPlayerId(), Long.parseLong(req.getTargetId()))
              .thenAccept(
                  (rankResultList1) -> {
                          GameLogger.pvpfight(
                              player,
                              false,
                              selfRank,
                              rankResultList1.get(0),
                              DungeonTypeEnum.CHAPTER_TYPE_DA_DAO.getId(),
                              targetPlayer,
                              targetRank,
                              rankResultList1.get(1),
                              req.getBattleTime(),
                              req.getEndType(),
                              req.getWin());
                  });

          return null;
        });
  }

  /** 获取大道争锋信息 */
  public static void getInfo(NetClient client, Object o) {
    BattleMsg.BattlePvPInfoResponse_13000118.Builder res =
        BattleMsg.BattlePvPInfoResponse_13000118.newBuilder();
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    if (!player.isFuncOpen(InitialUI.AvenueBattle)) {
      client.sendProtocol(res, ErrorMsgEnum.func_not_open.ID);
      return;
    }
    OfflineBattleModule module = player.getOfflineBattleModule();
    module.checkAndInit();

    res.setNum(module.playNum);
    res.setNextSeasonTimer((int) (module.nextSeasonTimer / 1000L));
    res.setSettlementDayTimer((int) (module.getDaySettlementTimer() / 1000L));
    res.setSettlementSeasonTimer((int) (module.getSeasonSettlementTimer() / 1000L));
    res.setBuyNum(module.buyNum);
    res.putAllSecretscriptMap(player.getSecretscriptModule().getPvPSecretscriptMap());
    res.setFreeRefreshNum(module.freeRefreshNum);
    res.setRefreshNum(module.costRefreshNUm);

    List<Integer> scoreList = new ArrayList<>();
    if (!module.tempRefreshList.isEmpty()) {
      module.tempRefreshList.forEach(
          simplePlayer -> {
            res.addTargetList(simplePlayer.toSimplePlayerInfo());
          });
      module
          .getSerachTargetScoreList(module.tempRefreshList, scoreList)
          .thenAccept(
              v -> {
                res.addAllScoreList(scoreList);
                client.sendProtocol(res);
              });
    } else if (!module.matchRefreshTargetList.isEmpty()) {
      List<SimplePlayer> simplePlayerList = new ArrayList<>();
      PlayerManager.getInstance()
          .batchGetSimplePlayerListFromRedisAsync(module.matchRefreshTargetList)
          .onSuccess(
              findList -> {
                for (int i = 0; i < findList.size(); i++) {
                  SimplePlayer simplePlayer = findList.get(i);
                  if (simplePlayer == null) {
                    NPCConfig npcConfig =
                        NPCManager.instance().get(module.matchRefreshTargetList.get(i).intValue());
                    simplePlayer = SimplePlayer.makeByNpcConfig(npcConfig);
                  }
                  res.addTargetList(simplePlayer.toSimplePlayerInfo());
                  simplePlayerList.add(simplePlayer);
                }
                module
                    .getSerachTargetScoreList(simplePlayerList, scoreList)
                    .thenAccept(
                        v1 -> {
                          res.addAllScoreList(scoreList);
                          client.sendProtocol(res);
                        });
              })
          .onFailure(
              e -> {
                e.printStackTrace();
                client.sendProtocol(res);
              });
    } else {
      client.sendProtocol(res);
    }
  }

  /** 购买挑战券 请求 */
  public static void buyTime(NetClient client, Object o) {
    BattleMsg.BattleBuyPvPTimeResponse_13000122.Builder res =
        BattleMsg.BattleBuyPvPTimeResponse_13000122.newBuilder();
    Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
    if (!player.isFuncOpen(InitialUI.AvenueBattle)) {
      client.sendProtocol(res, ErrorMsgEnum.func_not_open.ID);
      return;
    }
    OfflineBattleModule module = player.getOfflineBattleModule();
    if (!module.isPlay()) {
      client.sendProtocol(res, ErrorMsgEnum.da_dao_not_play.ID);
      return;
    }
    if (module.buyNum >= GlobalConst.DaDaoChallengeTicketCost.length) {
      client.sendProtocol(res, ErrorMsgEnum.da_dao_play_num_not_enough.ID);
      return;
    }
    int[] costs = GlobalConst.DaDaoChallengeTicketCost[module.buyNum];
    if (PlayerHelper.delResources(player, costs, OpType.DA_DAO_Buy)) {
      module.buyNum++;

      List<RewardMsg.RewardInfo> drops =
          PlayerHelper.addResources(player, DA_DAO_TICK_ITEM_ID, 1, OpType.DA_DAO_Buy, true);
      res.setBuyNum(module.buyNum);
      res.addAllRewards(drops);
      client.sendProtocol(res);
    } else {
      client.sendProtocol(res, ErrorMsgEnum.resource_not_enough.ID);
    }
  }
}
