package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattlePvPTargetListRequest_13000111Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattlePvPTargetListRequest_13000111.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattlePvPTargetListRequest_13000111.newBuilder() ; 
		
		builder.setRefreshFlag(RandomUtils.nextBoolean());
		builder.setUseCost(RandomUtils.nextBoolean());
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattlePvPTargetListRequest_13000111.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattlePvPTargetListRequest_13000111.newBuilder() ; 
		
		builder.setRefreshFlag(RandomUtils.nextBoolean());
		builder.setUseCost(RandomUtils.nextBoolean());
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattlePvPTargetListRequest_13000111Test instance = new BattlePvPTargetListRequest_13000111Test();
	    instance.start();
	}

}