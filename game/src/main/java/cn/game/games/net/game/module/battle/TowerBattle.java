package cn.game.games.net.game.module.battle;

import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.RankConfig;
import cn.game.protocol.generated.config.TowerConfig;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.RankManager;
import cn.game.protocol.generated.manager.TowerManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

import java.time.LocalDate;
import java.util.*;

/**
 * 宝石塔 龙渊秘藏
 * 2025年7月23日 15:35:04
 *
 * @author QYK
 */
public class TowerBattle extends XiYouBattleHandler {
    /**
     * 随机BUFF
     */
    private int radomBuff;    //
    /**
     * 当前层数  已完成的最高层
     */
    private Map<Integer, Integer> curFloor = new HashMap<>();     //
    /**
     * 剩余奖励次数   主塔用 与扫荡共享
     */
    private int rewardCount;
    /**
     * 剩余爬塔层高   全部共享用
     */
    private int floorCount;
    //

    public TowerBattle() {
    }

    public void InitTowerBattle() {
        if (curFloor.size() == 0) {
            curFloor.put(DungeonTypeEnum.GemTower.getId(), DungeonTypeEnum.GemTower.getId() * 10000 + 1);
            curFloor.put(DungeonTypeEnum.GemTowerIce.getId(), DungeonTypeEnum.GemTowerIce.getId() * 10000 + 1);
            curFloor.put(DungeonTypeEnum.GemTowerFire.getId(), DungeonTypeEnum.GemTowerFire.getId() * 10000 + 1);
            curFloor.put(DungeonTypeEnum.GemTowerPoison.getId(), DungeonTypeEnum.GemTowerPoison.getId() * 10000 + 1);
            curFloor.put(DungeonTypeEnum.GemTowerThunder.getId(), DungeonTypeEnum.GemTowerThunder.getId() * 10000 + 1);
            rewardCount = GlobalConst.MainTowerRewardMax;
            floorCount = GlobalConst.MainTowerFloorMax;
            LocalDate currentDate = LocalDate.now();
            this.radomBuff = currentDate.getDayOfMonth();
        }
    }

    @Override
    void newDay() {
        reset();

    }

    /**
     * 每天重置数据
     */
    public void reset() {
        rewardCount = GlobalConst.MainTowerRewardMax;
        floorCount = GlobalConst.MainTowerFloorMax;
        LocalDate currentDate = LocalDate.now();
        this.radomBuff = currentDate.getDayOfMonth();

        BattleMsg.BattleTowerDataPush_13100523.Builder resp = BattleMsg.BattleTowerDataPush_13100523.newBuilder();
        resp.setFloorCount(getFloorCount());
        resp.setRadomBuff(getRadomBuff());
        resp.setRewardCount(getRewardCount());
        curFloor.forEach((k, v) -> resp.putCurFloor(k, v));
        player.getGameClient().sendProtocol(resp.build());
    }

    @Override
    public int checkCustom(int id, int subId,long ... args) {
        BattleConfig battleConfig = BattleManager.instance().getNullable(id);
        if (battleConfig == null) {
            return ErrorMsgEnum.pre_condition_check_error.ID;
        }
        int battleType = battleConfig.BattleType;
        // 检查表格 ID存在 且层数正确
        if (!curFloor.containsKey(battleType) || curFloor.get(battleType) < id) {
            return ErrorMsgEnum.pre_condition_check_error.ID;
        }
        int curTowerBattleId = id;
        boolean isMain = battleType == DungeonTypeEnum.GemTower.getId();

        if (isMain) {
            if (curFloor.get(battleType) > id) {
                // 异常数据
                return ErrorMsgEnum.pre_condition_check_error.ID;
            } else if (curFloor.get(battleType) == id) {
                // 打下一关 检查次数
                if (floorCount <= 0) {
                    return ErrorMsgEnum.times_limit.ID;
                }
            } else if (curFloor.get(battleType) > id) {
                // 扫荡
            }
        } else {
            TowerConfig towerConfig =  TowerManager.instance().get(battleType);
            if(towerConfig==null)
            {
                return ErrorMsgEnum.pre_condition_check_error.ID;
            }
            LocalDate currentDate = LocalDate.now();
            boolean result = Arrays.asList(Arrays.stream(towerConfig.time)
                            .boxed()
                            .toArray(Integer[]::new))
                    .contains(currentDate.getDayOfWeek().getValue());
            if(!result) {
                return ErrorMsgEnum.pre_condition_check_error.ID;
            }
            //分塔只有打 没有扫荡  且不扣奖励次数 只有升层次数
            if (curTowerBattleId != id) {
                return ErrorMsgEnum.pre_condition_check_error.ID;
            }
            // 分塔打下一关 检查次数
            if (floorCount <= 0) {
                return ErrorMsgEnum.times_limit.ID;
            }
        }
        return 0;
    }

