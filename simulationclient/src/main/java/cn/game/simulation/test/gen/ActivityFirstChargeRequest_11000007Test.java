package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityFirstChargeRequest_11000007Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRequest_11000007.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRequest_11000007.newBuilder() ;

		builder.setId(10);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRequest_11000007.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRequest_11000007.newBuilder() ;

		builder.setId(10);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityFirstChargeRequest_11000007Test instance = new ActivityFirstChargeRequest_11000007Test();
	    instance.start();
	}

}