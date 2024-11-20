package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmMailDeleteRequest_77000046Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmMailDeleteRequest_77000046.Builder builder = cn.game.protocol.protobuf.GmMsg.GmMailDeleteRequest_77000046.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmMailDeleteRequest_77000046.Builder builder = cn.game.protocol.protobuf.GmMsg.GmMailDeleteRequest_77000046.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmMailDeleteRequest_77000046Test instance = new GmMailDeleteRequest_77000046Test();
	    instance.start();
	}

}