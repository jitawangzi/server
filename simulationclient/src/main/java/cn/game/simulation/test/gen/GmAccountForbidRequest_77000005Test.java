package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmAccountForbidRequest_77000005Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmAccountForbidRequest_77000005.Builder builder = cn.game.protocol.protobuf.GmMsg.GmAccountForbidRequest_77000005.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmAccountForbidRequest_77000005.Builder builder = cn.game.protocol.protobuf.GmMsg.GmAccountForbidRequest_77000005.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmAccountForbidRequest_77000005Test instance = new GmAccountForbidRequest_77000005Test();
	    instance.start();
	}

}