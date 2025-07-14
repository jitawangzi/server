package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class EquipDecomposeRequest_09000005Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipDecomposeRequest_09000005.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipDecomposeRequest_09000005.newBuilder() ; 
		
		builder.addUid("301619302048487206");
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipDecomposeRequest_09000005.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipDecomposeRequest_09000005.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    EquipDecomposeRequest_09000005Test instance = new EquipDecomposeRequest_09000005Test();
	    instance.start();
	}

}