package cn.game.simulation.client.handler;

import java.util.List;
import org.springframework.stereotype.Component;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.protobuf.GemMsg.GemComposeResponse_10000008;
import cn.game.protocol.protobuf.GemMsg.GemLockResponse_10000006;
import cn.game.protocol.protobuf.GemMsg.GemTeardownResponse_10000004;
import cn.game.protocol.protobuf.GemMsg.GemWearResponse_10000002;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.simulation.client.Client;
import cn.game.protocol.protobuf.GemMsg.GemXiLianResponse_10000011;
import cn.game.protocol.protobuf.GemMsg.GemGacheResponse_10000013;

@Component
public class ClientGemHandler extends BaseHandler {

    @Override
    protected int getModule() {
        return 0x10;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.GemWearResponse_10000002, this::wear);
        putInvoker(PbProtocol.GemTeardownResponse_10000004, this::teardown);
        putInvoker(PbProtocol.GemLockResponse_10000006, this::lock);
        putInvoker(PbProtocol.GemComposeResponse_10000008, this::compose);
        putInvoker(PbProtocol.GemXiLianResponse_10000011, this::xiLian);
        putInvoker(PbProtocol.GemGacheResponse_10000013, this::gache);
    }

    private void wear(NetClient netClient, Object message) {
        GemWearResponse_10000002 resp = (GemWearResponse_10000002) message;
        Client client = (Client) netClient;
    }

    private void teardown(NetClient netClient, Object message) {
        GemTeardownResponse_10000004 resp = (GemTeardownResponse_10000004) message;
        Client client = (Client) netClient;
    }

    private void lock(NetClient netClient, Object message) {
        GemLockResponse_10000006 resp = (GemLockResponse_10000006) message;
        Client client = (Client) netClient;
    }

    private void compose(NetClient netClient, Object message) {
        GemComposeResponse_10000008 resp = (GemComposeResponse_10000008) message;
        List<RewardInfo> gemsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void xiLian(NetClient netClient, Object message) {
        GemXiLianResponse_10000011 resp = (GemXiLianResponse_10000011) message;
        GemInfo gem = resp.getGem();
        Client client = (Client) netClient;
    }

    private void gache(NetClient netClient, Object message) {
        GemGacheResponse_10000013 resp = (GemGacheResponse_10000013) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        int gemGacheFreeTimesNormal = resp.getGemGacheFreeTimesNormal();
        int gemGacheFreeTimesHigh = resp.getGemGacheFreeTimesHigh();
        int gemGacheBaoDiNormal = resp.getGemGacheBaoDiNormal();
        int gemGacheBaoDiHigh = resp.getGemGacheBaoDiHigh();
        Client client = (Client) netClient;
    }
}
