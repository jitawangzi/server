package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleEquipTowerGetTicketRequest_13000533Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerGetTicketRequest_13000533.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerGetTicketRequest_13000533.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerGetTicketRequest_13000533.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerGetTicketRequest_13000533.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleEquipTowerGetTicketRequest_13000533Test instance = new BattleEquipTowerGetTicketRequest_13000533Test();
	    instance.start();
	}

}