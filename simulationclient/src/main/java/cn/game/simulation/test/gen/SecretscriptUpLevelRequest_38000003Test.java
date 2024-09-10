package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class SecretscriptUpLevelRequest_38000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptUpLevelRequest_38000003.Builder builder = cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptUpLevelRequest_38000003.newBuilder() ; 
		
		builder.setId(101);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new SecretscriptUpLevelRequest_38000003Test().getMessage(client));

		
	}

}