package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityWestLuckyCountRewardRequest_11000097Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyCountRewardRequest_11000097.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyCountRewardRequest_11000097.newBuilder() ; 
		
		builder.setActivityId(1001); 
		builder.addRewardIndex(1); 
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyCountRewardRequest_11000097.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityWestLuckyCountRewardRequest_11000097.newBuilder() ; 
		
		builder.setActivityId(1001); 
		builder.addRewardIndex(1); 
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityWestLuckyCountRewardRequest_11000097Test instance = new ActivityWestLuckyCountRewardRequest_11000097Test();
	    instance.start();
	}

}