package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityJQBInfoRequest_11000081Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityJQBInfoRequest_11000081.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityJQBInfoRequest_11000081.newBuilder() ; 
		
		builder.setActivityId(22);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityJQBInfoRequest_11000081.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityJQBInfoRequest_11000081.newBuilder() ; 
		
		builder.setActivityId(22);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityJQBInfoRequest_11000081Test instance = new ActivityJQBInfoRequest_11000081Test();
	    instance.start();
	}

}