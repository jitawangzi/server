package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleStaminaRequest_13000050Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleStaminaRequest_13000050.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleStaminaRequest_13000050.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleStaminaRequest_13000050.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleStaminaRequest_13000050.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleStaminaRequest_13000050Test instance = new BattleStaminaRequest_13000050Test();
	    instance.start();
	}

}