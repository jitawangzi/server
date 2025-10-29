package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class MonthCardDoubleBonusRequest_15000016Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ShopMsg.MonthCardDoubleBonusRequest_15000016.Builder builder = cn.game.protocol.protobuf.ShopMsg.MonthCardDoubleBonusRequest_15000016
				.newBuilder();

		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ShopMsg.MonthCardDoubleBonusRequest_15000016.Builder builder = cn.game.protocol.protobuf.ShopMsg.MonthCardDoubleBonusRequest_15000016
				.newBuilder();

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		MonthCardDoubleBonusRequest_15000016Test instance = new MonthCardDoubleBonusRequest_15000016Test();
		instance.start();
	}

}