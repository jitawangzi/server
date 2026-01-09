package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleMountainFinishNodeRequest_13000541Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleMountainFinishNodeRequest_13000541.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleMountainFinishNodeRequest_13000541.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleMountainFinishNodeRequest_13000541.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleMountainFinishNodeRequest_13000541.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleMountainFinishNodeRequest_13000541Test instance = new BattleMountainFinishNodeRequest_13000541Test();
	    instance.start();
	}

}