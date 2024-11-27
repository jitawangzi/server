package cn.game.games.net.game.module.pvp;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.SimplePlayer;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.rank.RankEntry;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.*;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.NPCManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.commons.lang.math.RandomUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 * @ClassName OfflineBattleModule
 *
 * @description: 大道争锋
 * @author: ly
 * @create: 2024-09-07 13:58 @Version 1.0
 */
public class OfflineBattleModule extends BasePlayerModule {
  /** 本次刷新 已经刷出来过的 用户id集合 */
  @JsonIgnore final List<Long> usedPidList = new ArrayList<>();

  /** 今天挑战次数 */
  int playNum;

  /** 购买的次数 */
  int buyNum;

  long nextSeasonTimer;
  /**
   * 领取免费挑战券的时间戳
   */
  long lastRewardTickerTimer;

  /**
   * 免费刷新次数
   */
  int freeRefreshNum;
  /**
   * 付费刷新次数
   */
  int costRefreshNUm;
  /*
   * 本次挑战刷新出来的5个对手 PId
   */
  List<Long> matchRefreshTargetList = new ArrayList<>();


  /** 本次挑战刷新出来的5个对手 */
  @JsonIgnore List<SimplePlayer> tempRefreshList = new ArrayList<>();

  /** 正在挑战中用户 断线重连 则重置 */
  @JsonIgnore SimplePlayer inBattlePlayer;

  @Override
  public void buildPlayerAllInfo(PlayerMsg.PlayerAllInfo.Builder builder) {}

  @Override
  public void initFromDbAfter() {
    clear();
  }

  @Override
  public EventTypeEnum[] getEventTypes() {
    return new EventTypeEnum[] {EventTypeEnum.NewDay, EventTypeEnum.refresh,EventTypeEnum.FuncOpen};
  }

  @Override
  public void handleEvent(GameEvent event) {
    switch (event.getType()) {
      case NewDay -> {
        clear();
        clearRefreshNum();
        matchRefreshTargetList.clear();
        if (isJoin()){
          checkAndAddTicker();
          tryResetSeasonData();
        }
        break;
      }
      case refresh -> {
        clearTempTarget();
        checkAndAddTicker();
        break;
      }
      case FuncOpen -> {
        InitialUI openFucntion = (InitialUI) event.getParameter(0);
        if (openFucntion ==  InitialUI.AvenueBattle) {
          checkAndInit();
        }
        break;
      }
    }
  }

  private void checkAndAddTicker() {
    if (!isJoin()){
      return;
    }
    long now = System.currentTimeMillis();
    if (!DateUtil.isSameDay(now, lastRewardTickerTimer)){
      PlayerHelper.addResources(player, OfflineBattleHandler.DA_DAO_TICK_ITEM_ID, GlobalConst.DaDaoFreeCnt, OpType.DA_DAO_FREE_ADD,true);
      lastRewardTickerTimer = now;
    }
  }

  public int getPlayNum() {
    return playNum;
  }

  public void setPlayNum(int playNum) {
    this.playNum = playNum;
  }


    public long getDaySettlementTimer() {
      RankConfig config = RankManager.instance().get(9);
      int hour = 23, minute =40;
      if (config != null && config.RewardTime1.length > 2){
        hour = config.RewardTime1[0];
        minute = config.RewardTime1[1];
      }
      return DateUtil.getDayTimeBySet(hour, minute, 0);
    }

  public long getSeasonSettlementTimer() {
    long weekEndTimer = nextSeasonTimer - 1000L;
    return DateUtil.getTimeBySet(weekEndTimer, 22, 0, 0);
  }

  public CompletionStage<Double> addDayRankScore(long pid, String serverId, int score) {
    return RankService.getInstance()
        .updateScoreAsync(serverId, RankType.DaDaoZhengFengDay, pid, score);
  }

  public CompletionStage<Double> addSeasonRankScore(long pid, String serverId, int score) {
    return RankService.getInstance()
        .updateScoreAsync(serverId, RankType.DaDaoZhengFengSeason, pid, score);
  }

