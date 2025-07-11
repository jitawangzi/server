package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class EquipPartStrengthRequest_09000007Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipPartStrengthRequest_09000007.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipPartStrengthRequest_09000007.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipPartStrengthRequest_09000007.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipPartStrengthRequest_09000007.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    EquipPartStrengthRequest_09000007Test instance = new EquipPartStrengthRequest_09000007Test();
	    instance.start();
	}

}