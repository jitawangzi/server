package cn.game.games.net.cross.zongmen;

import cn.game.protocol.generated.config.ShopConfig;
import cn.game.protocol.generated.manager.ShopManager;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.DateUtil;

/**
 * @ClassName ZongMenShop
 *
 * @description:
 * @author: ly
 * @create: 2025-02-12 15:20 @Version 1.0
 */
public class ZongMenShop implements ZongMenConstants.ZongMenEventHandler {
    long nextRefreshTimer;


    void init(ZongMenInfo info){
        checkRefreshTimer(info);
    }

    private void checkRefreshTimer(ZongMenInfo info) {
        long now = System.currentTimeMillis();
        ShopConfig shopConfig = ShopManager.instance().get(17);
        if (shopConfig.Refresh == 2){
            if (DateUtil.diffDays(nextRefreshTimer,now) >= 7){
                nextRefreshTimer = DateUtil.addWeekBeginTimer(1);
                info.refreshShopByWeek();
            }
        }
    }

    @Override
    public ZongMenConstants.ZongMenEvenType[] getRegisterEvent() {
        return new ZongMenConstants.ZongMenEvenType[]{ZongMenConstants.ZongMenEvenType.CROSS_DAY};
    }

    @Override
    public void handleEventType(ZongMenConstants.ZongMenEvenType type, ZongMenInfo info, Object... params) {
            switch (type){
                case CROSS_DAY -> checkRefreshTimer(info);
            }
    }

    public ZongMenMsg.ZongMenShopProto toProto(ZongMenMember member){
        return ZongMenMsg.ZongMenShopProto.newBuilder()
                .setNextRefreshTimer(nextRefreshTimer/1000)
                .putAllItemBuyNumMap(member.buyShopItemNumMap).build();
    }
}
