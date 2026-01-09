package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PetBondsActivateRequest_19000013Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.PetMsg.PetBondsActivateRequest_19000013.Builder builder = cn.game.protocol.protobuf.PetMsg.PetBondsActivateRequest_19000013.newBuilder() ; 
		
		builder.setId(1);
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.PetMsg.PetBondsActivateRequest_19000013.Builder builder = cn.game.protocol.protobuf.PetMsg.PetBondsActivateRequest_19000013.newBuilder() ; 
		
		builder.setId(1);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PetBondsActivateRequest_19000013Test instance = new PetBondsActivateRequest_19000013Test();
	    instance.start();
	}

}