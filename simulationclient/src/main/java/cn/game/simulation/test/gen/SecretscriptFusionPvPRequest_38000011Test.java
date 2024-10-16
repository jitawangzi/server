package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class SecretscriptFusionPvPRequest_38000011Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptFusionPvPRequest_38000011.Builder builder = cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptFusionPvPRequest_38000011.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new SecretscriptFusionPvPRequest_38000011Test().getMessage(client));

		
	}

}