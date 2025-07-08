package cn.game.games.net.game.module.develop.equip;

import org.springframework.stereotype.Component;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.protobuf.EquipMsg.EquipWearRequest_09000001;
import cn.game.protocol.protobuf.EquipMsg.EquipWearResponse_09000002;
import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.EquipMsg.EquipTeardownRequest_09000003;
import cn.game.protocol.protobuf.EquipMsg.EquipTeardownResponse_09000004;
import cn.game.protocol.protobuf.EquipMsg.EquipPartStrengthRequest_09000007;
import cn.game.protocol.protobuf.EquipMsg.EquipPartStrengthResponse_09000008;
import cn.game.protocol.protobuf.EquipMsg.EquipDecomposeRequest_09000005;
import cn.game.protocol.protobuf.EquipMsg.EquipDecomposeResponse_09000006;
import java.util.List;

/**
 * 装备
 */
@Component
public class EquipHandler extends BaseHandler {

    @Override
    protected int getModule() {
        return 0x09;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.EquipWearRequest_09000001, this::wear);
        putInvoker(PbProtocol.EquipTeardownRequest_09000003, this::teardown);
        putInvoker(PbProtocol.EquipPartStrengthRequest_09000007, this::partStrength);
        putInvoker(PbProtocol.EquipDecomposeRequest_09000005, this::decompose);
    }

    private void wear(NetClient client, Object message) {
        EquipWearRequest_09000001 req = (EquipWearRequest_09000001) message;
        String uid = req.getUid();
        EquipWearResponse_09000002 defaultInstance = EquipWearResponse_09000002.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(defaultInstance);
    }

    private void teardown(NetClient client, Object message) {
        EquipTeardownRequest_09000003 req = (EquipTeardownRequest_09000003) message;
        String uid = req.getUid();
        EquipTeardownResponse_09000004 defaultInstance = EquipTeardownResponse_09000004.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(defaultInstance);
    }

    private void partStrength(NetClient client, Object message) {
        EquipPartStrengthRequest_09000007 req = (EquipPartStrengthRequest_09000007) message;
        int type = req.getType();
        EquipPartStrengthResponse_09000008 defaultInstance = EquipPartStrengthResponse_09000008.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        EquipPartStrengthResponse_09000008.Builder resp = EquipPartStrengthResponse_09000008.newBuilder();
        client.sendProtocol(resp.build());
    }

    private void decompose(NetClient client, Object message) {
        EquipDecomposeRequest_09000005 req = (EquipDecomposeRequest_09000005) message;
        List<String> uidList = req.getUidList();
        EquipDecomposeResponse_09000006 defaultInstance = EquipDecomposeResponse_09000006.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(defaultInstance);
    }
}
