package cn.game.games.net.cross.zongmen;

import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.protobuf.ZongMenMsg;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ZongMenOptLog
 *
 * @description: 宗门操作日志
 * @author: ly
 * @create: 2025-02-05 16:50 @Version 1.0
 */
public class ZongMenOptLog implements ZongMenConstants.ZongMenEventHandler {

  List<OptLogData> optLogDataList = new ArrayList<>();

  @Override
  public ZongMenConstants.ZongMenEvenType[] getRegisterEvent() {
    return new ZongMenConstants.ZongMenEvenType[] {
      ZongMenConstants.ZongMenEvenType.JOIN_ZONG_MEN,
      ZongMenConstants.ZongMenEvenType.ZONG_MEN_POSITION_CHANGE,
    };
  }

  @Override
  public void handleEventType(
      ZongMenConstants.ZongMenEvenType type, ZongMenInfo info, Object... params) {
      switch (type){
          case JOIN_ZONG_MEN -> {
              String joinPlayerName = params[1]+"";
              addLog(type.getId(),joinPlayerName);
          }
          case ZONG_MEN_POSITION_CHANGE -> {
            long targetPlayerId = (long)params[0];
            int oldPosition = (int)params[1];
            int newPosition = (int)params[2];
            PlayerManager.getInstance().getSimplePlayerFromRedisAsync(targetPlayerId).onSuccess(simplePlayer -> {
              addLog(type.getId(),simplePlayer.getName(),oldPosition+"",newPosition+"");
            });
          }

          case CHANGE_ZONG_MEN_NAME -> {
              String changeNamePlayerName = params[0]+"";
              String newName = params[1]+"";
              addLog(type.getId(),changeNamePlayerName,newName);
          }
          case QUIT_ZONG_MEN,
               CHANGE_ZONG_MEN_DECLARATION,
               CHANGE_ZONG_MEN_NOTICE -> {
              String quitPlayerName = params[0]+"";
              addLog(type.getId(),quitPlayerName);
          }
      }
  }

    private void addLog(int type, String... params) {
      long now = System.currentTimeMillis();
      List<String> paramList = new ArrayList<>();
      for (String param : params) {
          paramList.add(param);
      }
      OptLogData logData = new OptLogData(now,type,paramList);
      while (optLogDataList.size() >= GlobalConst.ZongmenLogNum){
          optLogDataList.remove(optLogDataList.size() - 1);
      }
      optLogDataList.add(logData);
    }

    public List<ZongMenMsg.ZongMenLogProto> toProto(){
      List<ZongMenMsg.ZongMenLogProto> list = new ArrayList<>();
      optLogDataList.forEach(optLogData -> {list.add(optLogData.toProto());});
      return list;
    }

    public static class OptLogData {
    long createTimer;
    int optType;
    List<String> params = new ArrayList<>();

    public OptLogData(long createTimer, int optType, List<String> params) {
      this.createTimer = createTimer;
      this.optType = optType;
      this.params = params;
    }

    public OptLogData() {}
    public ZongMenMsg.ZongMenLogProto toProto(){
        return ZongMenMsg.ZongMenLogProto.newBuilder().setType(optType).setCreateTimer((int)(createTimer/1000L)).addAllParams(params).build();
    }
  }
}