  public void joinPlay() {
    tryResetSeasonData();
  }

  private void tryResetSeasonData() {
    long now = System.currentTimeMillis();
    if (now > nextSeasonTimer) {
      clear();
      nextSeasonTimer = DateUtil.addWeekBeginTimer(1);
      addDayRankScore(player.getPlayerId(), player.getServerId(), GlobalConst.DaDaoStartupPoint);
      addSeasonRankScore(player.getPlayerId(), player.getServerId(), GlobalConst.DaDaoStartupPoint);
      log.info(String.format("tryResetSeasonData pid:%s",player.getPlayerId()));
    }
  }

  public boolean isJoin() {
    return System.currentTimeMillis() < nextSeasonTimer;
  }

  public void checkAndInit(){
    if (!isJoin()){
      joinPlay();
      checkAndAddTicker();
    }
  }

  private void clear() {
    playNum = 0;
    buyNum = 0;
    clearTempTarget();
  }

  private void clearTempTarget() {
    tempRefreshList.clear();
    setInBattlePlayer(null);
  }

  public SimplePlayer getInBattlePlayer() {
    return inBattlePlayer;
  }

  public void setInBattlePlayer(SimplePlayer inBattlePlayer) {
    this.inBattlePlayer = inBattlePlayer;
  }

  public boolean isPlay() {
    long now = System.currentTimeMillis();
    long nextDay = DateUtil.nextDayStartTime(1);
    boolean play = now >= getDaySettlementTimer() && now <= nextDay;
    return !play;
  }

  public Future<List<SimplePlayer>> searchTargetList(boolean refreshFlag, List<Integer> scoreList) {

    if (refreshFlag) {
      usedPidList.clear();
    }
    tempRefreshList.clear();
    matchRefreshTargetList.clear();
    setInBattlePlayer(null);
    Promise<List<SimplePlayer>> promise = Promise.promise();
    List<SimplePlayer> resultList = new ArrayList<>();
    CompletionStage<RankEntry> selfRankInfo = getSelfRankInfo();
    selfRankInfo
        .thenCompose(
            rankEntry -> {
              int rank = 0;
              int score = GlobalConst.DaDaoStartupPoint;
              if (rankEntry != null) {
                rank = rankEntry.getRank();
                score = (int) rankEntry.getScore() == 0 ? GlobalConst.DaDaoStartupPoint : (int) rankEntry.getScore();
              }
              int[][] scoreRange = getScoreRange(rank);
              final int finalScore = score;
              return getMatchPidMapFuture(scoreRange, finalScore);
            })
        .thenAccept(
            mapResult -> {
              processMatchResults(mapResult, resultList, promise);
            })
        .thenCompose(
            msg -> {
               return  getSerachTargetScoreList(resultList, scoreList);
            })
        .thenAccept(
            action -> {
              if (resultList.size() >= GlobalConst.DaDaoOpponentPicking.length) {
                tempRefreshList.addAll(resultList);
              }
              promise.complete(resultList);
            })
        .exceptionally(
            err -> {
              err.printStackTrace();
              promise.fail(err);
              return null;
            });
    return promise.future();
  }

   CompletionStage<Void> getSerachTargetScoreList(
      List<SimplePlayer> resultList, List<Integer> scoreList) {
    CompletableFuture completableFuture = new CompletableFuture<>();
    List<CompletableFuture<Long>> futureList = new ArrayList<>();
    resultList.forEach(
        targetPlayer -> {
          NPCConfig npcConfig = NPCManager.instance().getNullable((int) targetPlayer.getId());
          if (npcConfig != null) {
            scoreList.add(npcConfig.Integral);
          } else {
            scoreList.add(0);
            final int index = scoreList.size() - 1;
            CompletableFuture<Long> scoreFindFuture =
                RankService.getInstance()
                    .getScoreAsync(
                        player.getServerId(), RankType.DaDaoZhengFengDay, targetPlayer.getId())
                    .toCompletableFuture();
            futureList.add(scoreFindFuture);
            scoreFindFuture.thenAccept(
                score -> {
                  scoreList.set(index, score.intValue());
                });
          }
        });
    CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
        .whenComplete(
            (action, err) -> {
              if (err != null) {
                err.printStackTrace();
                completableFuture.completeExceptionally(err);
                return;
              }
              completableFuture.complete(null);
            });
    return completableFuture;
  }

