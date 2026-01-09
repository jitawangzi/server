package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmMailListRequest_77000042Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmMailListRequest_77000042.Builder builder = cn.game.protocol.protobuf.GmMsg.GmMailListRequest_77000042.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmMailListRequest_77000042.Builder builder = cn.game.protocol.protobuf.GmMsg.GmMailListRequest_77000042.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmMailListRequest_77000042Test instance = new GmMailListRequest_77000042Test();
	    instance.start();
	}

}