package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleNightmareRealmQuickRequest_13000084Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmQuickRequest_13000084.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmQuickRequest_13000084.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmQuickRequest_13000084.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmQuickRequest_13000084.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleNightmareRealmQuickRequest_13000084Test instance = new BattleNightmareRealmQuickRequest_13000084Test();
	    instance.start();
	}

}