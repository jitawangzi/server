package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmMailCheckRequest_77000044Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmMailCheckRequest_77000044.Builder builder = cn.game.protocol.protobuf.GmMsg.GmMailCheckRequest_77000044.newBuilder() ; 
		
		builder.addUid("3");
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmMailCheckRequest_77000044.Builder builder = cn.game.protocol.protobuf.GmMsg.GmMailCheckRequest_77000044.newBuilder() ; 
		
		builder.addUid("3");
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new GmMailCheckRequest_77000044Test().getMessage(client));

		
	}

}