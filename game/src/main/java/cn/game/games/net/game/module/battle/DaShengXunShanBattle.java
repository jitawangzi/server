package cn.game.games.net.game.module.battle;

import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.RankConfig;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.RankManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.MountainNodeType;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

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

    int getFloor(int nodeId) {
        return nodeId / 1000;
    }

    int getIndex(int nodeId) {
        return nodeId % 10;
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

    private boolean checkCanFinihsh(int nodeId) {
        MountainMapNodeData nodeData = getMountainrNodeData(nodeId);
        if (nodeData == null) {
            return false;
        }
        if (nodeData.getNodeStatus() != 0) {
            return false;
        }
        int floor = getFloor(mountainMapData.getCurNodeId());
        int line = getLine(mountainMapData.getCurNodeId());
        int index = getIndex(mountainMapData.getCurNodeId());
        int n1 = getNodeUid(floor, line + 1, 1);
        int n2 = getNodeUid(floor, line + 1, 2);
        int n3 = getNodeUid(floor, line + 1, 3);
        if (line == 3 || line == 5 || line == 7 || line == 9) {
            // 一层3个点 3解2
            if (index == 1) {
                //解1个
                return nodeId == n1;
            } else if (index == 2) {
                //解2个
                return nodeId == n1 || nodeId == n2;
            } else if (index == 3) {
                //解1个
                return nodeId == n2;
            }
        } else if (line == 2 || line == 4 || line == 6 || line == 8) {
            // 一层2个点 2解3
            if (index == 1) {
                //解2个
                return nodeId == n1 || n2 == nodeId;
            } else if (index == 2) {
                //解2个
                return nodeId == n3 || n2 == nodeId;
            }
        } else if (line == 1) {
            // 全解
            return nodeId == n1 || n2 == nodeId;
        } else if (line == 10) {
            // 全解
            return nodeId == getNodeUid(floor, line + 1, 1);
        }
        return true;
    }

    public void finishNode(int Uid, int para) {

        MountainMapNodeData nodeData = getMountainrNodeData(Uid);
        if (nodeData == null) {
            return;
        }
        if (!checkCanFinihsh(Uid)) {
            return;
        }
        int nodeType = nodeData.getNodeType();
        boolean success = true;
        switch (MountainNodeType.get(nodeType)) {
            case MountainNodeType.Battle:
                break;
            case MountainNodeType.Event:
                handleEventNode(nodeData, para);
                break;
            case MountainNodeType.Shop:
                success = handleShopNode(nodeData, para);
                break;
            case MountainNodeType.Boss:
                break;
            case MountainNodeType.Hp:
                handleHpNode(nodeData);
                break;
            default:
                break;
        }
        if (success) {
            nodeData.setNodeStatus(1);
            mountainMapData.setCurNodeId(Uid);
        }
    }

    void handleHpNode(MountainMapNodeData nodeData) {
        // 没有配置 就是回复30%  由有圣物就是50%
        int addNum = 5000;
        var tmp = mountainMapData.getHp() + addNum;
        mountainMapData.setHp(tmp);
    }

    void handleEventNode(MountainMapNodeData nodeData, int param) {
        // 事件
    }

    boolean handleShopNode(MountainMapNodeData nodeData, int param) {
        // 商店选择
        int buffId = 1;
        //扣钱 加BUFF
        int costID = GlobalConst.TicketItemId;
        int cosNum = 10;
        long num = player.getItemModule().getCount(costID);
        if (num >= cosNum) {
            PlayerHelper.delResources(player, costID, cosNum, OpType.MountainBattleBuffShop);
        } else {
            return false;
        }
        mountainMapData.getBuffBag().add(buffId);
        return true;
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
        return DungeonTypeEnum.MountainBattle.getId();
    }


    public MountainMapData getMountainMapData() {
        return mountainMapData;
    }

    public void setMountainMapData(MountainMapData mountainMapData) {
        this.mountainMapData = mountainMapData;
    }
}
