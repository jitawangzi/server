package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ShopFundPassBuyRequest_15000030Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopFundPassBuyRequest_15000030.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopFundPassBuyRequest_15000030.newBuilder() ; 
		builder.setId(2);
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopFundPassBuyRequest_15000030.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopFundPassBuyRequest_15000030.newBuilder() ; 
		builder.setId(2);
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ShopFundPassBuyRequest_15000030Test instance = new ShopFundPassBuyRequest_15000030Test();
	    instance.start();
	}

}