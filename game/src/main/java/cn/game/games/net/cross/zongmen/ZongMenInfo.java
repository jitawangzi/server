package cn.game.games.net.cross.zongmen;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.games.cache.entity.Zongmen;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.GuildBasicConfig;
import cn.game.protocol.generated.config.GuildPermissionsConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.GuildBasicManager;
import cn.game.protocol.generated.manager.GuildPermissionsManager;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;

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
    public void init(int createServerId, long newZongMenId, String name, long createPlayerId, String createPlayerName,int power) {
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
        data.setCreateTime(DateUtil.getTimeByPattern(new Date(),DateUtil.pattern_en));
        data.setExp(0);
        data.setCreateServerId(createServerId);
        data.setServerNodeId(ServerContext.getInstance().getServerId());

        //初始化各个模块
        module = new ZongMenModuleData();
        module.init();
        module.afterInit(this);
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

    public void handleEvent(ZongMenConstants.ZongMenEvenType zongMenEvenType,Object... params) {
        module.handleEvent(zongMenEvenType, this,params);
    }

    public SimpleZongMen toSimpleZongMen(){
        SimpleZongMen simpleZongMen = new SimpleZongMen();
        simpleZongMen.setId(data.getId());
        simpleZongMen.setLv(data.getLv());
        simpleZongMen.setName(data.getName());
        simpleZongMen.setIcon(data.getIcon());
        simpleZongMen.setNum(module.menMemberMap.size());
        simpleZongMen.setAutoJoin(isAutoJoin());
        simpleZongMen.setTianDaoLevel(module.setting.getTianDaoLevel());
        return simpleZongMen;
    }

    public ZongMenMsg.ZongMenInfoProto toProto(long playerId) {
        ZongMenMsg.ZongMenInfoProto.Builder builder = ZongMenMsg.ZongMenInfoProto.newBuilder();
        builder.setSimpleInfo(toSimpleZongMen().toProto());
        builder.setExp(getExp());
        builder.addAllLogList(module.optLog.toProto());

        //封装 ZongMenMemberProto
        Map<Long, ZongMenMsg.ZongMenMemberProto.Builder> memberProtoMap = new HashMap<>();
        module.menMemberMap.forEach((pid, member) ->{
            memberProtoMap.put(pid,member.toProto());
        });

        List<Long> pidList = new ArrayList<>(module.menMemberMap.keySet());
        //该玩家有审批权限 同步 申请列表
        ZongMenMember member = getMember(playerId);
        GuildPermissionsConfig permissionsConfig = GuildPermissionsManager.instance().get(member.position);
        if (permissionsConfig.Approval){
            pidList.addAll(module.applyList);
        }
        //redis 同步加载 SimplePlayer
        List<SimplePlayer> simplePlayerList = PlayerManager.getInstance().batchGetSimplePlayerListFromRedisAsync(new ArrayList<>(module.menMemberMap.keySet())).result();
        if (simplePlayerList != null){
            simplePlayerList.forEach(simplePlayer -> {
                if (memberProtoMap.containsKey(simplePlayer.getId())){
                    memberProtoMap.get(simplePlayer.getId()).setSimplePlayer(simplePlayer.toSimplePlayerInfo());
                } else if(module.applyList.contains(simplePlayer.getId())){//同步申请列表
                    builder.addApplyList(simplePlayer.toSimplePlayerInfo());
                }
            });
        }
        memberProtoMap.values().forEach(memberProto ->{
            builder.addMemberList(memberProto.build());
        });

    // 封装 ZongMenSetting
    ZongMenMsg.ZongMenSettingProto.Builder settingProto = module.setting.toProto();
    settingProto.setNotice(data.getNotice());
    settingProto.setDeclaration(data.getDeclaration());
    builder.setSetting(settingProto.build());
    builder.setLiveness(module.liveness);

    //封装shop
     builder.setShop(module.shop.toProto(member));

    return builder.build();
    }


    public long callTotalPower() {
     return module.getTotalPower();
    }

    public boolean isHasMember(long playerId) {
        return module.menMemberMap.containsKey(playerId);
    }

    public boolean isFull() {
        GuildBasicConfig basicConfig = GuildBasicManager.instance().get(getLv());
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
        delZongMenNameIdRedisData();
        //删除宗门 simple 对象
        RedisLocalCache.getInstance().deleteAsync(CacheType.ZONG_MEN_SIMPLE_DATA.key(getId()));
        //删除数据库宗门
        DAO.delete(data);
        ZongMenManager.log.info("解散宗门成功 id:" + getId() + " name:" + getName() + "");
    }

     void delZongMenNameIdRedisData() {
        RedisLocalCache.getInstance().deleteAsync(CacheType.ZONG_MEN_NAME_ID.key(getName()));
    }

    public ZongMenModuleData getModule() {
        return module;
    }

    public void quitZongMen(ZongMenMember member, String playerName) {
        module.removeMember(member.getPlayerId());
        module.handleEvent(ZongMenConstants.ZongMenEvenType.QUIT_ZONG_MEN, this,playerName);
    }

    public int getPositionMemberNum(int position) {
        return (int) module.menMemberMap.values().stream().filter(member -> member.position == position).count();
    }

    public void addExp(int addExp){
        int totalExp = getExp() + addExp;
        GuildBasicConfig basicConfig = GuildBasicManager.instance().get(getLv());
        while (totalExp >= basicConfig.Exp){
            totalExp -= basicConfig.Exp;
            setLv(getLv() + 1);
            basicConfig = GuildBasicManager.instance().get(getLv());
            handleEvent(ZongMenConstants.ZongMenEvenType.ZONG_MEN_LEVEL_UP, this,getLv());
        }
        setExp(totalExp);
    }

    public void addZongMenAsset(long playerId, int id, int num) {
        if (id == Asset.ZongMenPoint.ID) {//宗门活跃度
            module.setLiveness(module.liveness + num);
        } else if (id == Asset.ZongMenExp.ID) {//宗门经验
            addExp(num);
        } else if (id == Asset.ZongMenContribute.ID) {//个人贡献
            ZongMenMember member = getMember(playerId);
            member.addcontribution(num);

        }
    }

  /**
   * 审批 同意 加入
   *
   * @param targetPidListList 被加入的列表
   * @param playerName 审批人
   */
  public void addMemberAuth(List<Long> targetPidListList, String playerName) {
    PlayerManager.getInstance()
        .batchGetSimplePlayerListFromRedisAsync(targetPidListList)
        .onSuccess(
            res -> {
              res.forEach(
                  simplePlayer -> {
                    if (isFull()) {
                      return;
                    }
                    //删除申请记录
                    module.removeApply(simplePlayer.getId());
                    //加入宗门
                    joinZongMen(
                        simplePlayer.getId(),
                        simplePlayer.getName(),
                        simplePlayer.combatEffectiveness,
                        ZongMenConstants.ZONG_MEN_POSITION_BANG_ZHONG);
                    // 通知被加入的玩家 加入宗门
                    ZongMenHelper.notifyMsgToPlayer(
                        simplePlayer.id,
                        ZongMenMsg.notifyJoinZongMen_40000044
                            .newBuilder()
                            .addTargetPids(simplePlayer.getId())
                            .setZongMen(toProto(simplePlayer.id))
                            .build(),
                        PbProtocol.notifyJoinZongMen_40000044);
                  });
              //更新宗门战斗力排行榜
              ZongMenManager.getInstance().saveZongMenTotalPowerRank(this);
            })
        .onFailure(
            err -> {
              err.printStackTrace();
            });
    }

    // 删除申请记录
    public void removeApplyAuth(List<Long> targetPidListList, String playerName) {
      targetPidListList.forEach(targetPid -> {
        module.removeApply(targetPid);
      });
    }

  // 踢人
  public void kickMember(List<Long> targetPidListList, String playerName) {
    PlayerManager.getInstance()
        .batchGetSimplePlayerListFromRedisAsync(targetPidListList)
        .onSuccess(
            res -> {
              res.forEach(
                  simplePlayer -> {
                    module.removeMember(simplePlayer.getId());
                    handleEvent(
                        ZongMenConstants.ZongMenEvenType.ZONG_MEN_KICK_MEMBER,
                        this,
                        playerName,
                        simplePlayer.getName());
                  });
              // 更新宗门战斗力排行榜
              ZongMenManager.getInstance().saveZongMenTotalPowerRank(this);
            })
        .onFailure(
            err -> {
              err.printStackTrace();
            });
  }

  /**
   * 检查宗主转让
   * - 自动转让宗主规则：
   *   - 宗主3天未上线则进行自动转让宗主（ZongmenSuzerainTransfer）；
   *   - 自动转让：职位最高且在3天内上线的玩家，同一职位转让至贡献度最高的玩家，贡献度相同则转让给战力最高的玩家；
   */
  public void checkZongZhuTransfer(long now) {
      List<ZongMenMember> memberList = new ArrayList<>(module.menMemberMap.values());

      memberList.sort((m1, m2)->{
        if (m1.position == m2.position){
            if (m1.getTotalContribution() == m2.getTotalContribution()){
                return m1.getPower() - m2.getPower();
            }
          return m1.getTotalContribution() - m2.getTotalContribution();
        }
        return m1.position < m2.position ? 1 : -1;
      });
      ZongMenMember zongZhu = memberList.get(0);
      PlayerManager.getInstance().getSimplePlayerFromRedisAsync(zongZhu.getPlayerId()).onSuccess(player -> {
        if (DateUtil.diffDays(now,player.getLastLoginTimer()) >= GlobalConst.ZongmenSuzerainTransfer){
            if (memberList.size() <= 1){//宗门没人
                return;
            }
            List<Long> targetPidListList = new ArrayList<>();
            PlayerManager.getInstance().batchGetSimplePlayerFromRedisAsync(targetPidListList).thenAccept(simplePlayerMap->{
                //被转让的人
                ZongMenMember targetZongZhu = null;
                for(int i = 1; i < memberList.size(); i++) {
                    long lastLoginTimer = simplePlayerMap.get(memberList.get(i).getPlayerId()).getLastLoginTimer();
                    if (DateUtil.diffDays(now,lastLoginTimer) < GlobalConst.ZongmenSuzerainTransfer){
                        targetZongZhu = memberList.get(i);
                        break;
                    }
                }
                if (targetZongZhu == null){
                    return;
                }
                //转让宗主
                zongZhuTransfer(zongZhu, targetZongZhu);

            }).exceptionally(err ->{err.printStackTrace();
                return null;
            });
        }
      }).onFailure(err ->{
          err.printStackTrace();
      });

  }

     void zongZhuTransfer(ZongMenMember zongZhu, ZongMenMember targetZongZhu) {
        zongZhu.setPosition(ZongMenConstants.ZONG_MEN_POSITION_BANG_ZHONG);
        handleEvent(ZongMenConstants.ZongMenEvenType.ZONG_MEN_POSITION_CHANGE, zongZhu.playerId,ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU, zongZhu.position);
        int targetOldPosition = targetZongZhu.position;
        targetZongZhu.setPosition(ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU);
        handleEvent(ZongMenConstants.ZongMenEvenType.ZONG_MEN_POSITION_CHANGE, targetZongZhu.playerId,targetOldPosition, targetZongZhu.position);
    }

    public void refreshShopByWeek() {
      module.refreshShopByWeek();
    }

    public boolean isEnoughAsset(int[] costItemArr, int count,ZongMenMember member)  {
        final  int idType = costItemArr[1];
        final int useCount = costItemArr[2];
        if (!ZongMenHelper.isZongMenAsset(idType)){
            return false;
        }
        if (idType == Asset.ZongMenContribute.ID){
            return member.getTotalContribution() >= useCount * count;
         }else {
            return false;
        }
    }

    public void costAsset(int[] costItemArr, int count,ZongMenMember member){
        final  int idType = costItemArr[1];
        final int useCount = costItemArr[2];
        if (idType == Asset.ZongMenContribute.ID){
            member.setTotalContribution(member.getTotalContribution() - useCount * count);
            //TODO  BI log
        }
    }



}
