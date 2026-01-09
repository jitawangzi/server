package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleLineupChooseRequest_13000031Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLineupChooseRequest_13000031.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLineupChooseRequest_13000031.newBuilder() ; 
		
		builder.setBattleType(1); 
		builder.setSeq(0); 
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLineupChooseRequest_13000031.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLineupChooseRequest_13000031.newBuilder() ; 
		builder.setBattleType(1); 
		builder.setSeq(0); 
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleLineupChooseRequest_13000031Test instance = new BattleLineupChooseRequest_13000031Test();
	    instance.start();
	}

}