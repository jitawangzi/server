package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class PlayerGenderRequest_01000017Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerGenderRequest_01000017.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerGenderRequest_01000017
				.newBuilder();

		builder.setIsMan(Rnd.nextBoolean());

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerGenderRequest_01000017.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerGenderRequest_01000017
				.newBuilder();

		builder.setIsMan(Rnd.nextBoolean());

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		PlayerGenderRequest_01000017Test instance = new PlayerGenderRequest_01000017Test();
		instance.start();
	}

}