package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class FriendBlackRequest_30000010Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendBlackRequest_30000010.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendBlackRequest_30000010
				.newBuilder();
		builder.setId(240200679 + "");

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendBlackRequest_30000010.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendBlackRequest_30000010
				.newBuilder();
		if (client.friendsList.isEmpty()) {
			return null; 
		}
		builder.setId(Rnd.randomElement(client.friendsList).getPlayer().getId());

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		FriendBlackRequest_30000010Test instance = new FriendBlackRequest_30000010Test();
		instance.start();
	}

}