package cn.game.games.net.game.module.pvp;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.SimplePlayer;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.*;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.NPCManager;
import cn.game.protocol.protobuf.BattleMsg;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.util.DateUtil;
import cn.game.util.RedisUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.commons.lang.math.RandomUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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

  /** 加入 大道争锋 标识 */
  @JsonIgnore boolean joinFlag;

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
    return new EventTypeEnum[] {EventTypeEnum.NewDay, EventTypeEnum.LoginFinish};
  }

  @Override
  public void handleEvent(GameEvent event) {
    switch (event.getType()) {
      case NewDay -> {
        clear();
        tryResetSeasonData();
        break;
      }
      case LoginFinish -> {
        clear();
        break;
      }
    }
  }

  public int getPlayNum() {
    return playNum;
  }

  public void setPlayNum(int playNum) {
    this.playNum = playNum;
  }

  public boolean isJoinFlag() {
    return joinFlag;
  }

  public void setJoinFlag(boolean joinFlag) {
    this.joinFlag = joinFlag;
  }

  public long getDaySettlementTimer() {
    return DateUtil.getDayTimeBySet(22, 0, 0);
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
    player.updateOfflineAttrData();
  }

  private void tryResetSeasonData() {
    long now = System.currentTimeMillis();
    if (now > nextSeasonTimer) {
      clear();
      nextSeasonTimer = DateUtil.addWeek(1);
      addDayRankScore(player.getPlayerId(), player.getServerId(), GlobalConst.DaDaoStartupPoint);
      addSeasonRankScore(player.getPlayerId(), player.getServerId(), GlobalConst.DaDaoStartupPoint);
    }
  }

  public boolean isJoin() {
    return System.currentTimeMillis() < nextSeasonTimer;
  }

  private void clear() {
    tempRefreshList.clear();
    joinFlag = false;
    playNum = 0;
    buyNum = 0;
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

  public Future<List<SimplePlayer>> searchTargetList(boolean refreshFlag) {
    if (refreshFlag) {
      usedPidList.clear();
      tempRefreshList.clear();
    }
    setInBattlePlayer(null);
    Promise<List<SimplePlayer>> promise = Promise.promise();
    List<SimplePlayer> resultList = new ArrayList<>();
    CompletionStage<int[]> selfRankInfo = getSelfRankInfo();
    selfRankInfo
        .thenCompose(
            rankArr -> {
              int rank = rankArr[0];
              int score = rankArr[1];
              if (score == 0) {
                score = GlobalConst.DaDaoStartupPoint;
              }
              int[][] scoreRange = getScoreRange(rank);
              final int fianlScore = score;
              return getMatchPidMapFuture(scoreRange, fianlScore);
            })
        .thenAccept(
            mapResult -> {
              processMatchResults(mapResult, resultList, promise);
            })
        .exceptionally(
            err -> {
              err.printStackTrace();
              promise.fail(err);
              return null;
            });
    return promise.future();
  }

  private void processMatchResults(
      Map<Integer, SimplePlayer> mapResult,
      List<SimplePlayer> resultList,
      Promise<List<SimplePlayer>> promise) {
    for (int i = 0; i < GlobalConst.DaDaoOpponentPicking.length; i++) {
      SimplePlayer simplePlayer = mapResult.get(i);
      if (simplePlayer != null) {
        addFindPlayer(resultList, simplePlayer, promise);
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
              addFindPlayer(resultList, npcPlayer, promise);
            });
  }

  private CompletableFuture<Map<Integer, SimplePlayer>> getMatchPidMapFuture(
      int[][] scoreRange, int fianlScore) {
    List<List<Long>> matchList = new CopyOnWriteArrayList<>(new ArrayList<>(scoreRange.length));
    List<CompletableFuture<Collection<Long>>> futureList = new ArrayList<>();
    Map<Integer, SimplePlayer> matchSimplePlayerMap = new TreeMap<>();
    for (int i = 0; i < scoreRange.length; i++) {
      matchList.add(new ArrayList<>());
      int minScore = fianlScore * scoreRange[i][0] / 10000;
      int maxScore = fianlScore * scoreRange[i][1] / 10000;
        futureList.add(getTargetIdByScore(minScore, maxScore, i, matchList).toCompletableFuture());
    }
    return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
        .thenCompose(
            v -> {
              return processMatchList(matchList, matchSimplePlayerMap);
            });
  }

  private CompletionStage<Map<Integer, SimplePlayer>> processMatchList(
      List<List<Long>> matchList, Map<Integer, SimplePlayer> matchSimplePlayerMap) {
    List<Long> finalPidList =
        matchList.stream()
            .flatMap(Collection::stream)
            .distinct()
            .filter(pid -> !usedPidList.contains(pid) && pid != playerId)
            .collect(Collectors.toList());
    return PlayerManager.getInstance()
        .batchGetSimplePlayerFromRedisAsync(finalPidList)
        .thenApply(
            map -> {
              for (int i = 0; i < finalPidList.size(); i++) {
                SimplePlayer player = map.get(finalPidList.get(i));
                if (player != null) {
                  matchSimplePlayerMap.put(i, player);
                }
              }
              return matchSimplePlayerMap;
            });
  }

  private CompletionStage<int[]> getSelfRankInfo() {
    CompletionStage<Long> scoreStage = getSelfScore();
    CompletionStage<Integer> rankStage =
        RankService.getInstance()
            .getRankAsync(player.getServerId(), RankType.DaDaoZhengFengDay, player.getPlayerId());
    return rankStage.thenCombine(
        scoreStage,
        (rank, selfScore) -> {
          return new int[] {rank, selfScore.intValue()};
        });
  }

  private void addFindPlayer(
      List<SimplePlayer> resultList,
      SimplePlayer simplePlayer,
      Promise<List<SimplePlayer>> promise) {
    resultList.add(simplePlayer);
    usedPidList.add(simplePlayer.id);
    if (resultList.size() >= GlobalConst.DaDaoOpponentPicking.length) {
      tempRefreshList.addAll(resultList);
      promise.complete(resultList);
    }
  }

  private int[][] getScoreRange(Integer rank) {
    if (rank > 5) {
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

  public SimplePlayer getTargetPlayer(long targetId) {
    NPCConfig npcConfig = NPCManager.instance().get((int) targetId);
    SimplePlayer battleTargetPlayer = null;
    for (SimplePlayer simplePlayer : tempRefreshList) {
      if (simplePlayer.id == targetId) {
        setInBattlePlayer(simplePlayer);
        battleTargetPlayer = simplePlayer;
        break;
      }
    }
    if (npcConfig != null) {
      return null;
    }
    return battleTargetPlayer;
  }

  public Future<Void> updateScore(
      boolean win, long targetId, BattleMsg.BattlePvPEndResponse_13000116.Builder res) {
    int selfAddScore = 0, targetAddScore = 0;
    int targetIndex = 4;
    NPCConfig npcConfig = null;
    for (int i = 0; i < tempRefreshList.size(); i++) {
      if (tempRefreshList.get(i).id == targetId) {
        targetIndex = i;
        npcConfig = NPCManager.instance().get((int) targetId);
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
    if (npcConfig == null) {
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
}
