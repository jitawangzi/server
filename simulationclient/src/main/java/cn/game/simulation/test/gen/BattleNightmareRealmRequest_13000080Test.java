package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleNightmareRealmRequest_13000080Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmRequest_13000080.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmRequest_13000080.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmRequest_13000080.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleNightmareRealmRequest_13000080.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleNightmareRealmRequest_13000080Test instance = new BattleNightmareRealmRequest_13000080Test();
	    instance.start();
	}

}