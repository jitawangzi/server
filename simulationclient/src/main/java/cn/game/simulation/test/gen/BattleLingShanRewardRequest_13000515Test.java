package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleLingShanRewardRequest_13000515Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLingShanRewardRequest_13000515.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLingShanRewardRequest_13000515.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleLingShanRewardRequest_13000515.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleLingShanRewardRequest_13000515.newBuilder() ; 
		
		builder.setIndex(0); 
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleLingShanRewardRequest_13000515Test instance = new BattleLingShanRewardRequest_13000515Test();
	    instance.start();
	}

}