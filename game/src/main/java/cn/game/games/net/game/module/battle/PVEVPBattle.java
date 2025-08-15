package cn.game.games.net.game.module.battle;

import cn.game.core.cache.CacheType;
import cn.game.games.core.ResultObject;
import cn.game.games.core.SimplePlayer;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.rank.PlayerRank;
import cn.game.games.net.game.module.rank.RankEntry;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.DaShengExtraPointsConfig;
import cn.game.protocol.generated.config.DaShengPointsConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.DaShengExtraPointsManager;
import cn.game.protocol.generated.manager.DaShengPointsManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg;
import cn.game.protocol.protobuf.BattleMsg;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;
import cn.game.util.RedisUtil;
import cn.game.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PVEVP  大圣擂台
 * 2025年8月4日 15:35:04
 *
 * @author QYK
 */
public class PVEVPBattle extends XiYouBattleHandler {

    private static final Logger log = LoggerFactory.getLogger(PVEVPBattle.class);
    /**
     * 界面上显示的那4个人
     *
     * @param rankType
     * @return
     */
    private transient Map<Integer, PlayerRank> mainShowRank = new ConcurrentHashMap<>();
   // private  Map<Integer, Long> mainShowRank2 = new ConcurrentHashMap<>();
    private transient  PlayerRank inBattleRank;
    private   long   inBattleRankPlayerId;
    private transient RankEntry myRank;
    private transient int refreshTime = 0;
    private int ticketCount = 0;
    private int buyCount = 0;
    private int endTime = 0;
    public transient List<PVEVPRecordData> recordDataList = new ArrayList<>();
    private transient  volatile boolean refreshFlag = false;

    public PVEVPBattle() {
    }


    @Override
    void newDay() {
        reset();
    }
   public  void initPvevpBattle() {
        newWeek();
    }
    @Override
    public void onLogin() {
       if(inBattleRankPlayerId>0)
       {
           String rediskeyTarget = CacheType.PVEVP_RECORD_ID.key( inBattleRankPlayerId);
           String rediskeyMy = CacheType.PVEVP_RECORD_ID.key(player.getData().getPlayerId());
           boolean isRobot = inBattleRankPlayerId<10000;
           // 生成战报
           if (isRobot) {
               createBattleRecord_Robot(PlayerHelper.getSimplePlayer(inBattleRankPlayerId), 0, false,rediskeyMy,1);
           } else {
               createBattleRecord_Target(0,true,rediskeyTarget);
               createBattleRecord_My(PlayerHelper.getSimplePlayer(inBattleRankPlayerId), 0, false,rediskeyMy);
           }
           inBattleRankPlayerId = 0L;
           mainShowRank.clear();
       }
    }
    @Override
    public void reLogin() {
        if(inBattleRankPlayerId>0)
        {
            String rediskeyTarget = CacheType.PVEVP_RECORD_ID.key( inBattleRankPlayerId);
            String rediskeyMy = CacheType.PVEVP_RECORD_ID.key(player.getData().getPlayerId());
            boolean isRobot = inBattleRankPlayerId<10000;
            // 生成战报
            if (isRobot) {
                createBattleRecord_Robot(PlayerHelper.getSimplePlayer(inBattleRankPlayerId), 0, false,rediskeyMy,1);
            } else {
                createBattleRecord_Target(0,true,rediskeyTarget);
                createBattleRecord_My(PlayerHelper.getSimplePlayer(inBattleRankPlayerId), 0, false,rediskeyMy);
            }
            inBattleRankPlayerId = 0L;
            mainShowRank.clear();
        }
    }
    void newWeek() {
        // 获取下周一凌晨的时间戳（毫秒）
        long nextMondayMillis = DateUtil.addWeekBeginTimer(1);
        // 转换为秒级时间戳
        endTime= (int)(nextMondayMillis / 1000);
        reset();
    }
    /**
     * 每天重置数据
     */
    public void reset() {
        ticketCount=GlobalConst.DaShengFreeTicket;
        buyCount=GlobalConst.DaShengBuyTicket;
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
        inBattleRank = mainShowRank.get(subId);
        inBattleRankPlayerId=inBattleRank.getRankEntry().getPlayerId();
        ticketCount--;
        return 0;
    }

