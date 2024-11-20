package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleWorldBossInfoRequest_13000301Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleWorldBossInfoRequest_13000301.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleWorldBossInfoRequest_13000301.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleWorldBossInfoRequest_13000301.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleWorldBossInfoRequest_13000301.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleWorldBossInfoRequest_13000301Test instance = new BattleWorldBossInfoRequest_13000301Test();
	    instance.start();
	}

}