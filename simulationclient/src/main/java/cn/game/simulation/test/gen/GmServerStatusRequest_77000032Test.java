package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GmServerStatusRequest_77000032Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmServerStatusRequest_77000032.Builder builder = cn.game.protocol.protobuf.GmMsg.GmServerStatusRequest_77000032.newBuilder() ; 
		builder.setServerId("game_test");
		builder.setStatus(2);
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GmMsg.GmServerStatusRequest_77000032.Builder builder = cn.game.protocol.protobuf.GmMsg.GmServerStatusRequest_77000032.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GmServerStatusRequest_77000032Test instance = new GmServerStatusRequest_77000032Test();
	    instance.start();
	}

}