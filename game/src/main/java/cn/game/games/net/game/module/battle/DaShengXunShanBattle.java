package cn.game.games.net.game.module.battle;

import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.config.*;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.*;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.MountainNodeType;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;
import cn.game.util.Rnd;

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
    private final int SHOP_COIN = 100902;
    private final int END_NODEID3 = 3111;
    private final int END_NODEID4 = 4111;
    private final int BEGIN_NODEID = 1011;
    /**
     * 生成数据
     */
    public void initMountainData() {

        mountainMapData.setMapData(new HashMap<>());
        mountainMapData.setScore(0);
        mountainMapData.setScoreReward(new HashMap<>());
        mountainMapData.setHp(100);
        mountainMapData.setScoreMax(100);
        mountainMapData.setCurNodeId(0);
        mountainMapData.setBuffBag(new HashMap<>());
        // 获取下周一凌晨的时间戳（毫秒）
        long nextMondayMillis = DateUtil.addWeekBeginTimer(1);
        // 转换为秒级时间戳
        int endTime= (int)(nextMondayMillis / 1000);
        mountainMapData.setEndTime(endTime);
        mountainMapData.setSuccess(false);
        mountainMapData.setRefreshNum(3);
        int rankId = getSeasonRankId();
        mountainMapData.setRankId(rankId);
        initMountainMapData();
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
        mountainMapData.setCurNodeId(BEGIN_NODEID);
        // 构造随机节点
        mountainMapData.getMapData().forEach((floor, line) -> {
            initMountainNodeData_Floor(line, floor);
        });
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

    public void initMountainNodeData_Floor(List<MountainMapNodeData> line, int floor) {
        // 构造随机节点
        Map<Integer, Integer> radomPool = new HashMap<>();
        line.forEach(node -> {
            if (getLine(node.getNodeId()) >= 2 && getLine(node.getNodeId()) <= 10) {
                radomPool.put(node.getNodeId(), 0);
            }
        });
        List<Integer> keys = radomPool.keySet().stream().toList();
        Collections.shuffle(keys);
        //第一个起点
        int startId = getNodeUid(floor, 1, 1);
        mapData.get(startId).setNodeType(MountainNodeType.Start.getType());
        //最后1行一定是BOSS
        int endId = getNodeUid(floor, 11, 1);
        mapData.get(endId).setNodeType(MountainNodeType.Boss.getType());
        // 随机事件
        MountainBlockConfig mountainBlockConfigBoss = MountainBlockManager.instance().get(MountainNodeType.Boss.getType());
        if(mountainBlockConfigBoss ==  null) {
            return ;
        }
        //mapData.get(endId).setMonsterId( Rnd.randomInt(mountainBlockConfigBoss.blockRandom));

        // 随机事件
        MountainBlockConfig mountainBlockConfig = MountainBlockManager.instance().get(MountainNodeType.Event.getType());
        if(mountainBlockConfig==    null) {
            return;
        }
        int begin =0;
        List<Integer> eventPool=new ArrayList<>();
        MountainEventManager.instance().list().forEach(eventConfig -> {
            eventPool.add(eventConfig.ID);
        });
        Collections.shuffle(eventPool);
        genNodeType(keys,MountainNodeType.Event, eventPool);
        genNodeType(keys,MountainNodeType.Shop,eventPool );
        genNodeType(keys,MountainNodeType.Hard,eventPool );
        genNodeType(keys,MountainNodeType.Hp, eventPool);
        mapData.get(endId).setNodeType(MountainNodeType.Event.getType());
        for(int i=begin;i<keys.size();i++) {
            int nodeId = keys.get(i);
            mapData.get(nodeId).setNodeType(MountainNodeType.Easy.getType());
        }
    }

    void genNodeType(List<Integer> keys,MountainNodeType type, List<Integer> eventPool)
    {
        // 随机事件
        MountainBlockConfig mountainBlockConfig = MountainBlockManager.instance().get(type.getType());
        if(mountainBlockConfig==    null) {
            return ;
         }
        int index= Rnd.get(0,mountainBlockConfig.createNum.length-1);
        int num =mountainBlockConfig.createNum[index];

        for(int i=0;i<num;i++) {
            int nodeId = keys.get(i);
            mapData.get(nodeId).setNodeType(type.getType());
            if(type==MountainNodeType.Event
            || type==MountainNodeType.Hp)
            {
                int indexe= Rnd.get(0,eventPool.size()-1);
                int eventId=eventPool.get(indexe);
                mapData.get(nodeId).setEventId(eventId);
            }else if(type==MountainNodeType.Shop)
            {
                mapData.get(nodeId).getShopId().clear();
                for (int[] ints : GlobalConst.MountainShopRefreshRule) {
                    int shoptype= ints[0];
                    int shoptnum= ints[1];
                    List<MountainBuffConfig> buffList=MountainBuffManager.instance().getTypeList(shoptype);
                    if (buffList.size()<shoptnum)
                    {
                        shoptnum=buffList.size();
                    }
                    Collections.shuffle(buffList);
                    buffList.subList(0,shoptnum).forEach(buffConfig -> {
                        mapData.get(nodeId).getShopId().add(buffConfig.ID);
                    });
                }
            }

        }
    }
    public void radomBuffId(MountainMapNodeData nodeData, List<Integer> buffIdLis) {
        List<Integer> buffIdList=new ArrayList<>();
        if(nodeData.getNodeType()==MountainNodeType.Event.getType())
        {
            MountainEventManager.instance().list().forEach(eventConfig -> {
                buffIdList.add(eventConfig.ID);
            });

          // nodeData.setEventId(MountainEventManager.instance().list().);
        }
        int size=MountainEventManager.instance().list().size();
        int index= Rnd.get(0,size-1);
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

    public void finishNode(int Uid, int... para) {

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
            case MountainNodeType.Easy:
                break;
            case MountainNodeType.Hp:
            case MountainNodeType.Event:
                handleEventNode(nodeData, para);
                break;
            case MountainNodeType.Shop:
                success = handleShopNode(nodeData, para);
                break;
            case MountainNodeType.Boss:
                break;

            default:
                break;
        }
        if (success) {
            nodeData.setNodeStatus(1);
            mountainMapData.setCurNodeId(Uid);
            if(nodeType==MountainNodeType.Hard.getType()
            ||nodeType==MountainNodeType.Easy.getType()
            ||nodeType==MountainNodeType.Boss.getType())
            {
                MountainLayerConfig moduleLayer = MountainLayerManager.instance().get(getFloor(Uid));
                if(moduleLayer==    null) {
                    return;
                }
                addScore(moduleLayer.monsterPoint[nodeType-1]);
            }

        }
    }
    private  void  addBuff(int buffId) {
        MountainBuffConfig buffConfig = MountainBuffManager.instance().get(buffId);
        if(buffConfig==    null) {
            return;
        }
        if(mountainMapData.getBuffBag().containsKey(buffId))
        {
            int newcount=mountainMapData.getBuffBag().get(buffId)+1;
            if(newcount>=buffConfig.addMax) {
                newcount=buffConfig.addMax;
            }
            mountainMapData.getBuffBag().put(buffId,newcount);
        }else {
            mountainMapData.getBuffBag().put(buffId,1);
        }
    }

    void handleEventNode(MountainMapNodeData nodeData, int... param) {
        // 事件
        int eventId = nodeData.getEventId();
        MountainEventConfig eventConfig = MountainEventManager.instance().get(eventId);
        if(eventConfig==    null) {
            return;
        }
        int result = param[0];
//        if(!eventConfig.optionResult.contains(result)) {
//            return;
//        }
        MountainResultConfig resultConfig = MountainResultManager.instance().get(result);
        if(resultConfig==    null) {
            return;
        }
        switch (resultConfig.type) {
            case 1:
            {
                int buffId = param[1];
                addBuff(buffId);
            }
                break;
            case 2:
            {
                int buffId = param[1];
                addBuff(buffId);
                int buffId2 = param[2];
                addBuff(buffId2);
            }
                break;
            case 3: {
                int addNum = resultConfig.param[0];
                PlayerHelper.addResources(player, SHOP_COIN, addNum, OpType.MountainBattleEventAdd);
            }
                break;
            case 4: {
                int begin = resultConfig.param[0];
                int end = resultConfig.param[1];
                int addNum = Rnd.generateRandomNumbers(begin, end, 1).get(0);
                PlayerHelper.addResources(player, SHOP_COIN, addNum, OpType.MountainBattleEventAdd);
            }
                break;
            case 5:
                // todo  待定  要和客户端确认血量是万分比 还是真实血量
                break;
            case 6:
            {
                // todo  待定  要和客户端确认血量是万分比 还是真实血量
                int addpercent = resultConfig.param[0];
                mountainMapData.setHp(mountainMapData.getHp() + addpercent);
            }
                break;

        }
    }

    boolean handleShopNode(MountainMapNodeData nodeData, int...    param) {
        // 商店选择
        int buffId = param[0];
        MountainBuffConfig buffConfig = MountainBuffManager.instance().get(buffId);
        if(buffConfig==    null) {
            return false;
        }
        //扣钱 加BUFF
        int costID = SHOP_COIN;
        int cosNum = buffConfig.price;
        PlayerHelper.delResources(player, costID, cosNum, OpType.MountainBattleBuffShop);
        addBuff(buffId);
        return true;
    }

    @Override
    void newDay() {
       // reset();
    }

    /**
     * 重置数据
     */
    public void playerResetMap() {
        int num = mountainMapData.getRefreshNum();
        if(num<=0) {
            return;
        }
        mountainMapData.setRefreshNum(num-1);
        clearSeasonRank();
        mountainMapData.setScore(0);
        mountainMapData.setSuccess(false);
        long delNum=  player.getGoodsModule(SHOP_COIN).getCount(SHOP_COIN);
        PlayerHelper.delResources(player, SHOP_COIN, delNum, OpType.MountainRefresh);
        mountainMapData.getBuffBag().clear();


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
    public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request,  BattleMsg.BattleFieldEndResponse_13000004.Builder response) {
        BattleModule battleModule = player.getModule(BattleModule.class);
        if (request.getWin()) {
             int nodeId= request.getDaShengNodeId();
             finishNode(nodeId);
            return ResultObject.success();
        } else { // 失败了，最终结算

            return ResultObject.success();
        }
    }
    void addScore(int score) {
        mountainMapData.setScore(mountainMapData.getScore() + score);
        if(mountainMapData.getScoreMax() < mountainMapData.getScore())
        {
            mountainMapData.setScoreMax(mountainMapData.getScore());
            RankType rankType = RankType.get(1);
            RankService.getInstance().setScoreAsync(player.getServerId(), rankType, player.getPlayerId(),  mountainMapData.getScoreMax());
        }
    }
    /**
     * 设置赛季排行榜id
     */
    int getSeasonRankId()
    {
        int myLevel=player.getData().getLevel();
        int rankId=RankType. PatrollMountain1.ID;
        for (int[] ints : GlobalConst.MountainPlayerLevelParam) {
            if(ints[0]>myLevel) {
                break;
            }
            rankId++;
        }
        if(rankId>RankType. PatrollMountain4.ID) {
            rankId=RankType. PatrollMountain4.ID;
        }
        return rankId;
    }
    /**
     * 重置排行榜  换榜单直接清空  不换榜单则保留
     */
    void clearSeasonRank() {
        int lastrank = mountainMapData.getRankId();
        int newrank = getSeasonRankId();
        if(newrank>lastrank)
        {
           //换榜单 则清空老榜单
            RankService.getInstance().setScoreAsync(player.getServerId(), RankType.get(lastrank) , player.getPlayerId(),  0);
            mountainMapData.setRankId(newrank);
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
