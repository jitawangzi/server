package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattleMountainGetRewardRequest_13000543Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleMountainGetRewardRequest_13000543.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleMountainGetRewardRequest_13000543.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattleMountainGetRewardRequest_13000543.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattleMountainGetRewardRequest_13000543.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattleMountainGetRewardRequest_13000543Test instance = new BattleMountainGetRewardRequest_13000543Test();
	    instance.start();
	}

}