package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GemXiLianRequest_10000010Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemXiLianRequest_10000010.Builder builder = cn.game.protocol.protobuf.GemMsg.GemXiLianRequest_10000010.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemXiLianRequest_10000010.Builder builder = cn.game.protocol.protobuf.GemMsg.GemXiLianRequest_10000010.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GemXiLianRequest_10000010Test instance = new GemXiLianRequest_10000010Test();
	    instance.start();
	}

}