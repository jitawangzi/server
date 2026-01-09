package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HeroBattleUpLevelBatchRequest_16000025Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroBattleUpLevelBatchRequest_16000025.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroBattleUpLevelBatchRequest_16000025.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroBattleUpLevelBatchRequest_16000025.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroBattleUpLevelBatchRequest_16000025.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HeroBattleUpLevelBatchRequest_16000025Test instance = new HeroBattleUpLevelBatchRequest_16000025Test();
	    instance.start();
	}

}