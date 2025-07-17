package cn.game.simulation.client.handler;

import java.util.List;
import org.springframework.stereotype.Component;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.protobuf.DevelopMsg.DevelopHeavenlyDaoLvUpResponse_25000011;
import cn.game.protocol.protobuf.DevelopMsg.DevelopPotentialBreakResponse_25000004;
import cn.game.protocol.protobuf.DevelopMsg.DevelopPotentialLvUpResponse_25000002;
import cn.game.protocol.protobuf.DevelopMsg.DevelopRescueLvUpResponse_25000008;
import cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorReplaceResponse_25000023;
import cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorReversalResponse_25000021;
import cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorSpiritualRootUnlockResponse_25000025;
import cn.game.protocol.protobuf.DevelopMsg.SpiritualInfo;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.simulation.client.Client;
import cn.game.protocol.protobuf.DevelopMsg.DefenceLevelUpResponse_25000032;
import cn.game.protocol.protobuf.DevelopMsg.DefenceSkinStarUpResponse_25000034;
import cn.game.protocol.protobuf.DevelopMsg.DefenceSkinChangeResponse_25000036;

@Component
public class ClientDevelopHandler extends BaseHandler {

    @Override
    protected int getModule() {
        return 0x25;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.DevelopPotentialLvUpResponse_25000002, this::potentialLvUp);
        putInvoker(PbProtocol.DevelopPotentialBreakResponse_25000004, this::potentialBreak);
        putInvoker(PbProtocol.DevelopRescueLvUpResponse_25000008, this::rescueLvUp);
        putInvoker(PbProtocol.DevelopHeavenlyDaoLvUpResponse_25000011, this::heavenlyDaoLvUp);
        putInvoker(PbProtocol.QianKunMirrorReversalResponse_25000021, this::qianKunMirrorReversal);
        putInvoker(PbProtocol.QianKunMirrorReplaceResponse_25000023, this::qianKunMirrorReplace);
        putInvoker(PbProtocol.QianKunMirrorSpiritualRootUnlockResponse_25000025, this::qianKunMirrorSpiritualRootUnlock);
        putInvoker(PbProtocol.DefenceLevelUpResponse_25000032, this::defenceLevelUp);
        putInvoker(PbProtocol.DefenceSkinStarUpResponse_25000034, this::defenceSkinStarUp);
        putInvoker(PbProtocol.DefenceSkinChangeResponse_25000036, this::defenceSkinChange);
    }

    private void potentialLvUp(NetClient netClient, Object message) {
        DevelopPotentialLvUpResponse_25000002 resp = (DevelopPotentialLvUpResponse_25000002) message;
        Client client = (Client) netClient;
    }

    private void potentialBreak(NetClient netClient, Object message) {
        DevelopPotentialBreakResponse_25000004 resp = (DevelopPotentialBreakResponse_25000004) message;
        Client client = (Client) netClient;
    }

    private void rescueLvUp(NetClient netClient, Object message) {
        DevelopRescueLvUpResponse_25000008 resp = (DevelopRescueLvUpResponse_25000008) message;
        Client client = (Client) netClient;
    }

    private void heavenlyDaoLvUp(NetClient netClient, Object message) {
        DevelopHeavenlyDaoLvUpResponse_25000011 resp = (DevelopHeavenlyDaoLvUpResponse_25000011) message;
        Client client = (Client) netClient;
    }

    private void qianKunMirrorReversal(NetClient netClient, Object message) {
        QianKunMirrorReversalResponse_25000021 resp = (QianKunMirrorReversalResponse_25000021) message;
        SpiritualInfo spiritual = resp.getSpiritual();
        Client client = (Client) netClient;
    }

    private void qianKunMirrorReplace(NetClient netClient, Object message) {
        QianKunMirrorReplaceResponse_25000023 resp = (QianKunMirrorReplaceResponse_25000023) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void qianKunMirrorSpiritualRootUnlock(NetClient netClient, Object message) {
        QianKunMirrorSpiritualRootUnlockResponse_25000025 resp = (QianKunMirrorSpiritualRootUnlockResponse_25000025) message;
        Client client = (Client) netClient;
    }

    private void defenceLevelUp(NetClient netClient, Object message) {
        DefenceLevelUpResponse_25000032 resp = (DefenceLevelUpResponse_25000032) message;
        Client client = (Client) netClient;
    }

    private void defenceSkinStarUp(NetClient netClient, Object message) {
        DefenceSkinStarUpResponse_25000034 resp = (DefenceSkinStarUpResponse_25000034) message;
        Client client = (Client) netClient;
    }

    private void defenceSkinChange(NetClient netClient, Object message) {
        DefenceSkinChangeResponse_25000036 resp = (DefenceSkinChangeResponse_25000036) message;
        Client client = (Client) netClient;
    }
}
