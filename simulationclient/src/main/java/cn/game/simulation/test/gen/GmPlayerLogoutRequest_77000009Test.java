package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmPlayerLogoutRequest_77000009Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmPlayerLogoutRequest_77000009.Builder builder = cn.game.protocol.protobuf.GmMsg.GmPlayerLogoutRequest_77000009.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmPlayerLogoutRequest_77000009.Builder builder = cn.game.protocol.protobuf.GmMsg.GmPlayerLogoutRequest_77000009.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmPlayerLogoutRequest_77000009Test instance = new GmPlayerLogoutRequest_77000009Test();
	    instance.start();
	}

}