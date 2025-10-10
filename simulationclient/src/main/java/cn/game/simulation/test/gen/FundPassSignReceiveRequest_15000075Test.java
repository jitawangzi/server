package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class FundPassSignReceiveRequest_15000075Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ShopMsg.FundPassSignReceiveRequest_15000075.Builder builder = cn.game.protocol.protobuf.ShopMsg.FundPassSignReceiveRequest_15000075.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ShopMsg.FundPassSignReceiveRequest_15000075.Builder builder = cn.game.protocol.protobuf.ShopMsg.FundPassSignReceiveRequest_15000075.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    FundPassSignReceiveRequest_15000075Test instance = new FundPassSignReceiveRequest_15000075Test();
	    instance.start();
	}

}