package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class MailReceiveRequest_12000005Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.MailMsg.MailReceiveRequest_12000005.Builder builder = cn.game.protocol.protobuf.MailMsg.MailReceiveRequest_12000005
				.newBuilder();

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.MailMsg.MailReceiveRequest_12000005.Builder builder = cn.game.protocol.protobuf.MailMsg.MailReceiveRequest_12000005
				.newBuilder();
//		if (client.mailsList.isEmpty()) {
//			return null;
//		}
//		builder.setUid(Rnd.randomElement(client.mailsList).getUid());
		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		MailReceiveRequest_12000005Test instance = new MailReceiveRequest_12000005Test();
		instance.start();
	}

}