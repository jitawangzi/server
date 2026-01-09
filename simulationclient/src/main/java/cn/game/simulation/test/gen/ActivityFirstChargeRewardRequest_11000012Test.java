package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityFirstChargeRewardRequest_11000012Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012.newBuilder() ;

		builder.setId(31);
		builder.setChargeId(34);
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeRewardRequest_11000012.newBuilder() ;

		builder.setId(10);
		builder.setChargeId(1);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityFirstChargeRewardRequest_11000012Test instance = new ActivityFirstChargeRewardRequest_11000012Test();
	    instance.start();
	}

}