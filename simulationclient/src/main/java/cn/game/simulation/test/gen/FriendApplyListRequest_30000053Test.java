package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class FriendApplyListRequest_30000053Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendApplyListRequest_30000053.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendApplyListRequest_30000053
				.newBuilder();

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendApplyListRequest_30000053.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendApplyListRequest_30000053
				.newBuilder();
		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		FriendApplyListRequest_30000053Test instance = new FriendApplyListRequest_30000053Test();
		instance.start();
	}

}