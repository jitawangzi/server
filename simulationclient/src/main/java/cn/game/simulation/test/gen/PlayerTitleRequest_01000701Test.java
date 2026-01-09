package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerTitleRequest_01000701Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerTitleRequest_01000701.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerTitleRequest_01000701.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerTitleRequest_01000701.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerTitleRequest_01000701.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PlayerTitleRequest_01000701Test instance = new PlayerTitleRequest_01000701Test();
	    instance.start();
	}

}