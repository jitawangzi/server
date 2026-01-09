package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class BattlePVEVPRecordRequest_13000550Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattlePVEVPRecordRequest_13000550.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattlePVEVPRecordRequest_13000550.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.BattleMsg.BattlePVEVPRecordRequest_13000550.Builder builder = cn.game.protocol.protobuf.BattleMsg.BattlePVEVPRecordRequest_13000550.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    BattlePVEVPRecordRequest_13000550Test instance = new BattlePVEVPRecordRequest_13000550Test();
	    instance.start();
	}

}