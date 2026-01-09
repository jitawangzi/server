package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PetBreakUpRequest_19000005Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.PetMsg.PetBreakUpRequest_19000005.Builder builder = cn.game.protocol.protobuf.PetMsg.PetBreakUpRequest_19000005.newBuilder() ; 
		
		builder.setId(640001);
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.PetMsg.PetBreakUpRequest_19000005.Builder builder = cn.game.protocol.protobuf.PetMsg.PetBreakUpRequest_19000005.newBuilder() ; 
		
		builder.setId(640001);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PetBreakUpRequest_19000005Test instance = new PetBreakUpRequest_19000005Test();
	    instance.start();
	}

}