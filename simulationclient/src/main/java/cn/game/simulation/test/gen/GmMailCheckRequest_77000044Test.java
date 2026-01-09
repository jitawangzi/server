package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmMailCheckRequest_77000044Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmMailCheckRequest_77000044.Builder builder = cn.game.protocol.protobuf.GmMsg.GmMailCheckRequest_77000044.newBuilder() ; 
		
		builder.addUid("3");
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmMailCheckRequest_77000044.Builder builder = cn.game.protocol.protobuf.GmMsg.GmMailCheckRequest_77000044.newBuilder() ; 
		
		builder.addUid("3");
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmMailCheckRequest_77000044Test instance = new GmMailCheckRequest_77000044Test();
	    instance.start();
	}

}