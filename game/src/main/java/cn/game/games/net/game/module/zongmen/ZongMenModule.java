package cn.game.games.net.game.module.zongmen;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.protocol.protobuf.ZongMenMsg;

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
    /**解散次数 只有宗主才会有该操作**/
    int disbandCount;
    /**下次加入宗门的时间  第二次及后续解散时，宗主需要1小时才可加入其它宗门（ZongmenSuzerainCD） 第二次及后续退出时，需要1小时才可加入其它宗门（ZongmenMemberCD）**/
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
    public void buildPlayerAllInfo(PlayerMsg.PlayerAllInfo.Builder builder) {

    }

    @Override
    public EventTypeEnum[] getEventTypes() {
    return new EventTypeEnum[] {EventTypeEnum.LoginSuccess};
    }

    @Override
    public void handleEvent(GameEvent event) {
        switch (event.getType()){
            case LoginSuccess -> {
                checkZongMen();
            }
        }
    }

    private void checkZongMen() {
        ZongMenMsg.getZongMenInfoRequest_40000021 request =  ZongMenMsg.getZongMenInfoRequest_40000021.newBuilder().build();
    ZongMenGameHandler.sendMsgToZongMenServer(player, request)
        .onSuccess(
            msg -> {
                //玩家宗门 可能被解散了
              if (msg.errorCode == ErrorMsgEnum.zong_men_not_exist.ID) {
                  clearZongMen();
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
    }

    public void setZongMenInfo(ZongMenMsg.ZongMenInfoProto zongMen) {
        setZongMenId(zongMen.getSimpleInfo().getId());
        setZongMenName(zongMen.getSimpleInfo().getName());
    }

    public void kickZongMen(ZongMenMsg.notifyQuitZongMen_40000024 quitZongMenMsg) {
        clearZongMen();
        player.getGameClient().sendProtocol(quitZongMenMsg);
    }
}
