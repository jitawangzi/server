package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

//@Component
public class TestMessageRequest_6f000080Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestMessageRequest_6f000080.Builder builder = cn.game.protocol.protobuf.TestMsg.TestMessageRequest_6f000080.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestMessageRequest_6f000080.Builder builder = cn.game.protocol.protobuf.TestMsg.TestMessageRequest_6f000080.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    TestMessageRequest_6f000080Test instance = new TestMessageRequest_6f000080Test();
	    instance.start();
	}

}