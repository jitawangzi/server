package cn.game.games.net.game.module.battle;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.util.IdUtil;
import cn.game.games.cache.entity.EquiptowerHelp;
import cn.game.games.cache.entity.Friend;
import cn.game.games.core.ResultObject;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.GameServer;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.activity.GlobalActivityManager;
import cn.game.games.net.game.module.develop.fairyfriend.FairyFriendModule;
import cn.game.games.net.game.module.friend.FriendModule;
import cn.game.games.net.game.module.player.VarConstant;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.RankConfig;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.RankManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;
import cn.game.util.RedisUtil;
import io.vertx.core.Future;
import org.redisson.api.RFuture;

import javax.xml.crypto.Data;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 装备塔 踏碎凌霄
 * 2025年7月28日 15:35:04
 *
 * @author QYK
 */
public class EquipTowerBattle extends XiYouBattleHandler {
    /**
     * 当前层数  助战战斗ID  奖励状态
     */
    private Map<Integer, Map<Long, Integer>> helpRewardMap = new HashMap<>();

    /**
     * 当前层数  助战列表  可以帮自己大的人
     */
    private transient Map<Integer, List<Long>> simplePlayerMap = new HashMap<>();     //
    /**
     * 战报
     */
    private Map<Integer, String> battleRecord = new HashMap<>();
    /**
     * 剩余门票奖励次数
     */
    private int ticketCount;
    /**
     * 当前层数
     */
    private int curFloor;
    /**
     * 下次门票刷新时间戳
     */
    private long nextGetTicketTime;
    private transient long cacheHelpPlayerId;
    //
    transient final int MAXFLOOR = 10;

    public EquipTowerBattle() {
    }

    public void initEquipBattle() {
        curFloor = 1;
        helpRewardMap.clear();
        ticketCount = GlobalConst.TicketRefreshMax;
        nextGetTicketTime = DateUtil.currentTimeSeconds();
    }

