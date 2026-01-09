package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class TestRunRequest_6f000020Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestRunRequest_6f000020.Builder builder = cn.game.protocol.protobuf.TestMsg.TestRunRequest_6f000020.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestRunRequest_6f000020.Builder builder = cn.game.protocol.protobuf.TestMsg.TestRunRequest_6f000020.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    TestRunRequest_6f000020Test instance = new TestRunRequest_6f000020Test();
	    instance.start();
	}

}