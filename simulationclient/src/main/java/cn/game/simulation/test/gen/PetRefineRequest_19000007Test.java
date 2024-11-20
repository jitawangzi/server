package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PetRefineRequest_19000007Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PetMsg.PetRefineRequest_19000007.Builder builder = cn.game.protocol.protobuf.PetMsg.PetRefineRequest_19000007.newBuilder() ; 
		
		builder.setId(640001);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PetMsg.PetRefineRequest_19000007.Builder builder = cn.game.protocol.protobuf.PetMsg.PetRefineRequest_19000007.newBuilder() ; 
		
		builder.setId(640001);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PetRefineRequest_19000007Test instance = new PetRefineRequest_19000007Test();
	    instance.start();
	}

}