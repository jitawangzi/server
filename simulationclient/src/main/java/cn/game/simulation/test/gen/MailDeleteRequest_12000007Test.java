package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class MailDeleteRequest_12000007Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.MailMsg.MailDeleteRequest_12000007.Builder builder = cn.game.protocol.protobuf.MailMsg.MailDeleteRequest_12000007.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.MailMsg.MailDeleteRequest_12000007.Builder builder = cn.game.protocol.protobuf.MailMsg.MailDeleteRequest_12000007.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    MailDeleteRequest_12000007Test instance = new MailDeleteRequest_12000007Test();
	    instance.start();
	}

}