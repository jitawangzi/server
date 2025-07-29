package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleEquipTowerDataRequest_13000526Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerDataRequest_13000526.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerDataRequest_13000526.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerDataRequest_13000526.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleEquipTowerDataRequest_13000526.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleEquipTowerDataRequest_13000526Test instance = new BattleEquipTowerDataRequest_13000526Test();
	    instance.start();
	}

}