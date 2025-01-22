package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmPlayerDeleteRequest_77000052Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmPlayerDeleteRequest_77000052.Builder builder = cn.game.protocol.protobuf.GmMsg.GmPlayerDeleteRequest_77000052.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmPlayerDeleteRequest_77000052.Builder builder = cn.game.protocol.protobuf.GmMsg.GmPlayerDeleteRequest_77000052.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmPlayerDeleteRequest_77000052Test instance = new GmPlayerDeleteRequest_77000052Test();
	    instance.start();
	}

}