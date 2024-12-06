package cn.game.games.net.game.module.shop.xianshilibao;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.battle.ShiLuoZhenJingBattle;
import cn.game.protocol.generated.config.ActivityXianShiLiBaoConfig;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.ActivityXianShiLiBaoManager;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.protocol.protobuf.ShopMsg;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName XianShiLiBaoModule
 *
 * @description: 限时礼包模块
 * @author: ly
 * @create: 2024-11-27 11:37 @Version 1.0
 */
public class XianShiLiBaoModule extends BasePlayerModule {

    //解锁的限时礼包 key groupId, val 过期时间戳
    Map<Integer,Long> groupMap = new HashMap<>();
    //已经购买的限时礼包id
    List<Integer> buyIds = new ArrayList<>();
    boolean isInit;

    @Override
    public void buildPlayerAllInfo(PlayerMsg.PlayerAllInfo.Builder builder) {

    }

    @Override
    public void initFromDbAfter() {
        if (!isInit){
            this.isInit = true;
        }
    }

    public Map<Integer, Long> getGroupMap() {
        return groupMap;
    }

    public List<Integer> getBuyIds() {
        return buyIds;
    }


    @Override
    public EventTypeEnum[] getEventTypes() {
        return new EventTypeEnum[]{EventTypeEnum.LevelUp,EventTypeEnum.BattleEnd};
    }
    public List<ActivityXianShiLiBaoConfig> getXianShiLiBaoConfigList(int level, int battleId){
        Optional<ActivityXianShiLiBaoConfig> optionalActivityXianShiLiBaoConfig = ActivityXianShiLiBaoManager.instance().list().stream().filter( c -> c.BattleID == battleId && c.Lv<=level ).findAny();
        int groupId =optionalActivityXianShiLiBaoConfig.isPresent()? optionalActivityXianShiLiBaoConfig.get().Group: 0;
        if (groupId == 0){
            return new ArrayList<>();
        }
        return ActivityXianShiLiBaoManager.instance().list().stream().filter(c ->c.Group == groupId).collect(Collectors.toList());
    }
    @Override
    public void handleEvent(GameEvent event) {
        switch (event.getType()){
            case BattleEnd -> {
                int battleId = event.getIntParameter(0);
                BattleConfig battleConfig = BattleManager.instance().get(battleId);
                if (battleConfig == null){
                    log.error(String.format("not found BattleConfig battleConfig battleId:%d, pid:%d",battleId,player.getPlayerId()));
                    return;
                }
                if (battleConfig.BattleType != DungeonTypeEnum.ShiLuoZhenJing.getId()){
                    return;
                }
                int level = player.getLevel();
                checkAddNewLiBaoByBattleId(level, battleId);
            }
            case LevelUp -> {
                int level = event.getIntParameter(1);
               ShiLuoZhenJingBattle shiLuoZhenJingBattle = player.getChapterModule().getBattle(DungeonTypeEnum.ShiLuoZhenJing);
               List<ActivityXianShiLiBaoConfig> allLevelConfigList = getlevelConfigList(level);
                Map<Integer, ActivityXianShiLiBaoConfig> groupSet = new HashMap<>();
                if (allLevelConfigList != null){
                    allLevelConfigList.forEach(config ->{
                        if (!groupSet.containsKey(config.Group) &&
                                (
                                shiLuoZhenJingBattle == null ? config.BattleID == 0 :
                                (shiLuoZhenJingBattle.getHistoryMaxBattleId() >= config.BattleID && shiLuoZhenJingBattle.getHistoryMaxStage() == 10)||
                                        (shiLuoZhenJingBattle.getCompleteBattleId() >= config.BattleID && shiLuoZhenJingBattle.getBattleStage() == 10)
                        )
                        ){
                            groupSet.put(config.Group,config);
                        }
                    });
                    addNewLiBao(groupSet);
                }
            }
            default -> {
                log.error(String.format("XianShiLiBaoModule not found this type eventType:%s, pid:%d",event.getType(),player.getPlayerId()));
            }

        }
    }

    private List<ActivityXianShiLiBaoConfig> getlevelConfigList(int level) {
        return ActivityXianShiLiBaoManager.instance().list().stream().filter(c -> c.Lv <= level).collect(Collectors.toList());
    }

    private void checkAddNewLiBaoByBattleId(int level, int battleId) {
        List<ActivityXianShiLiBaoConfig> list = getXianShiLiBaoConfigList(level, battleId);
        Map<Integer, ActivityXianShiLiBaoConfig> groupIds = new HashMap<>();
        list.forEach(c ->{groupIds.put(c.Group,c);});
        addNewLiBao(groupIds);
    }

    private void addNewLiBao(Map<Integer, ActivityXianShiLiBaoConfig> groupIds) {
        groupIds.values().forEach(xianShiLiBaoConfig ->{
            if (groupMap.containsKey(xianShiLiBaoConfig.Group)){
                return;
            }
            long failTimer = (System.currentTimeMillis() + xianShiLiBaoConfig.Duration*1000L);
            groupMap.put(xianShiLiBaoConfig.Group,failTimer);
            ShopMsg.NotifyNewXianShiLiBao_15000054.Builder res = ShopMsg.NotifyNewXianShiLiBao_15000054.newBuilder();
            res.setInfo(ShopMsg.XianShiLiBaoInfo.newBuilder().setGroupId(xianShiLiBaoConfig.Group).setFailTimer((int) (failTimer/1000L)).build());
            player.getGameClient().sendProtocol(res);
        });
    }

    public List<ActivityXianShiLiBaoConfig> getGroupConfigList(int group) {
        return ActivityXianShiLiBaoManager.instance().list().stream()
                .filter(config -> config.Group == group)
                .collect(Collectors.toList());
    }

    public ShopMsg.XianShiLiBaoInfo buildXianShiLiBao(int group){
        if (!groupMap.containsKey(group)){
            return null;
        }
        List<Integer> ids = new ArrayList<>();
        getGroupConfigList(group).forEach(config -> {
            if (buyIds.contains(config.ID)){
                ids.add(config.ID);
            }
        });
        return ShopMsg.XianShiLiBaoInfo.newBuilder().setGroupId(group).setFailTimer((int) (groupMap.get(group)/1000L)).addAllBuyIds(ids).build();
    }

    public Iterable<RewardMsg.RewardInfo> addBuyId(ActivityXianShiLiBaoConfig config) {
        buyIds.add(config.ID);
        return PlayerHelper.addReward(player,config.Reward, OpType.BuyXianShiLiBao);
    }
}
