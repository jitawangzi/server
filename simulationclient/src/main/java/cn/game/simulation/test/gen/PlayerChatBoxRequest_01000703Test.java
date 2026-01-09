package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerChatBoxRequest_01000703Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerChatBoxRequest_01000703.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerChatBoxRequest_01000703.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerChatBoxRequest_01000703.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerChatBoxRequest_01000703.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PlayerChatBoxRequest_01000703Test instance = new PlayerChatBoxRequest_01000703Test();
	    instance.start();
	}

}