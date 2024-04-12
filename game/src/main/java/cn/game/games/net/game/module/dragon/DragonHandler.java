package cn.game.games.net.game.module.dragon;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.protobuf.DragonMsg.DragonSkillUpRequest_17000005;
import cn.game.protocol.protobuf.DragonMsg.DragonSkillUpResponse_17000006;
import cn.game.protocol.protobuf.DragonMsg.DragonStarUpRequest_17000003;
import cn.game.protocol.protobuf.DragonMsg.DragonStarUpResponse_17000004;
import cn.game.protocol.protobuf.DragonMsg.DragonUnlockRequest_17000001;
import cn.game.protocol.protobuf.DragonMsg.DragonUnlockResponse_17000002;
import cn.game.protocol.protobuf.PbProtocol;

@Component
public class DragonHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x17;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.DragonUnlockRequest_17000001, this::unlock);
		putInvoker(PbProtocol.DragonStarUpRequest_17000003, this::starUp);
		putInvoker(PbProtocol.DragonSkillUpRequest_17000005, this::skillUp);
	}

	private void unlock(NetClient client, Object message) {
		DragonUnlockRequest_17000001 req = (DragonUnlockRequest_17000001) message;
		DragonUnlockResponse_17000002.Builder resp = DragonUnlockResponse_17000002.newBuilder();
		client.sendProtocol(resp.build());
	}

	private void starUp(NetClient client, Object message) {
		DragonStarUpRequest_17000003 req = (DragonStarUpRequest_17000003) message;
		DragonStarUpResponse_17000004.Builder resp = DragonStarUpResponse_17000004.newBuilder();
		client.sendProtocol(resp.build());
	}

	private void skillUp(NetClient client, Object message) {
		DragonSkillUpRequest_17000005 req = (DragonSkillUpRequest_17000005) message;
		DragonSkillUpResponse_17000006.Builder resp = DragonSkillUpResponse_17000006.newBuilder();
		client.sendProtocol(resp.build());
	}
}

