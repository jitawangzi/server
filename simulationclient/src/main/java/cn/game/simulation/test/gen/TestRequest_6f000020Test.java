package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

//@Component
public class TestRequest_6f000020Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestRequest_6f000020.Builder builder = cn.game.protocol.protobuf.TestMsg.TestRequest_6f000020.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestRequest_6f000020.Builder builder = cn.game.protocol.protobuf.TestMsg.TestRequest_6f000020.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    TestRequest_6f000020Test instance = new TestRequest_6f000020Test();
	    instance.start();
	}

}