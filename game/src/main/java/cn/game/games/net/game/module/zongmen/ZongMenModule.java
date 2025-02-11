package cn.game.games.net.game.module.zongmen;

import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.cross.zongmen.ZongMenSetting;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.protocol.protobuf.ZongMenMsg;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @ClassName ZongMenModule
 *
 * @description: 玩家的宗门数据
 * @author: ly
 * @create: 2025-02-06 17:18 @Version 1.0
 */
public class ZongMenModule extends BasePlayerModule {
  long zongMenId;
  String zongMenName;

  /** 解散次数 只有宗主才会有该操作* */
  int disbandCount;

  /**
   * 下次加入宗门的时间 第二次及后续解散时，宗主需要1小时才可加入其它宗门（ZongmenSuzerainCD）
   * 第二次及后续退出时，需要1小时才可加入其它宗门（ZongmenMemberCD）*
   */
  long nextJoinTimer;

  public long getZongMenId() {
    return zongMenId;
  }

  public void setZongMenId(long zongMenId) {
    this.zongMenId = zongMenId;
  }

  public String getZongMenName() {
    return zongMenName;
  }

  public void setZongMenName(String zongMenName) {
    this.zongMenName = zongMenName;
  }

  public int getDisbandCount() {
    return disbandCount;
  }

  public void setDisbandCount(int disbandCount) {
    this.disbandCount = disbandCount;
  }

  public long getNextJoinTimer() {
    return nextJoinTimer;
  }

  public void setNextJoinTimer(long nextJoinTimer) {
    this.nextJoinTimer = nextJoinTimer;
  }

  @Override
  public void buildPlayerAllInfo(PlayerMsg.PlayerAllInfo.Builder builder) {}

  @Override
  public EventTypeEnum[] getEventTypes() {
    return new EventTypeEnum[] {EventTypeEnum.LoginSuccess,EventTypeEnum.NewDay};
  }

  @Override
  public void handleEvent(GameEvent event) {
    switch (event.getType()) {
      case LoginSuccess -> {
        checkZongMen();
      }
      case NewDay -> {//跨天刷新宗门任务
        refreshZongMenTask();
      }
    }
  }

  private void refreshZongMenTask() {
    if (zongMenId == 0) return;
    List<QuestConfig> zongMenTaskList =  QuestManager.instance().getTypeList(QuestTypeEnum.ZongMen.ID);
    QuestModule questModule =  player.getQuestModule();
    for (QuestConfig config : zongMenTaskList) {
      questModule.remove(config.ID);
      questModule.open(config.ID,true);
    }
  }

  private void checkZongMen() {
    // 未加入宗门 检测是否有宗门
    if (zongMenId == 0) {
      RedisLocalCache.getInstance().getAsync(CacheType.PLAYER_ID_ZONG_MEN_ID.key(player.getPlayerId()))
          .onSuccess(
              msg -> {
                if (msg == null) {
                  return;
                }
                long zongMenId = Long.parseLong(msg+"");
                setZongMenId(zongMenId);
                getZongMenInfo();
                List<QuestConfig> zongMenTaskList =  QuestManager.instance().getTypeList(QuestTypeEnum.ZongMen.ID);
                QuestModule questModule =  player.getQuestModule();
                for (QuestConfig config : zongMenTaskList) {
                  questModule.setState(questModule.get(config.ID),QuestHelper.ACCEPTED,true);
                }
                log.info(String.format("玩家[%d]登录成功，离线期间被审批加入宗门  宗门ID[%d]", player.getPlayerId(), zongMenId));
              })
          .onFailure(
              err -> {
                err.printStackTrace();
              });
    } else {
      getZongMenInfo();
    }
    getZongMenInfo();
  }

  private void getZongMenInfo() {
    ZongMenMsg.getZongMenInfoRequest_40000021 request =
        ZongMenMsg.getZongMenInfoRequest_40000021.newBuilder().build();
    ZongMenGameHandler.sendMsgToZongMenServer(player, request)
        .onSuccess(
            msg -> {
              // 玩家宗门 可能被解散了
              if (msg.errorCode == ErrorMsgEnum.zong_men_not_exist.ID) {
                clearZongMen();
              } else  if (msg.errorCode == ErrorMsgEnum.ok.ID) {
                ZongMenMsg.getZongMenInfoResponse_40000022 response = (ZongMenMsg.getZongMenInfoResponse_40000022) msg.response;
                if (StringUtils.isEmpty(zongMenName)){
                    setZongMenInfo(response.getInfo());
                }
              }
            })
        .onFailure(
            err -> {
              err.printStackTrace();
            });
  }

  public void clearZongMen() {
    setZongMenId(0);
    setZongMenName("");
    // 退出宗门 暂停宗门任务进度
    QuestModule questModule = player.getQuestModule();
    List<QuestConfig> zongMenTaskList =  QuestManager.instance().getTypeList(QuestTypeEnum.ZongMen.ID);
    for (QuestConfig config : zongMenTaskList) {
        questModule.setState(questModule.get(config.ID), QuestHelper.SHOW, true);
    }
  }

  public void setZongMenInfo(ZongMenMsg.ZongMenInfoProto zongMen) {
    setZongMenId(zongMen.getSimpleInfo().getId());
    setZongMenName(zongMen.getSimpleInfo().getName());
    refreshZongMenTask();
  }

  public void kickZongMen(ZongMenMsg.notifyQuitZongMen_40000024 quitZongMenMsg) {
    clearZongMen();
    player.getGameClient().sendProtocol(quitZongMenMsg);
  }

  public void joinZongMen(ZongMenMsg.notifyJoinZongMen_40000044 req) {
    setZongMenInfo(req.getZongMen());
    player.getGameClient().sendProtocol(req);
  }
}
