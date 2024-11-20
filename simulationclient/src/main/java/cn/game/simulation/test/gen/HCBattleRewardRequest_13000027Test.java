package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HCBattleRewardRequest_13000027Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.BattleMsg.HCBattleRewardRequest_13000027.Builder builder = cn.game.protocol.protobuf.BattleMsg.HCBattleRewardRequest_13000027.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.BattleMsg.HCBattleRewardRequest_13000027.Builder builder = cn.game.protocol.protobuf.BattleMsg.HCBattleRewardRequest_13000027.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HCBattleRewardRequest_13000027Test instance = new HCBattleRewardRequest_13000027Test();
	    instance.start();
	}

}