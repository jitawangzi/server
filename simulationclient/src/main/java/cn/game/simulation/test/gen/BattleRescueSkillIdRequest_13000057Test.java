package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleRescueSkillIdRequest_13000057Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleRescueSkillIdRequest_13000057.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleRescueSkillIdRequest_13000057.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleRescueSkillIdRequest_13000057.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleRescueSkillIdRequest_13000057.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleRescueSkillIdRequest_13000057Test instance = new BattleRescueSkillIdRequest_13000057Test();
	    instance.start();
	}

}