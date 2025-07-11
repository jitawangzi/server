package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GemWearRequest_10000001Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001.Builder builder = cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001.Builder builder = cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GemWearRequest_10000001Test instance = new GemWearRequest_10000001Test();
	    instance.start();
	}

}