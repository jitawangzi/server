package cn.game.games.net.game.module.gem;

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

		putInvoker(PbProtocol.GemComposeRequest_10000007, this::compose);
		putInvoker(PbProtocol.GemLockRequest_10000005, this::lock);
		putInvoker(PbProtocol.GemTeardownRequest_10000003, this::tearDown);
		putInvoker(PbProtocol.GemWearRequest_10000001, this::wear);
	}

	private void compose(NetClient client, Object message) {
		GemComposeRequest_10000007 req = (GemComposeRequest_10000007) message;
		GemComposeResponse_10000008.Builder resp = GemComposeResponse_10000008.newBuilder();

		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		client.sendProtocol(resp.build());
	}

	private void lock(NetClient client, Object message) {
		GemLockRequest_10000005 req = (GemLockRequest_10000005) message;
		GemLockResponse_10000006.Builder resp = GemLockResponse_10000006.newBuilder();
		client.sendProtocol(resp.build());
	}

	private void wear(NetClient client, Object message) {
		GemWearRequest_10000001 req = (GemWearRequest_10000001) message;
		GemWearResponse_10000002.Builder resp = GemWearResponse_10000002.newBuilder();
		client.sendProtocol(resp.build());
	}

	private void tearDown(NetClient client, Object message) {
		GemTeardownRequest_10000003 req = (GemTeardownRequest_10000003) message;
		GemTeardownResponse_10000004.Builder resp = GemTeardownResponse_10000004.newBuilder();
		client.sendProtocol(resp.build());
	}
}

