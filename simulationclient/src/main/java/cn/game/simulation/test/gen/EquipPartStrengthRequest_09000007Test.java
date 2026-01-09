package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class EquipPartStrengthRequest_09000007Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipPartStrengthRequest_09000007.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipPartStrengthRequest_09000007.newBuilder() ; 
		
		builder.setType(Rnd.get(1, 6));
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipPartStrengthRequest_09000007.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipPartStrengthRequest_09000007.newBuilder() ; 
		
		builder.setType(Rnd.get(1, 6));
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    EquipPartStrengthRequest_09000007Test instance = new EquipPartStrengthRequest_09000007Test();
	    instance.start();
	}

}