package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class EquipTeardownRequest_09000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipTeardownRequest_09000003.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipTeardownRequest_09000003.newBuilder() ;
		builder.setUid("284963592619446545");

		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipTeardownRequest_09000003.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipTeardownRequest_09000003.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    EquipTeardownRequest_09000003Test instance = new EquipTeardownRequest_09000003Test();
	    instance.start();
	}

}