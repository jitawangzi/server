package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class DevelopRescueLvUpRequest_25000007Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.DevelopRescueLvUpRequest_25000007.Builder builder = cn.game.protocol.protobuf.DevelopMsg.DevelopRescueLvUpRequest_25000007.newBuilder() ; 
		
		builder.setId(4001);
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.DevelopRescueLvUpRequest_25000007.Builder builder = cn.game.protocol.protobuf.DevelopMsg.DevelopRescueLvUpRequest_25000007.newBuilder() ; 
		
		builder.setId(4001);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    DevelopRescueLvUpRequest_25000007Test instance = new DevelopRescueLvUpRequest_25000007Test();
	    instance.start();
	}

}