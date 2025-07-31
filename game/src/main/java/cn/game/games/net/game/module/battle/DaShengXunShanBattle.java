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
import cn.game.protocol.manual.MountainNodeType;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

import java.time.LocalDate;
import java.util.*;

/**
 * 大圣巡山
 * 2025年7月31日 15:35:04
 *
 * @author QYK
 */
public class DaShengXunShanBattle extends XiYouBattleHandler {

    private MountainMapData mountainMapData = new MountainMapData();
    private transient Map<Integer, MountainMapNodeData> mapData = new HashMap<>();

    public DaShengXunShanBattle() {
    }

    /**
     * 生成地图数据 4层 11行 最多3个点
     */
    public void initMountainMapData() {
        for (int i = 1; i <= 4; i++) {
            List<MountainMapNodeData> floor = new ArrayList<>();
            for (int line = 1; line <= 11; line++) {
                int end = 1;
                if (line == 3 || line == 5 || line == 7 || line == 9) {
                    end = 3;
                } else if (line == 2 || line == 4 || line == 6 || line == 8 || line == 10) {
                    end = 2;
                } else if (line == 1 || line == 11) {
                    end = 1;
                }
                for (int k = 1; k <= end; k++) {
                    MountainMapNodeData mountainMapNodeData = new MountainMapNodeData();
                    mountainMapNodeData.setNodeId(getNodeUid(i, line, k));
                    mountainMapNodeData.clear();
                    floor.add(mountainMapNodeData);
                    mapData.put(mountainMapNodeData.getNodeId(), mountainMapNodeData);
                }
            }
            mountainMapData.getMapData().put(i, floor);
        }
    }

    int getLine(int nodeId) {
        return nodeId / 10 % 100;
    }

    int getNodeUid(int floor, int line, int index) {
        return floor * 1000 + line * 10 + index;
    }

    /**
     * 初始化节点数据
     */
    public void initMountainNodeData() {
        // 构造随机节点
        mountainMapData.getMapData().forEach((floor, line) -> {
            initMountainNodeData_Floor(line, floor);
        });
    }

    public void initMountainNodeData_Floor(List<MountainMapNodeData> line, int floor) {
        // 构造随机节点
        Map<Integer, Integer> radomPool = new HashMap<>();
        line.forEach(node -> {
            if (getLine(node.getNodeId()) > 2 && getLine(node.getNodeId()) <= 10) {
                radomPool.put(node.getNodeId(), 0);
            }
            ;
        });

        //第一个起点
        int startId = getNodeUid(floor, 1, 1);
        mapData.get(startId).setNodeType(MountainNodeType.Start.getType());
        //第2行 两个战斗
        mapData.get(getNodeUid(floor, 2, 0)).setNodeType(MountainNodeType.Battle.getType());
        mapData.get(getNodeUid(floor, 2, 1)).setNodeType(MountainNodeType.Battle.getType());
        //最后1行一定是BOSS
        int endId = getNodeUid(floor, 11, 1);
        mapData.get(endId).setNodeType(MountainNodeType.Boss.getType());
    }

    MountainMapNodeData getMountainrNodeData(int nodeUid) {
        if (mapData.containsKey(nodeUid)) {
            return mapData.get(nodeUid);
        }
        return null;
    }

    public void finishNode(int Uid, int floor) {

        MountainMapNodeData nodeData = getMountainrNodeData(Uid);
        if (nodeData == null) {
            return;
        }
        if (nodeData.getNodeStatus() == 2) {
            return;
        }
        int nodeType = nodeData.getNodeType();
        switch (MountainNodeType.get(nodeType)) {
            case MountainNodeType.Battle:
                break;
            case MountainNodeType.Event:
                break;
            case MountainNodeType.Shop:
                break;
            case MountainNodeType.Reward:
                break;
            case MountainNodeType.Boss:
                break;
            case MountainNodeType.Hp:
                handleHpNode(nodeData);
                break;
            default:
                break;
        }
    }
    void handleHpNode(MountainMapNodeData nodeData) {
        // 没有配置 就是回复30%  由有圣物就是50%
        int addNum = 5000;

    }

    @Override
    void newDay() {
        reset();

    }

    /**
     * 每天重置数据
     */
    public void reset() {

    }

    @Override
    public int checkCustom(int id, int subId, long... args) {
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
        return ResultObject.success();
    }

    @Override
    public int getType() {
        return DungeonTypeEnum.GemTower.getId();
    }


}
