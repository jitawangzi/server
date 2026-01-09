package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ShopBoxOpenRequest_15000040Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopBoxOpenRequest_15000040.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopBoxOpenRequest_15000040
				.newBuilder();

		builder.setWatchAds(false);

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopBoxOpenRequest_15000040.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopBoxOpenRequest_15000040
				.newBuilder();

		builder.setWatchAds(false);

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		ShopBoxOpenRequest_15000040Test instance = new ShopBoxOpenRequest_15000040Test();
		instance.start();
	}

}