package cn.game.simulation.client.handler;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.protobuf.PbProtocol;


@Component
public class ClientFriendHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x30;
	}

	@Override
	protected void inititialize() {

//		putInvoker(PbProtocol.FriendRecommendResponse_30000004, this::list);
//		putInvoker(PbProtocol.FriendListResponse_30000002, this::list);
	}
	protected void list(NetClient client, Object message) {
	}

}