    @Override
    public int battleStart(int id, int subId) {
        return 0;
    }

    @Override
    public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request, BattleMsg.BattleFieldEndResponse_13000004.Builder response) {
        BattleModule battleModule = player.getModule(BattleModule.class);
        if (request.getWin()) {
            BattleConfig battleConfig = BattleManager.instance().get(battleModule.getAttackingId());
            List<RewardInfo> allRewards = new ArrayList<>();
            boolean newRecord = battleModule.getAttackingId() == curFloor.get(battleConfig.BattleType);
            if (newRecord) {
                OpType opType = OpType.GemTowerFirstFinish;
                if (battleConfig.BattleType != DungeonTypeEnum.GemTower.getId()) {
                    List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.FirstPassReward, opType);
                    allRewards.addAll(reward);
                    addRank(battleConfig.BattleType, battleModule.getAttackingId());
                }else if (battleConfig.BattleType == DungeonTypeEnum.GemTower.getId())
                {
                    if(rewardCount >0  ) {
                        List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.FirstPassReward, opType);
                        allRewards.addAll(reward);
                        addRank(battleConfig.BattleType, battleModule.getAttackingId());
                        rewardCount--;
                    }
                }
            } else {
                OpType opType = OpType.GemTowerSweep;
                List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.SweepReward, opType);
                allRewards.addAll(reward);
            }
            if (BattleManager.instance().getNullable(battleModule.getAttackingId() + 1) != null) {
                curFloor.put(battleConfig.BattleType, battleModule.getAttackingId() + 1);
            } else {
                curFloor.put(battleConfig.BattleType, -1);
            }
            floorCount--;
            return ResultObject.success();
        } else { // 失败了，最终结算

            return ResultObject.success();
        }
    }

    public void addRank(int battelType, int battleId) {
        // 23-27 ->14-18
        RankType rankType = RankType.get(battelType - 9);
        RankConfig rankConfig = RankManager.instance().get(rankType.ID);
        if (BattleHelper.isComplete(battleId, rankConfig.Request)) {
            RankService.getInstance().setScoreAsync(player.getServerId(), rankType, player.getPlayerId(), battleId);
        }
    }

    @Override
    public ResultObject<List<RewardInfo>> quickEnd(int id, int subId, boolean isWin) {
        if (isWin) {
            BattleConfig battleConfig = BattleManager.instance().get(id);
            List<RewardInfo> allRewards = new ArrayList<>();
            int battleType = battleConfig.BattleType;
            if (battleType < DungeonTypeEnum.GemTower.getId() || battleType > DungeonTypeEnum.GemTowerPoison.getId()) {
                return ResultObject.fail(ErrorMsgEnum.pre_condition_check_error.ID);
            }
            // 已通关检查
            if (curFloor.get(battleConfig.BattleType) != -1 && id >= curFloor.get(battleConfig.BattleType)) {
                return ResultObject.fail(ErrorMsgEnum.pre_condition_check_error.ID);
            }
            int count = subId;
            if (count > rewardCount) {
                count = rewardCount;
            }
            if(battleConfig.BattleType==DungeonTypeEnum.GemTower.getId())
            {
                rewardCount-=count;
            }
            OpType opType = OpType.GemTowerSweep;
            for (int i = 0; i < count; i++) {
                List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.SweepReward, opType);
                allRewards.addAll(reward);
            }
            return ResultObject.success(allRewards);
        }
        return ResultObject.success();
    }

    @Override
    public int getType() {
        return DungeonTypeEnum.GemTower.getId();
    }

    public boolean towerBattleComplete(int id) {
        BattleConfig battleConfig = BattleManager.instance().getNullable(id);
        if(battleConfig==null) {
          return false;
        }
        if (battleConfig.BattleType < DungeonTypeEnum.GemTower.getId() || battleConfig.BattleType > DungeonTypeEnum.GemTowerPoison.getId()) {
            return false;
        }
        if(curFloor.get(battleConfig.BattleType)==-1) {
            return true;
        }
        return id >curFloor.get(battleConfig.BattleType);
    }

    public int getRadomBuff() {
        return radomBuff;
    }

    public void setRadomBuff(int radomBuff) {
        this.radomBuff = radomBuff;
    }

    public int getRewardCount() {
        return rewardCount;
    }

    public void setRewardCount(int rewardCount) {
        this.rewardCount = rewardCount;
    }

    public int getFloorCount() {
        return floorCount;
    }

    public void setFloorCount(int floorCount) {
        this.floorCount = floorCount;
    }

    public Map<Integer, Integer> getCurFloor() {
        return curFloor;
    }

    public void setCurFloor(Map<Integer, Integer> curFloor) {
        this.curFloor = curFloor;
    }
}
