package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleEquipTowerRecordRequest_13000528Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerRecordRequest_13000528.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerRecordRequest_13000528.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerRecordRequest_13000528.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerRecordRequest_13000528.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleEquipTowerRecordRequest_13000528Test instance = new BattleEquipTowerRecordRequest_13000528Test();
	    instance.start();
	}

}