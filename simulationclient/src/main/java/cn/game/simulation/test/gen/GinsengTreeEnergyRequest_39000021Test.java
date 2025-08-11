package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GinsengTreeEnergyRequest_39000021Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeEnergyRequest_39000021.Builder builder = cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeEnergyRequest_39000021.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeEnergyRequest_39000021.Builder builder = cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeEnergyRequest_39000021.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GinsengTreeEnergyRequest_39000021Test instance = new GinsengTreeEnergyRequest_39000021Test();
	    instance.start();
	}

}