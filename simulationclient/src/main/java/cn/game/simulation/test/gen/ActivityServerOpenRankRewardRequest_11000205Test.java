package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityServerOpenRankRewardRequest_11000205Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankRewardRequest_11000205.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankRewardRequest_11000205.newBuilder() ; 
		
		builder.setId(42); 
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankRewardRequest_11000205.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityServerOpenRankRewardRequest_11000205.newBuilder() ; 
		
		builder.setId(42); 
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityServerOpenRankRewardRequest_11000205Test instance = new ActivityServerOpenRankRewardRequest_11000205Test();
	    instance.start();
	}

}