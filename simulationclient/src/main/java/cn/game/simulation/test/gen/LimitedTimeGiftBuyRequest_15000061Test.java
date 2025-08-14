package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class LimitedTimeGiftBuyRequest_15000061Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ShopMsg.LimitedTimeGiftBuyRequest_15000061.Builder builder = cn.game.protocol.protobuf.ShopMsg.LimitedTimeGiftBuyRequest_15000061.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ShopMsg.LimitedTimeGiftBuyRequest_15000061.Builder builder = cn.game.protocol.protobuf.ShopMsg.LimitedTimeGiftBuyRequest_15000061.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    LimitedTimeGiftBuyRequest_15000061Test instance = new LimitedTimeGiftBuyRequest_15000061Test();
	    instance.start();
	}

}