  private void processMatchResults(
      Map<Integer, SimplePlayer> mapResult,
      List<SimplePlayer> resultList,
      Promise<List<SimplePlayer>> promise) {
    for (int i = 0; i < GlobalConst.DaDaoOpponentPicking.length; i++) {
      SimplePlayer simplePlayer = mapResult.get(i);
      if (simplePlayer != null) {
        addFindPlayer(resultList, simplePlayer);
      } else {
        matchNpcPlayer(resultList, promise);
      }
    }
  }

  private void matchNpcPlayer(List<SimplePlayer> resultList, Promise<List<SimplePlayer>> promise) {
    NPCManager.instance().list().stream()
        .filter(config -> !usedPidList.contains((long) config.ID))
        .findAny()
        .ifPresent(
            npcConfig -> {
              SimplePlayer npcPlayer = SimplePlayer.makeByNpcConfig(npcConfig);
              addFindPlayer(resultList, npcPlayer);
            });
  }

  private CompletableFuture<Map<Integer, SimplePlayer>> getMatchPidMapFuture(
      int[][] scoreRange, int fianlScore) {
    List<List<Long>> matchList = new CopyOnWriteArrayList<>(new ArrayList<>(scoreRange.length));
    List<CompletableFuture<Collection<Long>>> futureList = new ArrayList<>();
    for (int i = 0; i < scoreRange.length; i++) {
      matchList.add(new ArrayList<>());
      int minScore = fianlScore * scoreRange[i][0] / 10000;
      int maxScore = fianlScore * scoreRange[i][1] / 10000;
      futureList.add(getTargetIdByScore(minScore, maxScore, i, matchList).toCompletableFuture());
      log.info(String.format("getMatchPidMapFuture selfScore:%d,  minScore:%d, maxScore:%d scoreRange:[%d:%d]", fianlScore,minScore,maxScore, scoreRange[i][0], scoreRange[i][1]));
    }
    return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
        .thenCompose(
            v -> {
              return processMatchList(matchList);
            });
  }

  private CompletionStage<Map<Integer, SimplePlayer>> processMatchList(
      List<List<Long>> matchList) {
    List<Long> finalPidList = new ArrayList<>();
    matchList.forEach(
        list -> {
          Optional<Long> findPid = list.stream().filter(pid -> !usedPidList.contains(pid) && pid != playerId).findAny();
          finalPidList.add(findPid.isPresent() ? findPid.get() : 0L);
        });
    CompletableFuture<Map<Integer, SimplePlayer>> completableFuture = new CompletableFuture<>();
    PlayerManager.getInstance().batchGetSimplePlayerListFromRedisAsync(finalPidList).onSuccess(
            list ->{
              Map<Integer,SimplePlayer> map = new HashMap<>();
              for(int i = 0; i < list.size(); i++) {
                SimplePlayer simplePlayer = list.get(i);
                if (simplePlayer != null){
                  map.put( finalPidList.indexOf(simplePlayer.id),simplePlayer);
                } else {
                  map.put(i,null);
                }
              }
              completableFuture.complete(map);
            }
    ).onFailure( err ->{
      completableFuture.completeExceptionally(err);
    });
    return completableFuture;

  }

  private CompletionStage<RankEntry> getSelfRankInfo() {
    return RankService.getInstance()
        .getRankEntryAsync(player.getServerId(), RankType.DaDaoZhengFengDay, playerId);
  }

  private void addFindPlayer(List<SimplePlayer> resultList, SimplePlayer simplePlayer) {
    resultList.add(simplePlayer);
    matchRefreshTargetList.add(simplePlayer.id);
    usedPidList.add(simplePlayer.id);
  }

