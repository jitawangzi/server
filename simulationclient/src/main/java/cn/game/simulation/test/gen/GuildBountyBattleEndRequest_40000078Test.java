package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildBountyBattleEndRequest_40000078Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleEndRequest_40000078.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleEndRequest_40000078.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleEndRequest_40000078.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleEndRequest_40000078.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildBountyBattleEndRequest_40000078Test instance = new GuildBountyBattleEndRequest_40000078Test();
	    instance.start();
	}

}