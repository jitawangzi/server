package cn.game.games.net.game.module.pvp;

import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg;

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
    module
        .searchTargetList(req.getRefreshFlag())
        .onSuccess(
            result -> {
              result.forEach(
                  simplePlayer -> {
                    res.addTargetList(simplePlayer.toSimplePlayerInfo());
                  });
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
    if (module.getInBattlePlayer() != null) {
      client.sendProtocol(res, ErrorMsgEnum.da_dao_in_battle.ID);
      return;
    }
    boolean match =
        module.tempRefreshList.stream()
            .anyMatch(
                simplePlayer -> {
                  return simplePlayer.id == req.getTargetId();
                });
    if (!match) {
      client.sendProtocol(res, ErrorMsgEnum.da_dao_not_found_target_Player.ID);
      return;
    }
    res.setSelfAttrs(player.getAttrModule().buildBattleAttrs());
    BattleMsg.PlayerBattleAttrs targetAttrs = module.getTargetPlayerAttrs(req.getTargetId());
    if (targetAttrs != null) {
      res.setTargetAttrs(targetAttrs);
    }
    client.sendProtocol(res);
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
    module.playNum++;
    if (module.playNum > GlobalConst.DaDaoFreeCnt) {
      module.buyNum--;
      PlayerHelper.delResources(player, DA_DAO_TICK_ITEM_ID, 1, OpType.DA_DAO_JOIN, true);
    }
    module
        .updateScore(req.getWin(), res)
        .onComplete(
            result -> {
              if (req.getWin()) {
                res.addAllDrops(
                    PlayerHelper.addResources(
                        player, GlobalConst.DaDaoChallengeCoin, OpType.DA_DAO_WIN));
              }
              if (module.playNum <= GlobalConst.DaDaoBrawlPoint.length) {
                res.addAllDrops(
                    PlayerHelper.addResources(
                        player,
                        GlobalConst.DaDaoBrawlPoint[module.playNum - 1],
                        OpType.DA_DAO_JOIN));
              }
              module.setInBattlePlayer(null);
              client.sendProtocol(res);
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
    res.setNum(module.playNum);
    res.setNextSeasonTimer((int) (module.nextSeasonTimer / 1000L));
    res.setSettlementDayTimer((int) (module.getDaySettlementTimer() / 1000L));
    res.setSettlementSeasonTimer((int) (module.getSeasonSettlementTimer() / 1000L));
    res.setBuyNum(module.buyNum);
    client.sendProtocol(res);
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
    int maxPlayConfigNum = GlobalConst.DaDaoFreeCnt + GlobalConst.DaDaoChallengeTicketCost.length;
    if (module.playNum >= maxPlayConfigNum) {
      client.sendProtocol(res, ErrorMsgEnum.da_dao_play_num_not_enough.ID);
      return;
    }
    if (module.buyNum >= GlobalConst.DaDaoChallengeTicketCost.length) {
      client.sendProtocol(res, ErrorMsgEnum.da_dao_play_num_not_enough.ID);
      return;
    }
    int[] costs = GlobalConst.DaDaoChallengeTicketCost[module.buyNum];
    if (PlayerHelper.delResources(player, costs, OpType.DA_DAO_Buy)) {
      module.buyNum++;
      PlayerHelper.addResources(player, DA_DAO_TICK_ITEM_ID, 1);
      res.setCurNum(module.buyNum);
      client.sendProtocol(res);
    }else {
      client.sendProtocol(res, ErrorMsgEnum.resource_not_enough.ID);

    }
  }
}
