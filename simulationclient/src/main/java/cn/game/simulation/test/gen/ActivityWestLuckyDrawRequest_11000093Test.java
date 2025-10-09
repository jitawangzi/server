package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityWestLuckyDrawRequest_11000093Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyDrawRequest_11000093.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyDrawRequest_11000093.newBuilder() ; 
		
		builder.setActivityId(1001); 
		builder.setDrawNum(1); 
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyDrawRequest_11000093.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyDrawRequest_11000093.newBuilder() ; 
		builder.setActivityId(1001); 
		builder.setDrawNum(1); 
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityWestLuckyDrawRequest_11000093Test instance = new ActivityWestLuckyDrawRequest_11000093Test();
	    instance.start();
	}

}