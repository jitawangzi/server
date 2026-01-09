package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleMountainDataRequest_13000539Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleMountainDataRequest_13000539.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleMountainDataRequest_13000539.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleMountainDataRequest_13000539.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleMountainDataRequest_13000539.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleMountainDataRequest_13000539Test instance = new BattleMountainDataRequest_13000539Test();
	    instance.start();
	}

}