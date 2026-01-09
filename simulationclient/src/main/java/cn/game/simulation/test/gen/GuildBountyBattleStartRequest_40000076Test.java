package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildBountyBattleStartRequest_40000076Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleStartRequest_40000076.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleStartRequest_40000076.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleStartRequest_40000076.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBountyBattleStartRequest_40000076.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildBountyBattleStartRequest_40000076Test instance = new GuildBountyBattleStartRequest_40000076Test();
	    instance.start();
	}

}