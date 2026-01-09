package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HCBattleSweepRequest_13000040Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.HCBattleSweepRequest_13000040.Builder builder = cn.game.protocol.protobuf.BattleMsg.HCBattleSweepRequest_13000040.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.HCBattleSweepRequest_13000040.Builder builder = cn.game.protocol.protobuf.BattleMsg.HCBattleSweepRequest_13000040.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HCBattleSweepRequest_13000040Test instance = new HCBattleSweepRequest_13000040Test();
	    instance.start();
	}

}