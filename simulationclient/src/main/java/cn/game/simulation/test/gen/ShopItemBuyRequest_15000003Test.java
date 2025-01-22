package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ShopItemBuyRequest_15000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopItemBuyRequest_15000003.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopItemBuyRequest_15000003.newBuilder() ; 
		
		builder.setShopId(2);
		builder.setItemId(100);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopItemBuyRequest_15000003.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopItemBuyRequest_15000003.newBuilder() ; 
		
		builder.setShopId(5);
		builder.setItemId(5);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ShopItemBuyRequest_15000003Test instance = new ShopItemBuyRequest_15000003Test();
	    instance.start();
	}

}