  private int[][] getScoreRange(Integer rank) {
    if (rank > 5 || rank == -1) {
      return GlobalConst.DaDaoOpponentPicking;
    }
    int[][] outRangeScoreArr = new int[5][2];
    if (rank == 1) {
      for (int i = 0; i < outRangeScoreArr.length; i++) {
        int[] randomArr =
            RandomUtils.nextBoolean()
                ? GlobalConst.DaDaoOpponentPicking[3]
                : GlobalConst.DaDaoOpponentPicking[4];
        outRangeScoreArr[i][0] = randomArr[0];
        outRangeScoreArr[i][1] = randomArr[1];
      }
    } else if (rank == 2) {
      outRangeScoreArr[0][0] = GlobalConst.DaDaoOpponentPicking[2][0];
      outRangeScoreArr[0][1] = GlobalConst.DaDaoOpponentPicking[0][1];
      for (int i = 1; i < outRangeScoreArr.length; i++) {
        int[] randomArr =
            RandomUtils.nextBoolean()
                ? GlobalConst.DaDaoOpponentPicking[3]
                : GlobalConst.DaDaoOpponentPicking[4];
        outRangeScoreArr[i][0] = randomArr[0];
        outRangeScoreArr[i][1] = randomArr[1];
      }
    } else if (rank == 3) {
      outRangeScoreArr[0] = GlobalConst.DaDaoOpponentPicking[0];
      outRangeScoreArr[1] = GlobalConst.DaDaoOpponentPicking[1];
      for (int i = 2; i < outRangeScoreArr.length; i++) {
        int[] randomArr =
            RandomUtils.nextBoolean()
                ? GlobalConst.DaDaoOpponentPicking[3]
                : GlobalConst.DaDaoOpponentPicking[4];
        outRangeScoreArr[i][0] = randomArr[0];
        outRangeScoreArr[i][1] = randomArr[1];
      }
    } else if (rank == 4 || rank == 5) {
      outRangeScoreArr[0] = GlobalConst.DaDaoOpponentPicking[0];
      outRangeScoreArr[1] = GlobalConst.DaDaoOpponentPicking[1];
      outRangeScoreArr[2] = GlobalConst.DaDaoOpponentPicking[2];
      for (int i = 3; i < outRangeScoreArr.length; i++) {
        int[] randomArr =
            RandomUtils.nextBoolean()
                ? GlobalConst.DaDaoOpponentPicking[3]
                : GlobalConst.DaDaoOpponentPicking[4];
        outRangeScoreArr[i][0] = randomArr[0];
        outRangeScoreArr[i][1] = randomArr[1];
      }
    }
    return outRangeScoreArr;
  }

  private CompletionStage<Collection<Long>> getTargetIdByScore(
      int minScore, int maxScore, int index, List<List<Long>> matchList) {
    return RankService.getInstance()
        .searchRankEntryByScoreAsync(
            player.getServerId(), RankType.DaDaoZhengFengDay, minScore, maxScore, 10)
        .thenApply(
            (rankPids) -> {
              if (rankPids != null && !rankPids.isEmpty()) {
                matchList.get(index).addAll(rankPids);
              }
              return rankPids;
            });
  }

  private CompletionStage<Long> getSelfScore() {
    return RankService.getInstance()
        .getScoreAsync(player.getServerId(), RankType.DaDaoZhengFengDay, player.getPlayerId());
  }

