package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class EquipWearRequest_09000001Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipWearRequest_09000001.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipWearRequest_09000001.newBuilder() ; 
		
		builder.setUid("284963592619446545");
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipWearRequest_09000001.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipWearRequest_09000001.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    EquipWearRequest_09000001Test instance = new EquipWearRequest_09000001Test();
	    instance.start();
	}

}