package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerQuestionnaireRequest_01000300Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerQuestionnaireRequest_01000300.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerQuestionnaireRequest_01000300
				.newBuilder();

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerQuestionnaireRequest_01000300.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerQuestionnaireRequest_01000300
				.newBuilder();

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		PlayerQuestionnaireRequest_01000300Test instance = new PlayerQuestionnaireRequest_01000300Test();
		instance.start();
	}

}