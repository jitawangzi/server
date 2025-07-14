package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.BaseMsg.EquipInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class EquipDecomposeRequest_09000005Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipDecomposeRequest_09000005.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipDecomposeRequest_09000005.newBuilder() ; 
		
		List<EquipInfo> equipsList = client.getPlayerAllInfo().getEquipsList();
		EquipInfo randomOne = Rnd.randomElement(equipsList);
		if (randomOne == null) {
			return null;
		}
		builder.addUid(randomOne.getUid());
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipDecomposeRequest_09000005.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipDecomposeRequest_09000005.newBuilder() ; 
		
		List<EquipInfo> equipsList = client.getPlayerAllInfo().getEquipsList();
		EquipInfo randomOne = Rnd.randomElement(equipsList);
		if (randomOne == null) {
			return null;
		}
		builder.addUid(randomOne.getUid());
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    EquipDecomposeRequest_09000005Test instance = new EquipDecomposeRequest_09000005Test();
	    instance.start();
	}

}