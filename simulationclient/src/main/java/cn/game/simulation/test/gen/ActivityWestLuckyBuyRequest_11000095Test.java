package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityWestLuckyBuyRequest_11000095Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyBuyRequest_11000095.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyBuyRequest_11000095.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyBuyRequest_11000095.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyBuyRequest_11000095.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityWestLuckyBuyRequest_11000095Test instance = new ActivityWestLuckyBuyRequest_11000095Test();
	    instance.start();
	}

}