package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class SecretscriptFusionRequest_38000007Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptFusionRequest_38000007.Builder builder = cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptFusionRequest_38000007.newBuilder() ; 
		
		builder.setId(101);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptFusionRequest_38000007.Builder builder = cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptFusionRequest_38000007.newBuilder() ; 
		
		builder.setId(101);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    SecretscriptFusionRequest_38000007Test instance = new SecretscriptFusionRequest_38000007Test();
	    instance.start();
	}

}