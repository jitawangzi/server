package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerCloudBoxRequest_01000042Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxRequest_01000042.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxRequest_01000042
				.newBuilder();

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxRequest_01000042.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerCloudBoxRequest_01000042
				.newBuilder();

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		PlayerCloudBoxRequest_01000042Test instance = new PlayerCloudBoxRequest_01000042Test();
		instance.start();
	}

}