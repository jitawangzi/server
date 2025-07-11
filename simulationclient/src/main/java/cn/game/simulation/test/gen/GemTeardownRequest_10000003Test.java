package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GemTeardownRequest_10000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemTeardownRequest_10000003.Builder builder = cn.game.protocol.protobuf.GemMsg.GemTeardownRequest_10000003.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemTeardownRequest_10000003.Builder builder = cn.game.protocol.protobuf.GemMsg.GemTeardownRequest_10000003.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GemTeardownRequest_10000003Test instance = new GemTeardownRequest_10000003Test();
	    instance.start();
	}

}