    @Override
    public void onLogin() {
        var helpdata = player.getBattleModule().getHelpData();
        helpRewardMap.forEach((floorId, helpMap) -> {
            Iterator<Map.Entry<Long, Integer>> iterator = helpMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, Integer> entry = iterator.next();
                // 检查 helpdata 是否包含该助战场次
                if (!helpdata.containsKey(entry.getKey())) {
                    iterator.remove(); // 从当前楼层的助战映射中移除
                }
            }
        });
        player.getBattleModule().getHelpData().forEach((k, v) -> {
            int floor = v.getFloor();
            long helpId = v.getId();
            if (!helpRewardMap.containsKey(floor)) {
                helpRewardMap.put(floor, new HashMap<>());
            }
            var helpMap = helpRewardMap.get(floor);
            if (!helpMap.containsKey(helpId)) {
                helpMap.put(helpId, 0);
            }
        });
    }

    @Override
    void newDay() {
        reset();
    }

    /**
     * 每天重置数据
     */
    public void reset() {
        helpRewardMap.clear();
        ticketCount = GlobalConst.TicketRefreshMax;
    }


    public void getHelpPlayerInfo(int floor) {
        if (floor > curFloor) {
            return;
        }
        String rediskey = CacheType.EQUIP_TOWER_FLOOR_ID.key(floor);
        Set<Object> future = RedisUtil.getRedis().getSet(rediskey).random(10);
        List<Long> players = new ArrayList<>();
        for (Object element : future) {
            // 处理每个随机元素
            players.add((Long) element);
        }
        simplePlayerMap.put(floor, players);
    }

    public void addHelpRewards(long help, int floor) {
        EquiptowerHelp equiptowerHelp = new EquiptowerHelp();
        equiptowerHelp.setId(IdUtil.getId());
        equiptowerHelp.setPlayerId(help);// 奖励发给谁
        equiptowerHelp.setFloor(floor);
        equiptowerHelp.setHelpPlayerId(player.getPlayerId());
        int deltime = (int) (DateUtil.nextDayStartTime(DateUtil.currentTimeMillis(), 1) / 1000);
        equiptowerHelp.setExpiredTime(deltime);
        equiptowerHelp.insert();
        // 通知在线玩家
        if(PlayerManager.getInstance().isOnline(help)) {
            GameServerInterface gameServerInterface = GameServer.getInstance().getGameServerInterface(DistributedObjectType.PLAYER, help);
            gameServerInterface.addEquipTowerHelp(help, equiptowerHelp);
        }
    }
    // 在线接收
    public void addHelpReward_onLine(EquiptowerHelp equiptowerHelp ) {
        int floorId = equiptowerHelp.getFloor();
        long helpId = equiptowerHelp.getId();
        if (!helpRewardMap.containsKey(floorId)) {
            Map<Long, Integer> v = new HashMap<>();
            helpRewardMap.put(floorId, v);
        }
        helpRewardMap.get(floorId).put(helpId, 0);
    }

    public List<RewardInfo> getTicket() {
        if (DateUtil.currentTimeSeconds() < nextGetTicketTime) {
            return null;
        }
        if (ticketCount <= 0) {
            return null;
        }
        ticketCount--;
        nextGetTicketTime = DateUtil.currentTimeSeconds() + GlobalConst.TicketRefreshTime * 3600;
        return PlayerHelper.addResources(player, GlobalConst.TicketItemId, 1, OpType.EquipTowerTicket);
    }

    public List<RewardInfo> getHelpReward(int floor, long helpId, int type) {
        List<RewardInfo> allReward = new ArrayList<>();
        if (type == 1) {
            // 一键领取
            List<RewardInfo> finalAllRewards = new ArrayList<>();
            helpRewardMap.forEach((floorId, helpMap) -> {
                helpMap.forEach((k, isGet) -> {
                    if (isGet == 0) {
                        int battleId = 1;
                        BattleConfig battleConfig = BattleManager.instance().get(battleId);
                        var tmp = PlayerHelper.addReward(player, battleConfig.SweepReward, OpType.EquipTowerHelp);
                        finalAllRewards.addAll(tmp);
                    }
                });
            });
            allReward.addAll(finalAllRewards);
        } else {
            if (!helpRewardMap.containsKey(floor)) {
                return allReward;
            }
            if (!helpRewardMap.get(floor).containsKey(helpId)) {
                return allReward;
            }
            //已领取
            if (helpRewardMap.get(floor).get(helpId) != 0) {
                return allReward;
            }
            helpRewardMap.get(floor).put(helpId, 1);
            int battleId = 1;
            BattleConfig battleConfig = BattleManager.instance().get(battleId);
            allReward = PlayerHelper.addReward(player, battleConfig.SweepReward, OpType.EquipTowerHelp);

        }
        return allReward;
    }

    @Override
    public int checkCustom(int id, int subId, long... args) {
        BattleConfig battleConfig = BattleManager.instance().getNullable(id);
        if (battleConfig == null) {
            return ErrorMsgEnum.pre_condition_check_error.ID;
        }
        long num = player.getItemModule().getCount(GlobalConst.TicketItemId);
        if (num > 0){
            PlayerHelper.delResources(player,GlobalConst.TicketItemId,num, OpType.EquipTowerStart);
        }else {
            return ErrorMsgEnum.times_limit.ID;
        }
        int floor = id % 10;
        if (floor > curFloor) {
            return ErrorMsgEnum.pre_condition_check_error.ID;
        }
        if (args.length > 0) {
            long helpPlayerId = args[0];
            if (!simplePlayerMap.containsKey(floor)) {
                return ErrorMsgEnum.pre_condition_check_error.ID;
            }
            if (!simplePlayerMap.get(floor).contains(helpPlayerId)) {
                FriendModule friendModule = player.getModule(FriendModule.class);
                if (!friendModule.isFriend(helpPlayerId)) {
                    return ErrorMsgEnum.pre_condition_check_error.ID;
                }
            }
            cacheHelpPlayerId = helpPlayerId;
        }
        return 0;
    }

    @Override
    public int battleStart(int id, int subId) {
        return 0;
    }

    @Override
    public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request) {
        BattleModule battleModule = player.getModule(BattleModule.class);
        BattleConfig battleConfig = BattleManager.instance().get(battleModule.getAttackingId());
        List<RewardInfo> allRewards = new ArrayList<>();
        OpType opType = OpType.EquipTowerFinish;
        if (request.getWin()) {
            List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.WinRandom, opType);
            allRewards.addAll(reward);
           int battlefloor= battleModule.getAttackingId()%10;
            boolean newRecord = battlefloor == curFloor;
            if (newRecord) {
                curFloor++;
                if (MAXFLOOR < curFloor) {
                    curFloor = MAXFLOOR;
                }
                String rediskey = CacheType.EQUIP_TOWER_FLOOR_ID.key(battlefloor);
                RedisUtil.getRedis().getSet(rediskey).add(player.getPlayerId());
            }
            // todo 上传记录
            if(newRecord||battleModule.getAttackingSubId()==1) {
                battleRecord.put(battlefloor,request.getEquipBattleRecord());
            }
            if(cacheHelpPlayerId>0) {
                addHelpRewards(cacheHelpPlayerId, battleModule.getAttackingId());
                cacheHelpPlayerId = 0;
            }
            return ResultObject.success();
        } else { // 失败了，最终结算
            List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.FailRandom, opType);
            allRewards.addAll(reward);
            boolean newRecord = battleModule.getAttackingId() == curFloor;
            if (newRecord) {
                curFloor++;
                if (MAXFLOOR < curFloor) {
                    curFloor = MAXFLOOR;
                }
            }
            return ResultObject.success();
        }
    }

    public void addRank(int addPoint) {
        RankService.getInstance().updateScoreAsync(player.getServerId(), RankType.EquipTower, player.getPlayerId(), addPoint);
    }

    @Override
    public ResultObject<List<RewardInfo>> quickEnd(int id, int subId, boolean isWin) {

        return ResultObject.success();
    }

    @Override
    public int getType() {
        return DungeonTypeEnum.EquipTower.getId();
    }


    public Map<Integer, List<Long>> getSimplePlayerMap() {
        return simplePlayerMap;
    }

    public int getCurFloor() {
        return curFloor;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public long getNextGetTicketTime() {
        return nextGetTicketTime;
    }

    public Map<Integer, Map<Long, Integer>> getHelpRewardMap() {
        return helpRewardMap;
    }

    public Map<Integer, String> getBattleRecord() {
        return battleRecord;
    }

    public void setBattleRecord(Map<Integer, String> battleRecord) {
        this.battleRecord = battleRecord;
    }
}
