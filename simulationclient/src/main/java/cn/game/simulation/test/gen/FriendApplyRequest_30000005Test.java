package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class FriendApplyRequest_30000005Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendApplyRequest_30000005.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendApplyRequest_30000005
				.newBuilder();

		builder.addPlayerIds(240200679 + "");

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendApplyRequest_30000005.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendApplyRequest_30000005
				.newBuilder();
		if (client.recommendList.isEmpty()) {
			return null;
		}
		SimplePlayerInfo randomElement = Rnd.randomElement(client.recommendList);
		builder.addPlayerIds(randomElement.getId());

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		FriendApplyRequest_30000005Test instance = new FriendApplyRequest_30000005Test();
		instance.start();
	}

}