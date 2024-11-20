package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class rewardFreeVipGiftRequest_34000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.VipMsg.rewardFreeVipGiftRequest_34000003.Builder builder = cn.game.protocol.protobuf.VipMsg.rewardFreeVipGiftRequest_34000003.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.VipMsg.rewardFreeVipGiftRequest_34000003.Builder builder = cn.game.protocol.protobuf.VipMsg.rewardFreeVipGiftRequest_34000003.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    rewardFreeVipGiftRequest_34000003Test instance = new rewardFreeVipGiftRequest_34000003Test();
	    instance.start();
	}

}