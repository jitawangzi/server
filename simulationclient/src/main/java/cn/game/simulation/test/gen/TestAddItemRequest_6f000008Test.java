package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class TestAddItemRequest_6f000008Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestAddItemRequest_6f000008.Builder builder = cn.game.protocol.protobuf.TestMsg.TestAddItemRequest_6f000008
				.newBuilder();
//		builder.setId(Asset.playerExp.ID);
//		builder.setId(214001);
//		builder.setId(80);
//		builder.setCount(Integer.MAX_VALUE / 2);
//		builder.setCount(0);
//		
		builder.setId(100201);
		builder.setCount(1000000);

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestAddItemRequest_6f000008.Builder builder = cn.game.protocol.protobuf.TestMsg.TestAddItemRequest_6f000008
				.newBuilder();

//		builder.setId(Asset.playerExp.ID);
//		builder.setCount(Integer.MAX_VALUE / 2);
//		
		builder.setId(100201);
		builder.setCount(1000000);

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		TestAddItemRequest_6f000008Test instance = new TestAddItemRequest_6f000008Test();
		instance.start();
	}

}