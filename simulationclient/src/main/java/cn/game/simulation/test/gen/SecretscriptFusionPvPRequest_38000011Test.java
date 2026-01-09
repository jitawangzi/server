package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class SecretscriptFusionPvPRequest_38000011Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptFusionPvPRequest_38000011.Builder builder = cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptFusionPvPRequest_38000011.newBuilder() ; 
		builder.putSecretscriptMap(2, 3);
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptFusionPvPRequest_38000011.Builder builder = cn.game.protocol.protobuf.SecretscriptMsg.SecretscriptFusionPvPRequest_38000011.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    SecretscriptFusionPvPRequest_38000011Test instance = new SecretscriptFusionPvPRequest_38000011Test();
	    instance.start();
	}

}