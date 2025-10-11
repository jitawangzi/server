package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class FundPassSignBuyRequest_15000071Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ShopMsg.FundPassSignBuyRequest_15000071.Builder builder = cn.game.protocol.protobuf.ShopMsg.FundPassSignBuyRequest_15000071.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ShopMsg.FundPassSignBuyRequest_15000071.Builder builder = cn.game.protocol.protobuf.ShopMsg.FundPassSignBuyRequest_15000071.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    FundPassSignBuyRequest_15000071Test instance = new FundPassSignBuyRequest_15000071Test();
	    instance.start();
	}

}