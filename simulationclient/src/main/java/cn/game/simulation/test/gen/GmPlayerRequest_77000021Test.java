package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmPlayerRequest_77000021Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmPlayerRequest_77000021.Builder builder = cn.game.protocol.protobuf.GmMsg.GmPlayerRequest_77000021.newBuilder() ; 
		builder.setPlayerId(251220037 + "");
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmPlayerRequest_77000021.Builder builder = cn.game.protocol.protobuf.GmMsg.GmPlayerRequest_77000021.newBuilder() ; 
		builder.setPlayerId(251220037 + "");
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmPlayerRequest_77000021Test instance = new GmPlayerRequest_77000021Test();
	    instance.start();
	}

}