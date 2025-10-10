package cn.game.simulation.client.handler;

import java.util.List;
import org.springframework.stereotype.Component;
import cn.game.core.net.client.NetClient;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.protocol.protobuf.BaseMsg.PaymentOrderProto;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.ShopMsg.BuyXianShiLiBaoResponse_15000053;
import cn.game.protocol.protobuf.ShopMsg.GetXianShiLiBaoInfoResponse_15000051;
import cn.game.protocol.protobuf.ShopMsg.LimitedTimeGiftBuyResponse_15000062;
import cn.game.protocol.protobuf.ShopMsg.LimitedTimeGiftInfo;
import cn.game.protocol.protobuf.ShopMsg.LimitedTimeGiftPush_15100054;
import cn.game.protocol.protobuf.ShopMsg.MonthCardBuyResponse_15000011;
import cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRewardResponse_15000013;
import cn.game.protocol.protobuf.ShopMsg.MonthCardDayRewardResponse_15000015;
import cn.game.protocol.protobuf.ShopMsg.MonthCardDoubleBonusResponse_15000017;
import cn.game.protocol.protobuf.ShopMsg.MonthCardProto;
import cn.game.protocol.protobuf.ShopMsg.PaymentOrderPush_15010020;
import cn.game.protocol.protobuf.ShopMsg.ShopBoxOpenResponse_15000041;
import cn.game.protocol.protobuf.ShopMsg.ShopChapterPacksBuyResponse_15000021;
import cn.game.protocol.protobuf.ShopMsg.ShopFundPassBuyResponse_15000031;
import cn.game.protocol.protobuf.ShopMsg.ShopFundPassRewardResponse_15000033;
import cn.game.protocol.protobuf.ShopMsg.ShopHeishiRefreshResponse_15000006;
import cn.game.protocol.protobuf.ShopMsg.ShopItemBuyResponse_15000004;
import cn.game.protocol.protobuf.ShopMsg.ShopItemListResponse_15000002;
import cn.game.protocol.protobuf.ShopMsg.ShopItemProto;
import cn.game.protocol.protobuf.ShopMsg.ShopRechargeResponse_15000023;
import cn.game.protocol.protobuf.ShopMsg.XianShiLiBaoInfo;
import cn.game.simulation.client.Client;
import cn.game.protocol.protobuf.ShopMsg.FundPassSignBuyResponse_15000072;
import cn.game.protocol.protobuf.ShopMsg.FundPassSignAllResponse_15000074;
import cn.game.protocol.protobuf.ShopMsg.FundPassSignReceiveResponse_15000076;

@Component
public class ClientShopHandler extends GameBaseHandler {