    @Override
    public int battleStart(int id, int subId) {
        return 0;
    }

    @Override
    public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request,  BattleMsg.BattleFieldEndResponse_13000004.Builder response) {
        BattleModule battleModule = player.getModule(BattleModule.class);
        String rediskeyTarget = CacheType.PVEVP_RECORD_ID.key( inBattleRank.getRankEntry().getPlayerId());
        String rediskeyMy = CacheType.PVEVP_RECORD_ID.key(player.getData().getPlayerId());
        boolean isRobot = inBattleRank.getPlayer().getId()<10000;
        if (request.getWin()) {
            BattleConfig battleConfig = BattleManager.instance().get(battleModule.getAttackingId());
            List<RewardInfo> allRewards = new ArrayList<>();
            // 当前积分
            long targetScore = inBattleRank.getRankEntry().getScore();
            long myScore = myRank.getScore();
            // 计算积分
            long myAddScore=  calScore(myScore,Math.abs(targetScore-myScore),true,myScore>targetScore);
            long targetDelScore=  calScore(targetScore,Math.abs(targetScore-myScore),false,myScore<targetScore);
            myRank= new RankEntry(RankType.DaShengLeiTaiSeason.ID, player.getPlayerId(), myScore+myAddScore);

            RankService.getInstance().setScoreAsync(player.getServerId(), RankType.DaShengLeiTaiSeason, player.getPlayerId(), myRank.getScore());
            RankService.getInstance().setScoreAsync(player.getServerId(), RankType.DaShengLeiTaiDay, player.getPlayerId(), myRank.getScore());
            RankService.getInstance().updateScoreAsync(player.getServerId(), RankType.DaShengLeiTaiSeason, inBattleRank.getRankEntry().getPlayerId(), targetDelScore);
            RankService.getInstance().updateScoreAsync(player.getServerId(), RankType.DaShengLeiTaiDay, inBattleRank.getRankEntry().getPlayerId(), targetDelScore);

            // 生成战报
            if (isRobot) {
                createBattleRecord_Robot(PlayerHelper.getSimplePlayer(inBattleRank.getRankEntry().getPlayerId()), (int) myAddScore, true,rediskeyMy,1);
            } else {
                createBattleRecord_Target(targetDelScore,false,rediskeyTarget);
                createBattleRecord_My(PlayerHelper.getSimplePlayer(inBattleRank.getRankEntry().getPlayerId()), myAddScore, true,rediskeyMy);
            }
            resetCache();
            response.addParams((int)(myScore+myAddScore)   );
            response.addParams((int)myAddScore   );
            player.handleEvent(EventTypeEnum.DaShengPointsAdd,myScore,myAddScore);
           //  int rank = RankService.getInstance().getRank(player.getServerId(), RankType.DaShengLeiTaiDay, player.getPlayerId());
           // return ResultObject.success();
        } else { // 失败了，最终结算
            // 生成战报
            if (isRobot) {
                createBattleRecord_Robot(PlayerHelper.getSimplePlayer(inBattleRank.getRankEntry().getPlayerId()), 0, false,rediskeyMy,1);
            } else {
                createBattleRecord_Target(0,true,rediskeyTarget);
                createBattleRecord_My(PlayerHelper.getSimplePlayer(inBattleRank.getRankEntry().getPlayerId()), 0, false,rediskeyMy);
            }
            inBattleRank = null;
            inBattleRankPlayerId = 0L;
           // return ResultObject.success();
        }

        return ResultObject.success();
    }
    void createBattleRecord_Target( long change, boolean iswin,String rediskey) {
        var recordData = new PVEVPRecordData(iswin ? 1 : 0,
                player.getPlayerName(),
                player.getData().getLevel(),
                player.getAttrModule().getPower(),
                (int)change,
                player.getData().getHead(),
                player.getData().getHeadFrame(),
                DateUtil.currentTimeSeconds(),
                2,
                player.getPlayerId()
        );
        // 使用 List 保持插入顺序
        RedisUtil.getRedis().getList(rediskey).add(recordData);
        RedisUtil.getRedis().getList(rediskey).trim(0, GlobalConst.DaShengReport - 1);
    }
    void createBattleRecord_My(SimplePlayer simplePlayer, long change, boolean iswin,String rediskey) {
        var recordData = new PVEVPRecordData(iswin ? 1 : 0,
                simplePlayer.getName(),
                simplePlayer.getLevel(),
                simplePlayer.getCombatEffectiveness(),
                (int)change,
                simplePlayer.getHead(),
                simplePlayer.getHeadFrame(),
                DateUtil.currentTimeSeconds(),
                1,
                simplePlayer.getId()
        );

        // 使用 List 保持插入顺序
        RedisUtil.getRedis().getList(rediskey).add(recordData);
        RedisUtil.getRedis().getList(rediskey).trim(0, GlobalConst.DaShengReport
 - 1);

    }
    void createBattleRecord_Robot(SimplePlayer simplePlayer, long change, boolean iswin,String rediskey,int type){
        var recordData = new PVEVPRecordData(iswin ? 1 : 0,
                 simplePlayer.getName(),
                 simplePlayer.getLevel(),
                 simplePlayer.getCombatEffectiveness(),
                 (int)change,
                 simplePlayer.getHead(),
                 simplePlayer.getHeadFrame(),
                 DateUtil.currentTimeSeconds(),
                  type,
                simplePlayer.getId()
        );
        // 使用 List 保持插入顺序
        RedisUtil.getRedis().getList(rediskey).add(recordData);
        RedisUtil.getRedis().getList(rediskey).trim(0, GlobalConst.DaShengReport
 - 1);;
    }

    void getBattleRecordFromRedis() {
        recordDataList.clear();
        String rediskey = CacheType.PVEVP_RECORD_ID.key(player.getPlayerId());
        RedisUtil.getRedis().getList(rediskey).forEach(recordData -> {
            recordDataList.add((PVEVPRecordData) recordData);
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
        mainShowRank.clear();
        inBattleRank = null;
        inBattleRankPlayerId = 0L;
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
        int before = (int) (myscore * GlobalConst.DaShengRankingRatio[0]/ 10000.0f);
        if (before >= myscore - 3) {
            before = myscore - 3;
        }
        if(before<=1) {
            before=1;
        }
        rankIds.addAll(Rnd.generateRandomNumbers(before, myscore - 1, 3));
        int end = (int) (myscore * GlobalConst.DaShengRankingRatio[1]/ 10000.0f);
        if (end <= myscore + 1) {
            end = myscore + 1;
        }
        if (end >1000) {
            end = 1000;
        }
        rankIds.add(Rnd.get(myscore + 1, end));
        return rankIds;

    }

    public void fillMainShowRank(List<Integer> rankIds) {
        RankService rankService = RankService.getInstance();
        rankIds.forEach(rankId -> {
            var rankEntry = rankService.getRankEntry(player.getServerId(), RankType.DaDaoZhengFengSeason, rankId);
            setData(rankEntry);
        });
    }

    private void sendBattlePVEVPChallengeResponse_13000553() {

        BattleMsg.BattlePVEVPChallengeResponse_13000553.Builder resp = BattleMsg.BattlePVEVPChallengeResponse_13000553.newBuilder();
        mainShowRank.forEach((k, v) -> {
            BaseMsg.PlayerRankInfo.Builder rb = BaseMsg.PlayerRankInfo.newBuilder();
            rb.setRank(v.getRankEntry().getRank());
            rb.setPlayer(v.getPlayer().toSimplePlayerInfo());
            long score = v.getRankEntry().getScore();
            rb.setScore((score < 0 ? 0 : score) + "");
            resp.addChallengePlayers(rb);
        });
        refreshFlag = false;
        player.getGameClient().sendProtocol(resp.build());
    }


    public CompletionStage<Void> fillMainShowRankAsync(List<Integer> rankIds) {
        if (rankIds == null || rankIds.isEmpty()) {
            return CompletableFuture.completedStage(null);
        }

        RankService rankService = RankService.getInstance();
        List<CompletionStage<Void>> updateTasks = new ArrayList<>();
        // 并行获取所有排名数据
        rankIds.forEach(rankId -> {
            CompletionStage <RankEntry> rankEntry = rankService.getRankEntryAsync(player.getServerId(), RankType.DaShengLeiTaiSeason, rankId);
            updateTasks.add(rankEntry.thenAccept(this::setData));
        });
//        List<CompletionStage<Void>> updateTasks = rankIds.stream()
//                .map(rankId ->
//                {var rankEntry =rankService.getRankEntryAsync(player.getServerId(), RankType.DaShengLeiTaiSeason, rankId
//                })
//                .thenAccept(this::setData))
//                .toList();
//
//        // 等待所有任务完成
        return CompletableFuture.allOf(updateTasks.toArray(new CompletableFuture[0]));
    }

    private long calScore(long myscore, long diffscore, boolean iswin, boolean isHight) {
        int basescore = 0;
        List<DaShengPointsConfig> list = DaShengPointsManager.instance().list();
        for (int i = 0; i < list.size(); i++) {
            DaShengPointsConfig config = list.get(i);
            if (myscore >= config.PointStart && myscore <= config.PointEnd) {
                if (iswin) {
                    basescore = config.Victory;
                } else {
                    basescore = -config.Faild;
                }
                break;
            }
        }
        int extraScore = 0;
        List<DaShengExtraPointsConfig> list2 = DaShengExtraPointsManager.instance().list();
        for (int i = 0; i < list2.size(); i++) {
            DaShengExtraPointsConfig config = list2.get(i);
            if (diffscore >= config.PointGapStart && diffscore <= config.PointGapEnd) {
                if (isHight) {
                    extraScore = -config.HighPointMinus;
                } else {
                    extraScore = config.LowPointAdd;
                }
                break;
            }
        }
        basescore += extraScore;
        if (iswin) {
            //胜利 最小是0分
            basescore = basescore < 0 ? 0 : basescore;
        } else {
            //失败 最大是0分
            basescore = basescore > 0 ? 0 : basescore;
        }
        return basescore;
    }
    private void setData(RankEntry rankEntry) {
        var simplePlayer = PlayerHelper.getSimplePlayer(rankEntry.getPlayerId());
        var playerRank = new PlayerRank(rankEntry, simplePlayer);
        mainShowRank.put(rankEntry.getRank(), playerRank);
    }

    public void getRadomPlayer(int type) {

        if(refreshFlag) {
            return;
        }
        refreshFlag = true;
        if (type == 1) {
            // 请求
            myRank = RankService.getInstance().getRankEntry(player.getServerId(), RankType.DaShengLeiTaiSeason, player.getPlayerId());
            CompletionStage<Void> dataLoadingStage;
            if (myRank.getScore() == 0) {
                // 新玩家，获取排行榜末尾玩家
                myRank= new RankEntry(RankType.DaShengLeiTaiSeason.ID, player.getPlayerId(), 1000);
                dataLoadingStage = RankService.getInstance()
                        .getLastNAsync(player.getServerId(), RankType.DaShengLeiTaiSeason, 4)
                        .thenAccept(rankEntries -> {
                            mainShowRank.clear();
                            rankEntries.forEach(this::setData);
                        });
            } else {
                // 已有排名的玩家，获取随机对手
                List<Integer> targetRankIds;
                // 生成新的随机排名ID
                targetRankIds = radomPlayer(myRank.getRank());
                dataLoadingStage = fillMainShowRankAsync(targetRankIds);
            }
            // 数据加载完成后发送响应
            dataLoadingStage.thenRun(this::sendBattlePVEVPChallengeResponse_13000553).exceptionally(player::handleFailFunction);
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
                        .thenRun(this::sendBattlePVEVPChallengeResponse_13000553).exceptionally(player::handleFailFunction);
            }
        }
    }

    public void  buyCount()
    {
        if(buyCount<=0) {
            return;
        }
        int id = GlobalConst.DaShengTicketPrice[0];
        int num = GlobalConst.DaShengTicketPrice[1];
        if (  PlayerHelper.isEnough(player, id,num)){
            PlayerHelper.delResources(player, id,num, OpType.PVEVPBattleBuyTicket);
        }else {
            return ;
        }
        buyCount--;
        ticketCount++;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public int getEndTime() {
        return endTime;
    }

    public Map<Integer, PlayerRank> getMainShowRank() {
        return mainShowRank;
    }

    public int getBuyCount() {
        return buyCount;
    }

	public RankEntry getMyRank() {
		return myRank;
	}
    
}
