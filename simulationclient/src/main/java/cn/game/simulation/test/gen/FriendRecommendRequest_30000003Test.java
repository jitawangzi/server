package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class FriendRecommendRequest_30000003Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendRecommendRequest_30000003.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendRecommendRequest_30000003
				.newBuilder();

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendRecommendRequest_30000003.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendRecommendRequest_30000003
				.newBuilder();

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		FriendRecommendRequest_30000003Test instance = new FriendRecommendRequest_30000003Test();
		instance.start();
	}

}