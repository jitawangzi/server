package cn.game.games.net.game.module.battle;

import cn.game.core.cache.CacheType;
import cn.game.games.core.ResultObject;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.rank.PlayerRank;
import cn.game.games.net.game.module.rank.RankEntry;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.BaseMsg;
import cn.game.protocol.protobuf.BattleMsg;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;
import cn.game.util.RedisUtil;
import cn.game.util.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * PVEVP  大圣擂台
 * 2025年8月4日 15:35:04
 *
 * @author QYK
 */
public class PVEVPBattle extends XiYouBattleHandler {

    /**
     * 界缓存的 这一次刷新行为中缓存的玩家 暂不使用
     *
     * @param rankType
     * @return
     */
    private transient Map<Integer, PlayerRank> cachePlayerRankMap = new ConcurrentHashMap<>();
    /**
     * 界面上显示的那4个人
     *
     * @param rankType
     * @return
     */
    private Map<Integer, PlayerRank> mainShowRank = new ConcurrentHashMap<>();
    private transient RankEntry inBattleRank;
    private transient RankEntry myRank;
    private transient int refreshTime = 0;
    private int ticketCount = 0;
    private int endTime = 0;
    public transient List<BaseMsg.PVEVPRecordData> recordDataList = new ArrayList<>();

    // 限制最大记录数，防止无限增长
    final int MAX_RECORDS = 50;
    public PVEVPBattle() {
    }


    @Override
    void newDay() {
    }


    void newWeek() {

    }

    /**
     * 每天重置数据
     */
    public void reset() {
        // 获取下周一凌晨的时间戳（毫秒）
        long nextMondayMillis = DateUtil.addWeekBeginTimer(1);
        // 转换为秒级时间戳
        endTime= (int)(nextMondayMillis / 1000);
        ticketCount=3;
    }


    @Override
    public int checkCustom(int id, int subId, long... args) {

        if (ticketCount <= 0) {
            return ErrorMsgEnum.PVEVP_No_Ticket.getId();
        }
        if (!mainShowRank.containsKey(subId)) {
            return ErrorMsgEnum.PVEVP_No_Player.getId();
        }
//        if (endTime <= DateUtil.currentTimeSeconds()) {
//            return ErrorMsgEnum.PVEVP_Season_Over.getId();
//        }
        inBattleRank = mainShowRank.get(subId).getRankEntry();
        return 0;
    }

    @Override
    public int battleStart(int id, int subId) {
        return 0;
    }

