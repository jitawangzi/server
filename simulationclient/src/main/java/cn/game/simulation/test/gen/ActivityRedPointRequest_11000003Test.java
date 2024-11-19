package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityRedPointRequest_11000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityRedPointRequest_11000003.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityRedPointRequest_11000003.newBuilder() ; 
		builder.addIds(10);
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityRedPointRequest_11000003.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityRedPointRequest_11000003.newBuilder() ; 
		builder.addIds(10);
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new ActivityRedPointRequest_11000003Test().getMessage(client));

		
	}

}