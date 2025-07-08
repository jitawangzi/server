package cn.game.simulation.client.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.protobuf.BaseMsg.EquipPartInfo;
import cn.game.protocol.protobuf.EquipMsg.EquipDecomposeResponse_09000006;
import cn.game.protocol.protobuf.EquipMsg.EquipPartStrengthResponse_09000008;
import cn.game.protocol.protobuf.EquipMsg.EquipTeardownResponse_09000004;
import cn.game.protocol.protobuf.EquipMsg.EquipWearResponse_09000002;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.simulation.client.Client;

@Component
public class ClientEquipHandler extends BaseHandler {

    @Override
    protected int getModule() {
        return 0x09;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.EquipWearResponse_09000002, this::wear);
        putInvoker(PbProtocol.EquipTeardownResponse_09000004, this::teardown);
        putInvoker(PbProtocol.EquipDecomposeResponse_09000006, this::decompose);
        putInvoker(PbProtocol.EquipPartStrengthResponse_09000008, this::partStrength);
    }

    private void wear(NetClient netClient, Object message) {
        EquipWearResponse_09000002 resp = (EquipWearResponse_09000002) message;
        Client client = (Client) netClient;
    }

    private void teardown(NetClient netClient, Object message) {
        EquipTeardownResponse_09000004 resp = (EquipTeardownResponse_09000004) message;
        Client client = (Client) netClient;
    }

    private void decompose(NetClient netClient, Object message) {
        EquipDecomposeResponse_09000006 resp = (EquipDecomposeResponse_09000006) message;
        Client client = (Client) netClient;
    }

    private void partStrength(NetClient netClient, Object message) {
        EquipPartStrengthResponse_09000008 resp = (EquipPartStrengthResponse_09000008) message;
        List<EquipPartInfo> partsList = resp.getPartsList();
        Client client = (Client) netClient;
    }
}
