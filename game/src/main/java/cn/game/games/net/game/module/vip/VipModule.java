package cn.game.games.net.game.module.vip;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.VIPConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.VIPManager;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.protocol.protobuf.VipMsg;
import cn.game.util.DateUtil;

/**
* @ClassName VipModule
* @description: VIP 模块
* @author: ly
* @create: 2024-09-04 15:46
* @Version 1.0
**/
public class VipModule extends BasePlayerModule {

    /**
     * 买过的VIP 一次性礼包 等级
     */
    List<Integer> buyGiftList = new ArrayList<>();


    /**
     * 免费礼包领取时间戳
     */
    long rewardFreeGiftTimer;
    /**领取过的免费礼包id*/
    List<Integer> rewardFreeGiftList = new ArrayList<>();

    @Override
    public void buildPlayerAllInfo(PlayerMsg.PlayerAllInfo.Builder builder) {
    }

    public List<Integer> getRewardFreeGiftList() {
        return rewardFreeGiftList;
    }

    public void setRewardFreeGiftList(List<Integer> rewardFreeGiftList) {
        this.rewardFreeGiftList = rewardFreeGiftList;
    }

    @Override
    public EventTypeEnum[] getEventTypes() {
        return new EventTypeEnum[]{EventTypeEnum.Charge,EventTypeEnum.LevelUp};
    }

    @Override
    public void handleEvent(GameEvent event) {
        switch (event.getType()){
            case Charge -> {
                int payNum = event.getIntParameter(0);
                int addExp =  GlobalConst.PayVIPExp * payNum;
                upVipLevel(addExp);
            }
            case LevelUp  -> {
                int type = event.getIntParameter(0);
                int level = event.getIntParameter(1);
                long curExp = event.getLongParameter(2);
                if (type == Asset.VIPExp.ID){
                    //旧的免费礼包未领取  在VIP升级后，没有领取的每日奖励通过邮件发送ID=8；
                    if (level > 0 && !getRewardFreeGiftList().contains(level - 1)){
                        sendFreeGiftMail(level -1);
                        rewardFreeGiftTimer = 0;
                        VipMsg.getVipInfoResponse_34000002.Builder res = VipMsg.getVipInfoResponse_34000002.newBuilder();
                        res.setInfo(toPb());
                        player.getGameClient().sendProtocol(res);
                    }
                   /* if (!DateUtil.isSameDay(System.currentTimeMillis(), rewardFreeGiftTimer)){
                        sendFreeGiftMail(level);
                    }*/
                }
                break;
            }
            default -> {
                log.error(String.format("VipModule handleEvent not found this type:%s, pid:%d",event.getType(),player.getPlayerId()));
            }
        }

    }

    private void sendFreeGiftMail(int level) {
        VIPConfig config = VIPManager.instance().get(level);
        MailHelper.sendMail(playerId, 8, PlayerHelper.randomReward(config.DailyBox), true);
        rewardFreeGiftList.add(config.ID);
        log.info(String.format( " sendFreeGiftMail vip levelUp curLevel:%d,  pid:%d", level,player.getPlayerId()));

    }

    private void upVipLevel(int addExp) {
        PlayerHelper.addResources(player,Asset.VIPExp.ID,addExp);
    }





    public List<Integer> getBuyGiftList() {
        return buyGiftList;
    }

    public void setBuyGiftList(List<Integer> buyGiftList) {
        this.buyGiftList = buyGiftList;
    }

    public long getRewardFreeGiftTimer() {
        return rewardFreeGiftTimer;
    }

    public void setRewardFreeGiftTimer(long rewardFreeGiftTimer) {
        this.rewardFreeGiftTimer = rewardFreeGiftTimer;
    }

    public VIPConfig getCurVipConfig() {
        return VIPManager.instance().get(player.getVipLevel());
    }

    public VipMsg.VipInfo toPb() {
        long now = System.currentTimeMillis();
    return VipMsg.VipInfo.newBuilder()
            .setRewardFreeGiftTimer((int) (rewardFreeGiftTimer / 1000L))
            .addAllBuyGiftIdList(buyGiftList)
            .setCanRewardFreeGift(!DateUtil.isSameDay(now,rewardFreeGiftTimer))
        .build();
    }
}
