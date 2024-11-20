package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PetUpLevelRequest_19000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.PetMsg.PetUpLevelRequest_19000003.Builder builder = cn.game.protocol.protobuf.PetMsg.PetUpLevelRequest_19000003.newBuilder() ; 
		
		builder.setId(640001);
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.PetMsg.PetUpLevelRequest_19000003.Builder builder = cn.game.protocol.protobuf.PetMsg.PetUpLevelRequest_19000003.newBuilder() ; 
		
		builder.setId(640001);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PetUpLevelRequest_19000003Test instance = new PetUpLevelRequest_19000003Test();
	    instance.start();
	}

}