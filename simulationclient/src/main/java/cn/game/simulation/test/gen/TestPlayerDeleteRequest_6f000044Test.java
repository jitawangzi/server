package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class TestPlayerDeleteRequest_6f000044Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestPlayerDeleteRequest_6f000044.Builder builder = cn.game.protocol.protobuf.TestMsg.TestPlayerDeleteRequest_6f000044.newBuilder() ; 
		
		builder.setPlayerId(240201711);
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestPlayerDeleteRequest_6f000044.Builder builder = cn.game.protocol.protobuf.TestMsg.TestPlayerDeleteRequest_6f000044.newBuilder() ; 
		
		builder.setPlayerId(240200002);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    TestPlayerDeleteRequest_6f000044Test instance = new TestPlayerDeleteRequest_6f000044Test();
	    instance.start();
	}

}