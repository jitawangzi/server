package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class GinsengTreeHarvestRequest_39000013Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHarvestRequest_39000013.Builder builder = cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHarvestRequest_39000013.newBuilder() ; 
		
		builder.setPos(0);
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHarvestRequest_39000013.Builder builder = cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeHarvestRequest_39000013.newBuilder() ; 
		
		builder.setPos(Rnd.get(0, 3));
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GinsengTreeHarvestRequest_39000013Test instance = new GinsengTreeHarvestRequest_39000013Test();
	    instance.start();
	}

}