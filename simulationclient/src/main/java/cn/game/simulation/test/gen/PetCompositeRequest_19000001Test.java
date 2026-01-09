package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PetCompositeRequest_19000001Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.PetMsg.PetCompositeRequest_19000001.Builder builder = cn.game.protocol.protobuf.PetMsg.PetCompositeRequest_19000001.newBuilder() ; 
		
		builder.setId(640001);
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.PetMsg.PetCompositeRequest_19000001.Builder builder = cn.game.protocol.protobuf.PetMsg.PetCompositeRequest_19000001.newBuilder() ; 
		
		builder.setId(640001);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PetCompositeRequest_19000001Test instance = new PetCompositeRequest_19000001Test();
	    instance.start();
	}

}