package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildQuitRequest_40000017Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildQuitRequest_40000017.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildQuitRequest_40000017.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildQuitRequest_40000017.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildQuitRequest_40000017.newBuilder() ; 
		
		if (client.guildAllInfo == null) {
			return null; 
		}
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildQuitRequest_40000017Test instance = new GuildQuitRequest_40000017Test();
	    instance.start();
	}

}