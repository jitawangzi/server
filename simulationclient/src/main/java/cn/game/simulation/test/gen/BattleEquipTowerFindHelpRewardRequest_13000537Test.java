package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleEquipTowerFindHelpRewardRequest_13000537Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerFindHelpRewardRequest_13000537.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerFindHelpRewardRequest_13000537.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerFindHelpRewardRequest_13000537.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerFindHelpRewardRequest_13000537.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleEquipTowerFindHelpRewardRequest_13000537Test instance = new BattleEquipTowerFindHelpRewardRequest_13000537Test();
	    instance.start();
	}

}