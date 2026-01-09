package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.BaseMsg.EquipInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class EquipWearRequest_09000001Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipWearRequest_09000001.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipWearRequest_09000001.newBuilder() ; 
		
		builder.setUid("301619302048487206");
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.EquipMsg.EquipWearRequest_09000001.Builder builder = cn.game.protocol.protobuf.EquipMsg.EquipWearRequest_09000001.newBuilder() ; 
		
		List<EquipInfo> equipsList = client.getPlayerAllInfo().getEquipsList();
		EquipInfo randomOne = Rnd.randomElement(equipsList);
		if (randomOne == null) {
			return null ; 
		}
		builder.setUid(randomOne.getUid());
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    EquipWearRequest_09000001Test instance = new EquipWearRequest_09000001Test();
	    instance.start();
	}

}