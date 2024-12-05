package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

//@Component
public class TestGmCmdRequest_6f000001Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestGmCmdRequest_6f000001.Builder builder = cn.game.protocol.protobuf.TestMsg.TestGmCmdRequest_6f000001.newBuilder() ; 
		builder.setCmd("citem 100002");
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.TestMsg.TestGmCmdRequest_6f000001.Builder builder = cn.game.protocol.protobuf.TestMsg.TestGmCmdRequest_6f000001.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    TestGmCmdRequest_6f000001Test instance = new TestGmCmdRequest_6f000001Test();
	    instance.start();
	}

}