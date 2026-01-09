package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class MonthCardBuyRequest_15000010Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRequest_15000010.Builder builder = cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRequest_15000010
				.newBuilder();
		builder.setId(1);

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRequest_15000010.Builder builder = cn.game.protocol.protobuf.ShopMsg.MonthCardBuyRequest_15000010
				.newBuilder();
		builder.setId(1);

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		MonthCardBuyRequest_15000010Test instance = new MonthCardBuyRequest_15000010Test();
		instance.start();
	}

}