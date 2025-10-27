package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerImageRequest_01000019Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerImageRequest_01000019.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerImageRequest_01000019
				.newBuilder();

		builder.setImage(1);

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerImageRequest_01000019.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerImageRequest_01000019
				.newBuilder();

		builder.setImage(1);

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		PlayerImageRequest_01000019Test instance = new PlayerImageRequest_01000019Test();
		instance.start();
	}

}