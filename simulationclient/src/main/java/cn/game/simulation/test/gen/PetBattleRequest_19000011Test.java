package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PetBattleRequest_19000011Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.PetMsg.PetBattleRequest_19000011.Builder builder = cn.game.protocol.protobuf.PetMsg.PetBattleRequest_19000011.newBuilder() ; 
		
		builder.setId(640001);
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.PetMsg.PetBattleRequest_19000011.Builder builder = cn.game.protocol.protobuf.PetMsg.PetBattleRequest_19000011.newBuilder() ; 
		
		builder.setId(640001);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PetBattleRequest_19000011Test instance = new PetBattleRequest_19000011Test();
	    instance.start();
	}

}