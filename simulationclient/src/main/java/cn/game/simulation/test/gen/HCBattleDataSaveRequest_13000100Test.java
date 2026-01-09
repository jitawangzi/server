package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HCBattleDataSaveRequest_13000100Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.HCBattleDataSaveRequest_13000100.Builder builder = cn.game.protocol.protobuf.BattleMsg.HCBattleDataSaveRequest_13000100.newBuilder() ; 
		
		builder.setData("test");
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.HCBattleDataSaveRequest_13000100.Builder builder = cn.game.protocol.protobuf.BattleMsg.HCBattleDataSaveRequest_13000100.newBuilder() ; 
		
		builder.setData("test");
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HCBattleDataSaveRequest_13000100Test instance = new HCBattleDataSaveRequest_13000100Test();
	    instance.start();
	}

}