    @Override
    public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request) {
        BattleModule battleModule = player.getModule(BattleModule.class);
        if (request.getWin()) {
            BattleConfig battleConfig = BattleManager.instance().get(battleModule.getAttackingId());
            List<RewardInfo> allRewards = new ArrayList<>();
            // List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.FirstPassReward, OpType.BattleEnd);
            // allRewards.addAll(reward);
            ticketCount--;

            // 胜利后两人积分交换
            long newScore = inBattleRank.getScore();
            long myScore = myRank.getScore();
            RankService.getInstance().setScoreAsync(player.getServerId(), RankType.Battle, player.getPlayerId(), newScore);
            RankService.getInstance().setScoreAsync(player.getServerId(), RankType.Battle, inBattleRank.getPlayerId(), myScore);
            // 生成战报
            boolean isRobot = false;
            if (isRobot) {
                createBattleRecord_Robot(null, (int) (newScore - myScore), true);
            } else {
                createBattleRecord_player(PlayerHelper.getSimplePlayer(inBattleRank.getPlayerId()), (int) (newScore - myScore), true);
            }
            resetCache();
            return ResultObject.success();
        } else { // 失败了，最终结算
            // 生成战报
            boolean isRobot = false;
            if (isRobot) {
                createBattleRecord_Robot(null, 0, false);
            } else {
                createBattleRecord_player(PlayerHelper.getSimplePlayer(inBattleRank.getPlayerId()), 0, false);
            }
            return ResultObject.success();
        }
    }

    void createBattleRecord_player(SimplePlayer simplePlayer, int change, boolean iswin) {
        BaseMsg.PVEVPRecordData.Builder recordData = BaseMsg.PVEVPRecordData.newBuilder();
        recordData.setBattleTime(DateUtil.currentTimeSeconds());
        recordData.setName(simplePlayer.getName());
        recordData.setHead(simplePlayer.getHead());
        recordData.setLevel(simplePlayer.getLevel());
        recordData.setHeadFrame(simplePlayer.getHeadFrame());
        recordData.setResult(iswin ? 1 : 0);
        recordData.setCombatEffectiveness(simplePlayer.getCombatEffectiveness());
        recordData.setScoreChange(Math.abs(change));
        BaseMsg.PVEVPRecordData record = recordData.build();

        String rediskey = CacheType.PVEVP_RECORD_ID.key(player.getPlayerId());
        // 使用 List 保持插入顺序
        RedisUtil.getRedis().getList(rediskey).add(record);
        RedisUtil.getRedis().getList(rediskey).trim(0, MAX_RECORDS - 1);
        // 给对方积分
        BaseMsg.PVEVPRecordData.Builder recordDataOther = BaseMsg.PVEVPRecordData.newBuilder();
        recordDataOther.setBattleTime(DateUtil.currentTimeSeconds());
        recordDataOther.setName(player.getPlayerName());
        recordDataOther.setHead(player.getData().getHead());
        recordDataOther.setLevel(player.getData().getLevel());
        recordDataOther.setHeadFrame(player.getData().getHeadFrame());
        recordDataOther.setResult(iswin ? 0 : 1);
        recordDataOther.setCombatEffectiveness(player.getAttrModule().getPower());
        recordDataOther.setScoreChange(Math.abs(change));
        BaseMsg.PVEVPRecordData record2 = recordData.build();

        String rediskey2 = CacheType.PVEVP_RECORD_ID.key(simplePlayer.getId());
        // 使用 List 保持插入顺序
        RedisUtil.getRedis().getList(rediskey2).add(record2);
        RedisUtil.getRedis().getList(rediskey2).trim(0, MAX_RECORDS - 1);

    }

    void createBattleRecord_Robot(SimplePlayer simplePlayer, int change, boolean iswin) {
        BaseMsg.PVEVPRecordData.Builder recordData = BaseMsg.PVEVPRecordData.newBuilder();
        recordData.setBattleTime(DateUtil.currentTimeSeconds());
        recordData.setName(simplePlayer.getName());
        recordData.setHead(simplePlayer.getHead());
        recordData.setLevel(simplePlayer.getLevel());
        recordData.setHeadFrame(simplePlayer.getHeadFrame());
        recordData.setResult(iswin ? 1 : 0);
        recordData.setCombatEffectiveness(simplePlayer.getCombatEffectiveness());
        recordData.setScoreChange(Math.abs(change));
        BaseMsg.PVEVPRecordData record = recordData.build();

        String rediskey = CacheType.PVEVP_RECORD_ID.key(player.getPlayerId());
        // 使用 List 保持插入顺序
        RedisUtil.getRedis().getList(rediskey).add(record);
    }

    void getBattleRecordFromRedis() {
        recordDataList.clear();
        String rediskey = CacheType.PVEVP_RECORD_ID.key(player.getPlayerId());
        RedisUtil.getRedis().getList(rediskey).forEach(recordData -> {
            recordDataList.add((BaseMsg.PVEVPRecordData) recordData);
        });
    }


    @Override
    public ResultObject<List<RewardInfo>> quickEnd(int id, int subId, boolean isWin) {
        return ResultObject.success();
    }

    @Override
    public int getType() {
        return DungeonTypeEnum.PVEVPBattle.getId();
    }

    /**
     * 重置缓存数据 用于战斗胜利后
     */
    private void resetCache() {
        cachePlayerRankMap.clear();
        mainShowRank.clear();
        inBattleRank = null;
    }

    /**
     * 获取随机的4个玩家  战斗胜利后  或者登录没数据
     *
     * @param myscore
     */
    private List<Integer> radomPlayer(int myscore) {
        List<Integer> rankIds = new ArrayList<>();
        if (myscore <= 4) {
            rankIds.add(1);
            rankIds.add(2);
            rankIds.add(3);
            rankIds.add(4);
        }
        int before = (int) (myscore * 0.7f);
        if (before >= myscore - 3) {
            before = myscore - 3;
        }
        rankIds.addAll(Rnd.generateRandomNumbers(before, myscore - 1, 3));
        int end = (int) (myscore * 1.1f);
        if (end <= myscore + 1) {
            end = myscore + 1;
        }
        rankIds.add(Rnd.get(myscore + 1, end));
        return rankIds;

    }

    public void fillMainShowRank(List<Integer> rankIds) {
        RankService rankService = RankService.getInstance();
        rankIds.forEach(rankId -> {
            var rankEntry = rankService.getRankEntry(player.getServerId(), RankType.Battle, rankId);
            setData(rankEntry);
        });
    }

    private void sendBattlePVEVPChallengeResponse_13000553() {

        BattleMsg.BattlePVEVPChallengeResponse_13000553.Builder resp = BattleMsg.BattlePVEVPChallengeResponse_13000553.newBuilder();
        mainShowRank.forEach((k, v) -> {
            //  resp.addPlayers(v);
        });
        player.getGameClient().sendProtocol(resp.build());
    }


    public CompletionStage<Void> fillMainShowRankAsync(List<Integer> rankIds) {
        if (rankIds == null || rankIds.isEmpty()) {
            return CompletableFuture.completedStage(null);
        }

        RankService rankService = RankService.getInstance();

        // 并行获取所有排名数据
        List<CompletionStage<Void>> updateTasks = rankIds.stream()
                .map(rankId -> rankService.getRankEntryAsync(player.getServerId(), RankType.Battle, rankId)
                        .thenAccept(this::setData))
                .toList();

        // 等待所有任务完成
        return CompletableFuture.allOf(updateTasks.toArray(new CompletableFuture[0]));
    }

    private void setData(RankEntry rankEntry) {
        var simplePlayer = PlayerHelper.getSimplePlayer(rankEntry.getPlayerId());
        var playerRank = new PlayerRank(rankEntry, simplePlayer);
        cachePlayerRankMap.put(rankEntry.getRank(), playerRank);
        mainShowRank.put(rankEntry.getRank(), playerRank);
    }

    public void getRadomPlayer(int type) {
        if (type == 1) {
            // 请求
            myRank = RankService.getInstance().getRankEntry(player.getServerId(), RankType.Battle, player.getPlayerId());
            CompletionStage<Void> dataLoadingStage;
            if (myRank.getScore() == 0) {
                // 新玩家，获取排行榜末尾玩家
                dataLoadingStage = RankService.getInstance()
                        .getLastNAsync(player.getServerId(), RankType.Battle, 4)
                        .thenAccept(rankEntries -> {
                            mainShowRank.clear();
                            cachePlayerRankMap.clear();
                            rankEntries.forEach(this::setData);
                        });
            } else {
                // 已有排名的玩家，获取随机对手
                List<Integer> targetRankIds;
                if (mainShowRank.size() > 0) {
                    // 使用现有排名ID更新数据
                    targetRankIds = new ArrayList<>(mainShowRank.keySet());
                } else {
                    // 生成新的随机排名ID
                    targetRankIds = radomPlayer(myRank.getRank());
                }
                dataLoadingStage = fillMainShowRankAsync(targetRankIds);
            }

            // 数据加载完成后发送响应
            dataLoadingStage
                    .thenRun(this::sendBattlePVEVPChallengeResponse_13000553);
        } else {
            // 刷新 - 生成新的随机对手
            if(refreshTime>=DateUtil.currentTimeSeconds()) {
                return;
            }
            refreshTime=DateUtil.currentTimeSeconds()+2;
            if (myRank != null) {
                List<Integer> ids = radomPlayer(myRank.getRank());
//                List<Integer> res = new ArrayList<>();
//                mainShowRank.clear();
//                for (int i = 0; i < ids.size(); i++) {
//                    int id = ids.get(i);
//                    if (cachePlayerRankMap.containsKey(id)) {
//                        mainShowRank.put(id, cachePlayerRankMap.get(id));
//                    } else {
//                        res.add(id);
//                    }
//                }
                fillMainShowRankAsync(ids)
                        .thenRun(this::sendBattlePVEVPChallengeResponse_13000553);
            }
        }
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(int ticketCount) {
        this.ticketCount = ticketCount;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public Map<Integer, PlayerRank> getMainShowRank() {
        return mainShowRank;
    }

    public void setMainShowRank(Map<Integer, PlayerRank> mainShowRank) {
        this.mainShowRank = mainShowRank;
    }
}
