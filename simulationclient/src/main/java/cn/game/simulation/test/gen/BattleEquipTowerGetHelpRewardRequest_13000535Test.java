package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleEquipTowerGetHelpRewardRequest_13000535Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerGetHelpRewardRequest_13000535.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerGetHelpRewardRequest_13000535.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerGetHelpRewardRequest_13000535.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerGetHelpRewardRequest_13000535.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleEquipTowerGetHelpRewardRequest_13000535Test instance = new BattleEquipTowerGetHelpRewardRequest_13000535Test();
	    instance.start();
	}

}