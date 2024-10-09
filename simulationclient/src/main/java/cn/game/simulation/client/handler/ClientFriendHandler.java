package cn.game.simulation.client.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.FriendMsg.FriendRecommendResponse_30000004;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.simulation.client.Client;


@Component
public class ClientFriendHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x30;
	}

	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.FriendRecommendResponse_30000004, this::recommendList);
		putInvoker(PbProtocol.FriendListResponse_30000002, this::list);
	}

	protected void recommendList(NetClient client, Object message) {
		Client client2 = (Client) client;
		FriendRecommendResponse_30000004 response = (FriendRecommendResponse_30000004) message;
		List<SimplePlayerInfo> playersList = response.getPlayersList();
		client2.recommendList = playersList;

	}

	protected void list(NetClient client, Object message) {
	}

}
