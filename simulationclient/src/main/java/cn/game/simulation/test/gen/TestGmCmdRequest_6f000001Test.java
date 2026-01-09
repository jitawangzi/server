package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class TestGmCmdRequest_6f000001Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestGmCmdRequest_6f000001.Builder builder = cn.game.protocol.protobuf.TestMsg.TestGmCmdRequest_6f000001
				.newBuilder();
//		builder.setCmd("itemdel 240200370 100002 10");
		builder.setCmd("init");
//		builder.setCmd("playerdel 240222081");

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestGmCmdRequest_6f000001.Builder builder = cn.game.protocol.protobuf.TestMsg.TestGmCmdRequest_6f000001
				.newBuilder();
		builder.setCmd("init");

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		TestGmCmdRequest_6f000001Test instance = new TestGmCmdRequest_6f000001Test();
		instance.start();
	}

}