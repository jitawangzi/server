package cn.game.games.net.cross.zongmen;

import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.RedisUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;


/**
 * @ClassName ZongMenModuleData
 *
 * @description: 宗门各个模块管理
 * @author: ly
 * @create: 2025-02-05 14:53 @Version 1.0
 */
public class ZongMenModuleData {
    @JsonIgnore
   Map<ZongMenConstants.ZongMenEvenType, List<ZongMenConstants.ZongMenEventHandler>> eventTypeHandleMaps = new HashMap<>();

    /***      宗门操作日志 */
     ZongMenOptLog optLog;
    /***      宗门 成员列表 */
    Map<Long, ZongMenMember> menMemberMap = new HashMap<>();
    /**  宗门 设置 */
    ZongMenSetting setting;
    /**  宗门 活跃度 */
    int liveness;

  /*** 宗门 申请列表 */
  List<Long> applyList = new ArrayList<>();

    void registerAllModuleEventHandler(){
        registerEventHandler(optLog);
        registerEventHandler(setting);
        menMemberMap.values().forEach(member -> {
             registerEventHandler(member);
         });
    }

    void registerEventHandler(ZongMenConstants.ZongMenEventHandler eventHandler) {
        for (ZongMenConstants.ZongMenEvenType eventType : eventHandler.getRegisterEvent()) {
            List<ZongMenConstants.ZongMenEventHandler> handleList;
            if (eventTypeHandleMaps.containsKey(eventType)){
                handleList = eventTypeHandleMaps.get(eventType);
            } else {
                handleList = new ArrayList<>();
                eventTypeHandleMaps.put(eventType,handleList);
            }
            handleList.add(eventHandler);
        }
    }

    public void handleEvent(ZongMenConstants.ZongMenEvenType evenType, ZongMenInfo info , Object...params){
        long beginTimer = System.currentTimeMillis();

        eventTypeHandleMaps.get(evenType).forEach(eventHandler -> {
            eventHandler.handleEventType(evenType,info,params);
        });
        long endTimer = System.currentTimeMillis();
        if (endTimer - beginTimer > 50){
            ZongMenManager.log.error("handleEvent time is too long, type:%s, use:%d",evenType.getDesc(),endTimer - beginTimer);
        }
    }

    public void init() {
        optLog = new ZongMenOptLog();
        setting = new ZongMenSetting();

    }

    public void addMember(ZongMenMember member) {
        menMemberMap.put(member.playerId,member);
        registerEventHandler(member);
        RedisUtil.setAsync(CacheType.PLAYER_ID_ZONG_MEN_ID.key(member.playerId),member.playerId);
    }

    public void addApply(long playerId) {
        applyList.add(playerId);
    }


    public long getTotalPower() {
        long totalPower = 0;
        for (ZongMenMember member : menMemberMap.values()) {
            totalPower += member.getPower();
        }
        return totalPower;
    }

    public void removeAllMember() {
        menMemberMap.keySet().forEach(playerId -> {
            RedisUtil.deleteAsync(CacheType.PLAYER_ID_ZONG_MEN_ID.key(playerId));
            ZongMenHelper.notifyMsgToPlayer(playerId,ZongMenMsg.notifyQuitZongMen_40000024.newBuilder().build(), PbProtocol.notifyQuitZongMen_40000024);
        });
        menMemberMap.clear();
    }

    public void removeMember(long playerId) {
        menMemberMap.remove(playerId);
    }

    public void removeApply(long playerId) {
        applyList.remove(playerId);
    }

    public int getLiveness() {
        return liveness;
    }

    public void setLiveness(int liveness) {
        this.liveness = liveness;
    }
}
