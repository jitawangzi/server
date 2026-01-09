package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityTaskRewardRequest_11000041Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityTaskRewardRequest_11000041.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityTaskRewardRequest_11000041.newBuilder() ; 
		builder.setActivityId(23);
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityTaskRewardRequest_11000041.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityTaskRewardRequest_11000041.newBuilder() ; 
		builder.setActivityId(23);
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityTaskRewardRequest_11000041Test instance = new ActivityTaskRewardRequest_11000041Test();
	    instance.start();
	}

}