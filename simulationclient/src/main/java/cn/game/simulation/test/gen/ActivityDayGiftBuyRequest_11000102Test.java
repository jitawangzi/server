package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityDayGiftBuyRequest_11000102Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftBuyRequest_11000102.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftBuyRequest_11000102.newBuilder() ; 
		
		builder.setId(38);
		builder.setGiftId(99);
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftBuyRequest_11000102.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityDayGiftBuyRequest_11000102.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityDayGiftBuyRequest_11000102Test instance = new ActivityDayGiftBuyRequest_11000102Test();
	    instance.start();
	}

}