    @Override
    protected int getModule() {
        return 0x15;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.ShopItemListResponse_15000002, this::itemList);
        putInvoker(PbProtocol.ShopItemBuyResponse_15000004, this::itemBuy);
        putInvoker(PbProtocol.ShopHeishiRefreshResponse_15000006, this::heishiRefresh);
        putInvoker(PbProtocol.MonthCardBuyResponse_15000011, this::monthCardBuy);
        putInvoker(PbProtocol.MonthCardBuyRewardResponse_15000013, this::monthCardBuyReward);
        putInvoker(PbProtocol.MonthCardDayRewardResponse_15000015, this::monthCardDayReward);
        putInvoker(PbProtocol.MonthCardDoubleBonusResponse_15000017, this::monthCardDoubleBonus);
        putInvoker(PbProtocol.ShopChapterPacksBuyResponse_15000021, this::chapterPacksBuy);
        putInvoker(PbProtocol.ShopRechargeResponse_15000023, this::recharge);
        putInvoker(PbProtocol.PaymentOrderPush_15010020, this::paymentOrderPush);
        putInvoker(PbProtocol.ShopFundPassBuyResponse_15000031, this::fundPassBuy);
        putInvoker(PbProtocol.ShopFundPassRewardResponse_15000033, this::fundPassReward);
        putInvoker(PbProtocol.ShopBoxOpenResponse_15000041, this::boxOpen);
        putInvoker(PbProtocol.GetXianShiLiBaoInfoResponse_15000051, this::getXianShiLiBaoInfo);
        putInvoker(PbProtocol.BuyXianShiLiBaoResponse_15000053, this::buyXianShiLiBao);
        putInvoker(PbProtocol.LimitedTimeGiftBuyResponse_15000062, this::limitedTimeGiftBuy);
        putInvoker(PbProtocol.LimitedTimeGiftPush_15100054, this::limitedTimeGiftPush);
        putInvoker(PbProtocol.FundPassSignBuyResponse_15000072, this::fundPassSignBuy);
        putInvoker(PbProtocol.FundPassSignAllResponse_15000074, this::fundPassSignAll);
        putInvoker(PbProtocol.FundPassSignReceiveResponse_15000076, this::fundPassSignReceive);
    }

    private void itemList(NetClient netClient, Object message) {
        ShopItemListResponse_15000002 resp = (ShopItemListResponse_15000002) message;
        List<ShopItemProto> itemsList = resp.getItemsList();
        Client client = (Client) netClient;
    }

    private void itemBuy(NetClient netClient, Object message) {
        ShopItemBuyResponse_15000004 resp = (ShopItemBuyResponse_15000004) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void heishiRefresh(NetClient netClient, Object message) {
        ShopHeishiRefreshResponse_15000006 resp = (ShopHeishiRefreshResponse_15000006) message;
        List<ShopItemProto> itemsList = resp.getItemsList();
        Client client = (Client) netClient;
    }

    private void monthCardBuy(NetClient netClient, Object message) {
        MonthCardBuyResponse_15000011 resp = (MonthCardBuyResponse_15000011) message;
        MonthCardProto monthCard = resp.getMonthCard();
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void monthCardBuyReward(NetClient netClient, Object message) {
        MonthCardBuyRewardResponse_15000013 resp = (MonthCardBuyRewardResponse_15000013) message;
        Client client = (Client) netClient;
    }

    private void monthCardDayReward(NetClient netClient, Object message) {
        MonthCardDayRewardResponse_15000015 resp = (MonthCardDayRewardResponse_15000015) message;
        Client client = (Client) netClient;
    }

    private void monthCardDoubleBonus(NetClient netClient, Object message) {
        MonthCardDoubleBonusResponse_15000017 resp = (MonthCardDoubleBonusResponse_15000017) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void chapterPacksBuy(NetClient netClient, Object message) {
        ShopChapterPacksBuyResponse_15000021 resp = (ShopChapterPacksBuyResponse_15000021) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void recharge(NetClient netClient, Object message) {
        ShopRechargeResponse_15000023 resp = (ShopRechargeResponse_15000023) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void paymentOrderPush(NetClient netClient, Object message) {
        PaymentOrderPush_15010020 resp = (PaymentOrderPush_15010020) message;
        PaymentOrderProto order = resp.getOrder();
        String orderId = resp.getOrderId();
        Client client = (Client) netClient;
    }

    private void fundPassBuy(NetClient netClient, Object message) {
        ShopFundPassBuyResponse_15000031 resp = (ShopFundPassBuyResponse_15000031) message;
        Client client = (Client) netClient;
    }

    private void fundPassReward(NetClient netClient, Object message) {
        ShopFundPassRewardResponse_15000033 resp = (ShopFundPassRewardResponse_15000033) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void boxOpen(NetClient netClient, Object message) {
        ShopBoxOpenResponse_15000041 resp = (ShopBoxOpenResponse_15000041) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void getXianShiLiBaoInfo(NetClient netClient, Object message) {
        GetXianShiLiBaoInfoResponse_15000051 resp = (GetXianShiLiBaoInfoResponse_15000051) message;
        List<XianShiLiBaoInfo> infosList = resp.getInfosList();
        Client client = (Client) netClient;
    }

    private void buyXianShiLiBao(NetClient netClient, Object message) {
        BuyXianShiLiBaoResponse_15000053 resp = (BuyXianShiLiBaoResponse_15000053) message;
        int id = resp.getId();
        List<RewardInfo> rewardsList = resp.getRewardsList();
        XianShiLiBaoInfo info = resp.getInfo();
        Client client = (Client) netClient;
    }

    private void limitedTimeGiftBuy(NetClient netClient, Object message) {
        LimitedTimeGiftBuyResponse_15000062 resp = (LimitedTimeGiftBuyResponse_15000062) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void limitedTimeGiftPush(NetClient netClient, Object message) {
        LimitedTimeGiftPush_15100054 resp = (LimitedTimeGiftPush_15100054) message;
        LimitedTimeGiftInfo info = resp.getInfo();
        Client client = (Client) netClient;
    }

    private void fundPassSignBuy(NetClient netClient, Object message) {
        FundPassSignBuyResponse_15000072 resp = (FundPassSignBuyResponse_15000072) message;
        List<FundPassSignInfo> infoList = resp.getInfoList();
        Client client = (Client) netClient;
    }

    private void fundPassSignAll(NetClient netClient, Object message) {
        FundPassSignAllResponse_15000074 resp = (FundPassSignAllResponse_15000074) message;
        List<FundPassSignInfo> infoList = resp.getInfoList();
        Client client = (Client) netClient;
    }

    private void fundPassSignReceive(NetClient netClient, Object message) {
        FundPassSignReceiveResponse_15000076 resp = (FundPassSignReceiveResponse_15000076) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }
}
