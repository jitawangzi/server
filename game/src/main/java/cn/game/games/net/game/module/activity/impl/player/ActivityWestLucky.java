package cn.game.games.net.game.module.activity.impl.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.protocol.generated.config.ActivityWestLuckyPackConfig;
import cn.game.protocol.generated.config.ActivityWestLuckyTurntableConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.ActivityWestLuckyPackManager;
import cn.game.protocol.generated.manager.ActivityWestLuckyTurntableManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.ActivityMsg;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.util.Rnd;

/**
 * @ClassName ActivityWestLucky
 *
 * @description:  转盘活动
 * @author: ly
 * @create: 2024-12-08 14:16 @Version 1.0
 */
@ActivityType(type = ActivityTypeEnum.ActivityZhuanPan)
public class ActivityWestLucky extends PlayerActivityBase {

    /**累计抽取次数*/
    int totalNum;
    /**购买的物品数量 key id, val 数量*/
    Map<Integer,Integer> buyIdMap = new HashMap<>();
    /**抽中的格子信息*/
    Map<Integer,WestLuckyCellData> drawMap = new HashMap<>();

    @Override
    public EventTypeEnum[] getEventTypes() {
        return new EventTypeEnum[]{EventTypeEnum.NewDay};
    }

    @Override
    public void handleEvent(GameEvent event) {
        switch (event.getType()){
            case NewDay:
                //清空刷新购买次数
                List<ActivityWestLuckyPackConfig> list = ActivityWestLuckyPackManager.instance().list().stream().filter(c -> c.Refresh == 1 && c.NumberPeriods == id).toList();;
                list.forEach(c -> buyIdMap.remove(c.ID));
                break;
        }
    }

    @Override
    public Message buildActivityShowInfo() {
        ActivityMsg.ActivityWestLuckyInfoResponse_11000092.Builder res = ActivityMsg.ActivityWestLuckyInfoResponse_11000092.newBuilder();
        drawMap.forEach((k,v)->{
            res.putDramaMap(k,v.num);
        });
        res.setActivityId(id)
                .putAllBuyMap(buyIdMap);
        return res.build();
    }

    public int getDrawItemId(){
        return getConfigList().stream().findAny().get().ConsumeItemID;

    }

    @Override
    public List<RewardMsg.RewardInfo> receive(int id) {
        return null;
    }

    public int getDrawCellNum(int cellId){
        WestLuckyCellData cellData = drawMap.get(cellId);
        return cellData == null ? 0 : cellData.num;
    }

    List<ActivityWestLuckyTurntableConfig> getConfigList(){
        return ActivityWestLuckyTurntableManager.instance().list().stream().filter(c -> c.ActivityiD == id).toList();
    }

    public List<Integer> draw(boolean is3Type,List<Integer> findDrawList){
        List<Integer> result = new ArrayList<>();
        List<ActivityWestLuckyTurntableConfig> configList = getConfigList();
        //外圈待抽取集合
        List<ActivityWestLuckyTurntableConfig> outConfigList = configList.stream().filter(c -> c.CircleType == 1 || c.CircleType == 3 || c.CircleType == 4).collect(Collectors.toList()); //外圈：1
        if (is3Type){
            outConfigList = outConfigList.stream().filter(c ->{
                if (c.CircleType == 3 || c.CircleType == 4){
                    return false;
                }
                if (findDrawList.contains(c.ID)){
                    return  false;
                }
                return true;
            }).collect(Collectors.toList());
        }
        if (outConfigList == null || outConfigList.isEmpty()) {//外圈没有可抽取的物品 则不随机
            return result;
        }
        //内圈待抽取集合  内圈：2
        List<ActivityWestLuckyTurntableConfig> intterConfigList = configList.stream().filter(c -> {
            int drawNum = getDrawCellNum(c.ID);
            boolean flag = c.CircleType == 2 && c.LimitTimes > 0 && drawNum < c.LimitTimes;
            if (flag && c.NumberInNoObtain > 0){
                flag = totalNum >= c.NumberInNoObtain;
            }
            return  flag;
        }).collect(Collectors.toList());

        if (intterConfigList == null || intterConfigList.isEmpty()) {//内圈没有可抽取的物品 则不随机 进入内圈事件=4
            outConfigList.removeIf(c -> c.CircleType == 4);
        }
        ActivityWestLuckyTurntableConfig config =  Rnd.randomElement(outConfigList, c -> c.Weight);
        if (config != null){
            result.add(config.ID);
            if (config.CircleType == 3){//  连续3次外圈事件=3
                List<Integer> findList = new ArrayList<>(result);
                for(int i = 0; i < 3; i++) {
                    List<Integer> tempList = draw(true, findList);
                    findList.addAll(tempList);
                }
                result.addAll(findList.subList(1, findList.size()));
            } else if (config.CircleType == 4) {//进入内圈事件=4
                config =  Rnd.randomElement(intterConfigList, c -> c.Weight);
                result.add(config.ID);
                WestLuckyCellData cellData = drawMap.getOrDefault(config.ID, new WestLuckyCellData(config.ID));
                cellData.num++;
                drawMap.put(config.ID,cellData);
            }
        }
        return result;
    }

    public void addDrawNum(){
        this.totalNum++;
    }

    public Map<Integer, Integer> getBuyIdMap() {
        return buyIdMap;
    }

    @Override
    public void destroy() {
        //销毁身上的抽奖卷
        long num = player.getItemModule().getCount(getDrawItemId());
        if (num > 0){
            PlayerHelper.delResources(player,getDrawItemId(),num, OpType.ZhuanPanClearItem);
        }
    }

    //转盘格子抽奖信息
    public static class WestLuckyCellData{
        //格子id
        int id;
        //抽中的次数
        int num;

        public WestLuckyCellData() {
        }
        public WestLuckyCellData(int id) {
            this.id = id;
        }
    }
}
