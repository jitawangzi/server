package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class FriendDeleteRequest_30000009Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendDeleteRequest_30000009.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendDeleteRequest_30000009
				.newBuilder();
		builder.setId("2342323");

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendDeleteRequest_30000009.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendDeleteRequest_30000009
				.newBuilder();
		if (client.friendsList.isEmpty()) {
			return null;
		}
		builder.setId(Rnd.randomElement(client.friendsList).getPlayer().getId());
		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		FriendDeleteRequest_30000009Test instance = new FriendDeleteRequest_30000009Test();
		instance.start();
	}

}