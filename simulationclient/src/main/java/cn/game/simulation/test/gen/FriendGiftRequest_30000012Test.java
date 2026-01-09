package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class FriendGiftRequest_30000012Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendGiftRequest_30000012.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendGiftRequest_30000012
				.newBuilder();
		builder.addFriendId(240200680 + "");

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendGiftRequest_30000012.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendGiftRequest_30000012
				.newBuilder();
		if (client.friendsList.isEmpty()) {
			return null;
		}
		builder.addFriendId(Rnd.randomElement(client.friendsList).getPlayer().getId());
		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		FriendGiftRequest_30000012Test instance = new FriendGiftRequest_30000012Test();
		instance.start();
	}

}