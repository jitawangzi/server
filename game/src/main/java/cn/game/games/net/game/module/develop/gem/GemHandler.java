package cn.game.games.net.game.module.develop.gem;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.protobuf.GemMsg.GemComposeRequest_10000007;
import cn.game.protocol.protobuf.GemMsg.GemComposeResponse_10000008;
import cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005;
import cn.game.protocol.protobuf.GemMsg.GemLockResponse_10000006;
import cn.game.protocol.protobuf.GemMsg.GemTeardownRequest_10000003;
import cn.game.protocol.protobuf.GemMsg.GemTeardownResponse_10000004;
import cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001;
import cn.game.protocol.protobuf.GemMsg.GemWearResponse_10000002;
import cn.game.protocol.protobuf.PbProtocol;

@Component
public class GemHandler extends BaseHandler {

    @Override
    protected int getModule() {
        return 0x10;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.GemWearRequest_10000001, this::wear);
        putInvoker(PbProtocol.GemTeardownRequest_10000003, this::teardown);
        putInvoker(PbProtocol.GemLockRequest_10000005, this::lock);
        putInvoker(PbProtocol.GemComposeRequest_10000007, this::compose);
    }

    private void wear(NetClient client, Object message) {
        GemWearRequest_10000001 req = (GemWearRequest_10000001) message;
        String uid = req.getUid();
        int pos = req.getPos();
        GemWearResponse_10000002 defaultInstance = GemWearResponse_10000002.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(defaultInstance);
    }

    private void teardown(NetClient client, Object message) {
        GemTeardownRequest_10000003 req = (GemTeardownRequest_10000003) message;
        String uid = req.getUid();
        GemTeardownResponse_10000004 defaultInstance = GemTeardownResponse_10000004.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(defaultInstance);
    }

    private void lock(NetClient client, Object message) {
        GemLockRequest_10000005 req = (GemLockRequest_10000005) message;
		List<String> uids = req.getUidList();
        boolean lock = req.getLock();
        GemLockResponse_10000006 defaultInstance = GemLockResponse_10000006.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        client.sendProtocol(defaultInstance);
    }

    private void compose(NetClient client, Object message) {
        GemComposeRequest_10000007 req = (GemComposeRequest_10000007) message;
        List<String> uidsList = req.getUidsList();
        GemComposeResponse_10000008 defaultInstance = GemComposeResponse_10000008.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        GemComposeResponse_10000008.Builder resp = GemComposeResponse_10000008.newBuilder();
        client.sendProtocol(resp.build());
    }
}
