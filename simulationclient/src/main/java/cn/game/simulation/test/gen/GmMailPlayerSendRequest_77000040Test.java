package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmMailPlayerSendRequest_77000040Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmMailPlayerSendRequest_77000040.Builder builder = cn.game.protocol.protobuf.GmMsg.GmMailPlayerSendRequest_77000040.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmMailPlayerSendRequest_77000040.Builder builder = cn.game.protocol.protobuf.GmMsg.GmMailPlayerSendRequest_77000040.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new GmMailPlayerSendRequest_77000040Test().getMessage(client));

		
	}

}