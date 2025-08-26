package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleEquipTowerHelpPlayerRequest_13000531Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerHelpPlayerRequest_13000531.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerHelpPlayerRequest_13000531.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerHelpPlayerRequest_13000531.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerHelpPlayerRequest_13000531.newBuilder() ;
		builder.setFloor(1);
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleEquipTowerHelpPlayerRequest_13000531Test instance = new BattleEquipTowerHelpPlayerRequest_13000531Test();
	    instance.start();
	}
}