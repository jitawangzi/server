package cn.game.games.net.game.module.vip;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.generated.config.VIPConfig;
import cn.game.protocol.generated.manager.RandomAwardManager;
import cn.game.protocol.generated.manager.VIPManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.VipMsg;
import cn.game.util.DateUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName VipHandler
 *
 * @description:
 * @author: ly
 * @create: 2024-09-04 18:00 @Version 1.0
 */
@Component
public class VipHandler extends BaseHandler {
    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.getVipInfoRequest_34000001, this::getVipInfo);
        putInvoker(PbProtocol.rewardFreeVipGiftRequest_34000003, this::rewardFreeVipGift);
        putInvoker(PbProtocol.buyVipGiftRequest_34000005, this::buyVipGift);
    }

    @Override
    protected int getModule() {
        return 0x34;
    }

    private void getVipInfo(NetClient netClient, Object o) {
        VipMsg.getVipInfoResponse_34000002.Builder res = VipMsg.getVipInfoResponse_34000002.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(netClient.getPlayerId());
        VipModule vipModule = player.getVipModule();
        res.setInfo(vipModule.toPb());
        netClient.sendProtocol(res);
    }

    private void rewardFreeVipGift(NetClient client, Object o) {
        VipMsg.rewardFreeVipGiftResponse_34000004.Builder res = VipMsg.rewardFreeVipGiftResponse_34000004.newBuilder();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        VipModule vipModule = player.getVipModule();
        long now = System.currentTimeMillis();
        if (DateUtil.isSameDay(now,vipModule.getRewardFreeGiftTimer())){
            client.sendProtocol(res, ErrorMsgEnum.vip_free_gift_has_reward.getId());
            return;
        }
        vipModule.setRewardFreeGiftTimer(now);
        VIPConfig vipConfig = vipModule.getCurVipConfig();
        List<Goods> drops = PlayerHelper.randomReward(player,vipConfig.DailyBox);
        if (!drops.isEmpty()){
            res.addAllDrops(PlayerHelper.addGoods(player,drops, OpType.vipFreeGiftReward));
        }
        res.setRewardFreeGiftTimer((int) (vipModule.rewardFreeGiftTimer/1000L));
        client.sendProtocol(res);
    }

    private void buyVipGift(NetClient client, Object o) {
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        VipModule vipModule = player.getVipModule();
        VipMsg.buyVipGiftRequest_34000005 req = (VipMsg.buyVipGiftRequest_34000005)o;
        VipMsg.buyVipGiftResponse_34000006.Builder res = VipMsg.buyVipGiftResponse_34000006.newBuilder();
        int buyId = req.getId();
        if (vipModule.getBuyGiftList().contains(buyId)){
            client.sendProtocol(res, ErrorMsgEnum.vip_gift_has_reward.getId());
            return;
        }
        if (buyId > player.getVipLevel()){
            client.sendProtocol(res, ErrorMsgEnum.vip_gift_unlock.getId());
            return;
        }
        VIPConfig buyConfig = VIPManager.instance().get(buyId);
        if (buyConfig == null){
            client.sendProtocol(res, ErrorMsgEnum.config_data_not_found.getId());
            return;
        }
        player.pay(buyConfig.Price).onComplete(result->{
            if (result.result()){
                vipModule.getBuyGiftList().add(buyId);
                List<Goods> drops = Goods.valueOf(buyConfig.Item);
                if (!drops.isEmpty()){
                    res.addAllDrops(PlayerHelper.addGoods(player,drops, OpType.vipGiftReward));
                }
                res.addAllBuyGiftIdList(vipModule.getBuyGiftList());
                client.sendProtocol(res);
            } else {
                client.sendProtocol(res, ErrorMsgEnum.unknown.getId());
            }
        });

    }
}
