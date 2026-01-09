package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class DefenceLevelUpRequest_25000031Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.DefenceLevelUpRequest_25000031.Builder builder = cn.game.protocol.protobuf.DevelopMsg.DefenceLevelUpRequest_25000031.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.DefenceLevelUpRequest_25000031.Builder builder = cn.game.protocol.protobuf.DevelopMsg.DefenceLevelUpRequest_25000031.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    DefenceLevelUpRequest_25000031Test instance = new DefenceLevelUpRequest_25000031Test();
	    instance.start();
	}

}