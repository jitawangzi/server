package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleMountainBuffBagRequest_1300053bTest extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleMountainBuffBagRequest_1300053b.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleMountainBuffBagRequest_1300053b.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleMountainBuffBagRequest_1300053b.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleMountainBuffBagRequest_1300053b.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleMountainBuffBagRequest_1300053bTest instance = new BattleMountainBuffBagRequest_1300053bTest();
	    instance.start();
	}

}