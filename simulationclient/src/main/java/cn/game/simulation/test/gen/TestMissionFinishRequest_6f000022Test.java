package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

//@Component
public class TestMissionFinishRequest_6f000022Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestMissionFinishRequest_6f000022.Builder builder = cn.game.protocol.protobuf.TestMsg.TestMissionFinishRequest_6f000022.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestMissionFinishRequest_6f000022.Builder builder = cn.game.protocol.protobuf.TestMsg.TestMissionFinishRequest_6f000022.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    TestMissionFinishRequest_6f000022Test instance = new TestMissionFinishRequest_6f000022Test();
	    instance.start();
	}

}