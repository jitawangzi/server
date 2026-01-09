package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class buyVipGiftRequest_34000005Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.VipMsg.buyVipGiftRequest_34000005.Builder builder = cn.game.protocol.protobuf.VipMsg.buyVipGiftRequest_34000005.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.VipMsg.buyVipGiftRequest_34000005.Builder builder = cn.game.protocol.protobuf.VipMsg.buyVipGiftRequest_34000005.newBuilder() ; 
		
		
		builder.setId(0); 
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    buyVipGiftRequest_34000005Test instance = new buyVipGiftRequest_34000005Test();
	    instance.start();
	}

}