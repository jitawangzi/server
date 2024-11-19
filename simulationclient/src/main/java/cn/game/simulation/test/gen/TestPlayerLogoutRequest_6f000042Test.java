package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class TestPlayerLogoutRequest_6f000042Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestPlayerLogoutRequest_6f000042.Builder builder = cn.game.protocol.protobuf.TestMsg.TestPlayerLogoutRequest_6f000042.newBuilder() ; 
		builder.setPlayerId(329);
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestPlayerLogoutRequest_6f000042.Builder builder = cn.game.protocol.protobuf.TestMsg.TestPlayerLogoutRequest_6f000042.newBuilder() ; 
		builder.setPlayerId(329);
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new TestPlayerLogoutRequest_6f000042Test().getMessage(client));

		
	}

}