package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class FriendApplicationRequest_30000007Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.FriendMsg.FriendApplicationRequest_30000007.Builder builder = cn.game.protocol.protobuf.FriendMsg.FriendApplicationRequest_30000007.newBuilder() ; 
		
		builder.addPlayerIds(240200680 + "");
		builder.setAgree(true);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new FriendApplicationRequest_30000007Test().getMessage(client));

		
	}

}