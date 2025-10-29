package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class MailListRequest_12000001Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.MailMsg.MailListRequest_12000001.Builder builder = cn.game.protocol.protobuf.MailMsg.MailListRequest_12000001
				.newBuilder();

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.MailMsg.MailListRequest_12000001.Builder builder = cn.game.protocol.protobuf.MailMsg.MailListRequest_12000001
				.newBuilder();

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		MailListRequest_12000001Test instance = new MailListRequest_12000001Test();
		instance.start();
	}

}