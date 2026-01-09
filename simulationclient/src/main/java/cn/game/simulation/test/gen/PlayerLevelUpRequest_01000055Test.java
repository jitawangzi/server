package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.enume.Asset;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class PlayerLevelUpRequest_01000055Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerLevelUpRequest_01000055.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerLevelUpRequest_01000055.newBuilder() ; 
		builder.setId(Asset.CatalogPoints.ID);
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.PlayerMsg.PlayerLevelUpRequest_01000055.Builder builder = cn.game.protocol.protobuf.PlayerMsg.PlayerLevelUpRequest_01000055.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    PlayerLevelUpRequest_01000055Test instance = new PlayerLevelUpRequest_01000055Test();
	    instance.start();
	}

}