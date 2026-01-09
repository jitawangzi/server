package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class TestClearResourceAndItemRequest_6f000040Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestClearResourceAndItemRequest_6f000040.Builder builder = cn.game.protocol.protobuf.TestMsg.TestClearResourceAndItemRequest_6f000040.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestClearResourceAndItemRequest_6f000040.Builder builder = cn.game.protocol.protobuf.TestMsg.TestClearResourceAndItemRequest_6f000040.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    TestClearResourceAndItemRequest_6f000040Test instance = new TestClearResourceAndItemRequest_6f000040Test();
	    instance.start();
	}

}