package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class FairyFriendGiftRequest_27000001Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendGiftRequest_27000001.Builder builder = cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendGiftRequest_27000001.newBuilder() ; 
		
		builder.setId(1);
		builder.setItemId(205021);
		builder.setCount(10);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendGiftRequest_27000001.Builder builder = cn.game.protocol.protobuf.FairyFriendMsg.FairyFriendGiftRequest_27000001.newBuilder() ; 
		
		builder.setId(1);
		builder.setItemId(205021);
		builder.setCount(10);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    FairyFriendGiftRequest_27000001Test instance = new FairyFriendGiftRequest_27000001Test();
	    instance.start();
	}

}