package cn.game.simulation.test.gen;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.ShopMsg.ShopItemProto;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class ShopItemBuyRequest_15000003Test extends ServerTest {

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopItemBuyRequest_15000003.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopItemBuyRequest_15000003
				.newBuilder();

		builder.setShopId(21);
		builder.setItemId(2101);
		builder.setCount(55);

		return builder.build();
	}

	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopItemBuyRequest_15000003.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopItemBuyRequest_15000003
				.newBuilder();

		Map<Integer, List<ShopItemProto>> shopItemMap = client.shopItemMap; 
		if (shopItemMap.isEmpty()) {
			return null; 
		}
		Integer randomElement = Rnd.randomElement(shopItemMap.keySet()); 
		List<ShopItemProto> list = shopItemMap.get(randomElement); 
		
		builder.setShopId(randomElement);
		builder.setItemId(Rnd.randomElement(list).getItemId()) ; 
		builder.setCount(1); 
		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		ShopItemBuyRequest_15000003Test instance = new ShopItemBuyRequest_15000003Test();
		instance.start();
	}

}