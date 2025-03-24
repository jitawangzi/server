package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityDayGiftPackageBuyRequest_11000106Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftPackageBuyRequest_11000106.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftPackageBuyRequest_11000106.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftPackageBuyRequest_11000106.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftPackageBuyRequest_11000106.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityDayGiftPackageBuyRequest_11000106Test instance = new ActivityDayGiftPackageBuyRequest_11000106Test();
	    instance.start();
	}

}