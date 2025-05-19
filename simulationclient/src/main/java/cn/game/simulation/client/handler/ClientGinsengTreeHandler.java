package cn.game.simulation.client.handler;

import java.util.List;
import org.springframework.stereotype.Component;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeBugResponse_39000006;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeFertilizationResponse_39000012;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHarvestResponse_39000014;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeInfo;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeInfoResponse_39000002;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeInsecticidesResponse_39000008;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeWateringResponse_39000004;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.simulation.client.Client;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHangUpResponse_39000016;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHeroResponse_39000018;

@Component
public class ClientGinsengTreeHandler extends BaseHandler {

    @Override
    protected int getModule() {
        return 0x39;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.GinsengTreeInfoResponse_39000002, this::info);
        putInvoker(PbProtocol.GinsengTreeWateringResponse_39000004, this::watering);
        putInvoker(PbProtocol.GinsengTreeBugResponse_39000006, this::bug);
        putInvoker(PbProtocol.GinsengTreeInsecticidesResponse_39000008, this::insecticides);
        putInvoker(PbProtocol.GinsengTreeFertilizationResponse_39000012, this::fertilization);
        putInvoker(PbProtocol.GinsengTreeHarvestResponse_39000014, this::harvest);
        putInvoker(PbProtocol.GinsengTreeHangUpResponse_39000016, this::hangUp);
        putInvoker(PbProtocol.GinsengTreeHeroResponse_39000018, this::hero);
    }

    private void info(NetClient netClient, Object message) {
        GinsengTreeInfoResponse_39000002 resp = (GinsengTreeInfoResponse_39000002) message;
        GinsengTreeInfo treeInfo = resp.getTreeInfo();
        Client client = (Client) netClient;
    }

    private void watering(NetClient netClient, Object message) {
        GinsengTreeWateringResponse_39000004 resp = (GinsengTreeWateringResponse_39000004) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void bug(NetClient netClient, Object message) {
        GinsengTreeBugResponse_39000006 resp = (GinsengTreeBugResponse_39000006) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void insecticides(NetClient netClient, Object message) {
        GinsengTreeInsecticidesResponse_39000008 resp = (GinsengTreeInsecticidesResponse_39000008) message;
        GinsengTreeInfo treeInfo = resp.getTreeInfo();
        Client client = (Client) netClient;
    }

    private void fertilization(NetClient netClient, Object message) {
        GinsengTreeFertilizationResponse_39000012 resp = (GinsengTreeFertilizationResponse_39000012) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        GinsengTreeInfo treeInfo = resp.getTreeInfo();
        Client client = (Client) netClient;
    }

    private void harvest(NetClient netClient, Object message) {
        GinsengTreeHarvestResponse_39000014 resp = (GinsengTreeHarvestResponse_39000014) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void hangUp(NetClient netClient, Object message) {
        GinsengTreeHangUpResponse_39000016 resp = (GinsengTreeHangUpResponse_39000016) message;
        List<RewardInfo> rewardsList = resp.getRewardsList();
        Client client = (Client) netClient;
    }

    private void hero(NetClient netClient, Object message) {
        GinsengTreeHeroResponse_39000018 resp = (GinsengTreeHeroResponse_39000018) message;
        Client client = (Client) netClient;
    }
}
