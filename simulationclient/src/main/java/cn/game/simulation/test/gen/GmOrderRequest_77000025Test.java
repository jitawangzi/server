package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmOrderRequest_77000025Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmOrderRequest_77000025.Builder builder = cn.game.protocol.protobuf.GmMsg.GmOrderRequest_77000025.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmOrderRequest_77000025.Builder builder = cn.game.protocol.protobuf.GmMsg.GmOrderRequest_77000025.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmOrderRequest_77000025Test instance = new GmOrderRequest_77000025Test();
	    instance.start();
	}

}