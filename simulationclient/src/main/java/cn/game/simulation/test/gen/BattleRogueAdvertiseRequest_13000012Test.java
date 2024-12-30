package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleRogueAdvertiseRequest_13000012Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleRogueAdvertiseRequest_13000012.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleRogueAdvertiseRequest_13000012.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleRogueAdvertiseRequest_13000012.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleRogueAdvertiseRequest_13000012.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleRogueAdvertiseRequest_13000012Test instance = new BattleRogueAdvertiseRequest_13000012Test();
	    instance.start();
	}

}