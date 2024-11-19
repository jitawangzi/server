package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityLeiChongInfoRequest_11000051Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityLeiChongInfoRequest_11000051.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityLeiChongInfoRequest_11000051.newBuilder() ; 
		
		builder.setActivityId(23);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityLeiChongInfoRequest_11000051.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityLeiChongInfoRequest_11000051.newBuilder() ; 
		
		builder.setActivityId(23);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new ActivityLeiChongInfoRequest_11000051Test().getMessage(client));

		
	}

}