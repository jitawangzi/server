package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ActivityQingShenInfoRequest_11000071Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityQingShenInfoRequest_11000071.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityQingShenInfoRequest_11000071.newBuilder() ; 
		builder.setActivityId(25);
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.ActivityMsg.ActivityQingShenInfoRequest_11000071.Builder builder = cn.game.protocol.protobuf.ActivityMsg.ActivityQingShenInfoRequest_11000071.newBuilder() ; 
		builder.setActivityId(25);
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ActivityQingShenInfoRequest_11000071Test instance = new ActivityQingShenInfoRequest_11000071Test();
	    instance.start();
	}

}