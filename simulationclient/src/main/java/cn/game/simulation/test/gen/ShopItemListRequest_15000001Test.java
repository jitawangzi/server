package cn.game.simulation.test.gen;

import java.util.Collection;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.ShopConfig;
import cn.game.protocol.generated.manager.ShopManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class ShopItemListRequest_15000001Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopItemListRequest_15000001.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopItemListRequest_15000001
				.newBuilder();

		builder.setShopId(17);

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopItemListRequest_15000001.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopItemListRequest_15000001
				.newBuilder();

		Collection<ShopConfig> list = ShopManager.instance().list();
		builder.setShopId(Rnd.randomElement(list).ID);

		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		ShopItemListRequest_15000001Test instance = new ShopItemListRequest_15000001Test();
		instance.start();
	}

}