package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HCBattleDataRequest_13000102Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.HCBattleDataRequest_13000102.Builder builder = cn.game.protocol.protobuf.BattleMsg.HCBattleDataRequest_13000102.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.HCBattleDataRequest_13000102.Builder builder = cn.game.protocol.protobuf.BattleMsg.HCBattleDataRequest_13000102.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HCBattleDataRequest_13000102Test instance = new HCBattleDataRequest_13000102Test();
	    instance.start();
	}

}