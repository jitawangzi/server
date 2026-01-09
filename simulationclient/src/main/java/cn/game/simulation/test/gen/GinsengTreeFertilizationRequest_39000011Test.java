package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GinsengTreeFertilizationRequest_39000011Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeFertilizationRequest_39000011.Builder builder = cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeFertilizationRequest_39000011.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeFertilizationRequest_39000011.Builder builder = cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeFertilizationRequest_39000011.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GinsengTreeFertilizationRequest_39000011Test instance = new GinsengTreeFertilizationRequest_39000011Test();
	    instance.start();
	}

}