  public  Future<SimplePlayer> getTargetPlayer(long targetId) {
    NPCConfig npcConfig = NPCManager.instance().getNullable((int) targetId);
    Promise<SimplePlayer> promise = Promise.promise();
    SimplePlayer battleTargetPlayer = null;
    for (SimplePlayer simplePlayer : tempRefreshList) {
      if (simplePlayer.id == targetId) {
        battleTargetPlayer = simplePlayer;
        break;
      }
    }
    if (npcConfig != null) {
      promise.complete(null);
      return null;
    }
    if (battleTargetPlayer == null){//未找到 从redis 加载
      PlayerManager.getInstance().getSimplePlayerFromRedisAsync(targetId).onSuccess(
              findPlayer ->{
                if (findPlayer != null) {
                  setInBattlePlayer(findPlayer);
                }
                promise.complete(findPlayer);
              }
      ).onFailure(err ->{
        err.printStackTrace();
        promise.complete(null);
      });
    } else {//直接用本地缓存
      setInBattlePlayer(battleTargetPlayer);
      promise.complete(battleTargetPlayer);
    }
    return promise.future();
  }

  public SimplePlayer getTargetPlayer(String targetId) {
    if (inBattlePlayer != null) {
      return inBattlePlayer;
    } else {
      NPCConfig npcConfig = NPCManager.instance().get(Integer.parseInt(targetId));
      return SimplePlayer.makeByNpcConfig(npcConfig);
    }
    }

  public Future<Void> updateScore(
      boolean win, long targetId, BattleMsg.BattlePvPEndResponse_13000116.Builder res) {
//    matchRefreshTargetList.clear();
//    tempRefreshList.clear();
    clearRefreshNum();
    int selfAddScore = 0, targetAddScore = 0;
    int targetIndex = 4;
    NPCConfig npcConfig = null;
    for (int i = 0; i < tempRefreshList.size(); i++) {
      if (tempRefreshList.get(i).id == targetId) {
        targetIndex = i;
        npcConfig = NPCManager.instance().getNullable((int) targetId);
        break;
      }
    }
    if (win) {
      selfAddScore = GlobalConst.DaDaoActiveChallenge[targetIndex][0];
      targetAddScore = GlobalConst.DaDaoPassiveChallenge[targetIndex][1];
    } else {
      targetAddScore = GlobalConst.DaDaoPassiveChallenge[targetIndex][0];
      selfAddScore = GlobalConst.DaDaoActiveChallenge[targetIndex][1];
    }
    // 修改自己的积分
    addSeasonRankScore(player.getPlayerId(), player.getServerId(), selfAddScore);
    CompletionStage<Double> selfStage =
        addDayRankScore(player.getPlayerId(), player.getServerId(), selfAddScore);
    // 修改对方的积分
    CompletionStage<Double> targetStage = getDoubleCompletionStage();
    if (npcConfig == null && inBattlePlayer != null) {
      targetStage = addDayRankScore(inBattlePlayer.id, player.getServerId(), targetAddScore);
      addSeasonRankScore(inBattlePlayer.id, player.getServerId(), targetAddScore);
    }

    Promise<Void> promise = Promise.promise();
    selfStage.thenCombine(
        targetStage,
        (selfScore, targetScore) -> {
          res.setSelfScore(selfScore.intValue());
          if (targetScore != null) {
            res.setTargetScore(targetScore.intValue());
          }
          promise.complete();
          return null;
        });
    return promise.future();
  }

  private CompletionStage<Double> getDoubleCompletionStage() {
    Promise<Double> targetVoidPromise = Promise.promise();
    targetVoidPromise.complete(0.0);
    return targetVoidPromise.future().toCompletionStage();
  }

  public CompletableFuture<List<Integer>> getRankList(long selfPid, long targetPid){
    CompletableFuture<Integer> selfRankFuture = RankService.getInstance().getRankAsync(player.getServerId(), RankType.DaDaoZhengFengDay, selfPid).toCompletableFuture();
    CompletableFuture<Integer> targetRankFuture = RankService.getInstance().getRankAsync(player.getServerId(), RankType.DaDaoZhengFengDay, targetPid).toCompletableFuture();
    return targetRankFuture.thenCombine(selfRankFuture, (targetRank, selfRank) -> {
      List<Integer> rankList = new ArrayList<>();
      rankList.add(selfRank);
      rankList.add(targetRank);
      return rankList;
    });
  }

  void clearRefreshNum(){
    freeRefreshNum = 0;
    costRefreshNUm = 0;
  }
}
