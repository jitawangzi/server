package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ShopHeishiRefreshRequest_15000005Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopHeishiRefreshRequest_15000005.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopHeishiRefreshRequest_15000005
				.newBuilder();

		builder.setShopId(2);

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopHeishiRefreshRequest_15000005.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopHeishiRefreshRequest_15000005
				.newBuilder();

		builder.setShopId(2);

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		ShopHeishiRefreshRequest_15000005Test instance = new ShopHeishiRefreshRequest_15000005Test();
		instance.start();
	}

}