package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class FriendDeleteRequest_30000009Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendDeleteRequest_30000009.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendDeleteRequest_30000009.newBuilder() ; 
		builder.setId("2342323");
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new FriendDeleteRequest_30000009Test().getMessage(client));

		
	}

}