package cn.game.games.net.cross.zongmen;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.games.cache.entity.Zongmen;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.BasicConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.BasicManager;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;

import java.util.*;

/**
 * @ClassName ZongMenInfo
 *
 * @description:  宗门对象
 * @author: ly
 * @create: 2025-02-05 14:45 @Version 1.0
 */
public class ZongMenInfo {
    /**数据库 t_zongmen 表的数据*/
    private Zongmen data;
    /**宗门模块的数据 */
    private ZongMenModuleData module;
    /**保存数据到数据库的时间戳*/
    private long saveDataTimer;
    public ZongMenInfo() {
    }
    public ZongMenInfo(Zongmen data) {
        this.data = data;
        module = JsonUtil.parseObjectWithType(this.data.getModules());
        module.registerAllModuleEventHandler();
    }
    public void init(long newZongMenId, String name, long createPlayerId, String createPlayerName,int power) {
        module = new ZongMenModuleData();
        saveDataTimer = System.currentTimeMillis();
        //初始化 Zongmen 对象
        data = new Zongmen();
        data.setName(name);
        data.setId(newZongMenId);
        data.setLv((byte)1);
        data.setIcon(GlobalConst.ZongmenIconRes);
        data.setNotice(GlobalConst.ZongmenGonggao);
        data.setDeclaration(GlobalConst.ZongmenXuanyan);
        data.setCreateTime(DateUtil.getTimeByPattern(new Date()));
        data.setExp(0);
        data.setServerNodeId(Integer.parseInt(ServerContext.getInstance().getServerId()));

        //初始化各个模块
        module = new ZongMenModuleData();
        module.init();
        module.registerAllModuleEventHandler();
        module.handleEvent(ZongMenConstants.ZongMenEvenType.ZONG_MEN_CREATE, this,createPlayerId,createPlayerName);
        joinZongMen(createPlayerId,createPlayerName,power,ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU);
    }

    void joinZongMen(long joinPlayerId, String playerName,int power,int position) {
        ZongMenMember member = new ZongMenMember(joinPlayerId,power,position);
        module.addMember(member);
        module.handleEvent(ZongMenConstants.ZongMenEvenType.JOIN_ZONG_MEN, this,member,playerName);
    }

    public Zongmen getData() {
        return data;
    }

    public void setData(Zongmen data) {
        this.data = data;
    }

    public long getSaveDataTimer() {
        return saveDataTimer;
    }

    public void setSaveDataTimer(long saveDataTimer) {
        this.saveDataTimer = saveDataTimer;
    }

    public void updateModuleData(){
        data.setModules(JsonUtil.toJsonStringWithType(module));
    }

    public long getId(){
        return data.getId();
    }
    public int getLv(){
        return data.getLv();
    }
    public void setLv(int lv){
        this.data.setLv((byte)lv);
    }

    public String getName(){
        return data.getName();
    }
    public void setName(String name){
        data.setName(name);
    }

    public void setIcon(int icon){
        data.setIcon(icon);
    }
    public int getIcon(){
        return data.getIcon();
    }
    public void setNotice(String notice){
        data.setNotice(notice);
    }
    public String getNotice(){
        return data.getNotice();
    }
    public int getExp(){
        return data.getExp();
    }
    public void setExp(int exp){
        data.setExp(exp);
    }
    public long getCreateTimer(){
        return DateUtil.parse(data.getCreateTime()).getTime();
    }

    public void handleEvent(ZongMenConstants.ZongMenEvenType zongMenEvenType) {
        module.handleEvent(ZongMenConstants.ZongMenEvenType.CROSS_DAY, this);
    }

    public SimpleZongMen toSimpleZongMen(){
        SimpleZongMen simpleZongMen = new SimpleZongMen();
        simpleZongMen.setId(data.getId());
        simpleZongMen.setLv(data.getLv());
        simpleZongMen.setName(data.getName());
        simpleZongMen.setIcon(data.getIcon());
        simpleZongMen.setNum(module.menMemberMap.size());
        simpleZongMen.setAutoJoin(isAutoJoin());
        return simpleZongMen;
    }

    public ZongMenMsg.ZongMenInfoProto toProto() {
        ZongMenMsg.ZongMenInfoProto.Builder builder = ZongMenMsg.ZongMenInfoProto.newBuilder();
        builder.setSimpleInfo(toSimpleZongMen().toProto());
        builder.setExp(getExp());
        builder.addAllLogList(module.optLog.toProto());

        //封装 ZongMenMemberProto
        Map<Long, ZongMenMsg.ZongMenMemberProto.Builder> memberProtoMap = new HashMap<>();
        module.menMemberMap.forEach((pid, member) ->{
            memberProtoMap.put(pid,member.toProto());
        });
        //redis 同步加载 SimplePlayer
        List<SimplePlayer> simplePlayerList = PlayerManager.getInstance().batchGetSimplePlayerListFromRedisAsync(new ArrayList<>(module.menMemberMap.keySet())).result();
        simplePlayerList.forEach(simplePlayer -> {
            memberProtoMap.get(simplePlayer.getId()).setSimplePlayer(simplePlayer.toSimplePlayerInfo());
        });
        memberProtoMap.values().forEach(memberProto ->{
            builder.addMemberList(memberProto.build());
        });

    // 封装 ZongMenSetting
    ZongMenMsg.ZongMenSettingProto.Builder settingProto = module.setting.toProto();
    settingProto.setNotice(data.getNotice());
    settingProto.setDeclaration(data.getDeclaration());
    builder.setSetting(settingProto.build());


        return builder.build();
    }


    public long callTotalPower() {
     return module.getTotalPower();
    }

    public boolean isHasMember(long playerId) {
        return module.menMemberMap.containsKey(playerId);
    }

    public boolean isFull() {
        BasicConfig basicConfig = BasicManager.instance().get(getLv());
        return module.menMemberMap.size() >= basicConfig.NumberMax;
    }

    public boolean hasApply(long playerId) {
        return module.applyList.contains(playerId);
    }


    public void applyJoin(long playerId) {
        module.applyList.add(playerId);
    }

    public boolean isAutoJoin() {
        return module.setting.isAutoJoin();
    }

    public ZongMenMember getMember(long playerId) {
        return module.menMemberMap.get(playerId);
    }

    // 解散宗门
    public void dissolveZongMen() {
        //删除所有玩家
        module.removeAllMember();
        // 删除宗门排行榜
        RankService.getInstance().removeRankAsync(RankType.Battle);
        //删除宗门名称 id 映射
        RedisLocalCache.getInstance().deleteAsync(CacheType.ZONG_MEN_NAME_ID.key(getName()));
        //删除宗门 simple 对象
        RedisLocalCache.getInstance().deleteAsync(CacheType.ZONG_MEN_SIMPLE_DATA.key(getId()));
        //删除数据库宗门
        DAO.delete(data);
        ZongMenManager.log.info("解散宗门成功 id:" + getId() + " name:" + getName() + "");
    }
}
