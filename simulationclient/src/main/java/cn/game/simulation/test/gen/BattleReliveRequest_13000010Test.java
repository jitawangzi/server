package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleReliveRequest_13000010Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleReliveRequest_13000010.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleReliveRequest_13000010
				.newBuilder();

		builder.setType(2);

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleReliveRequest_13000010.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleReliveRequest_13000010
				.newBuilder();

		builder.setType(2);

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		BattleReliveRequest_13000010Test instance = new BattleReliveRequest_13000010Test();
		instance.start();
	}

}