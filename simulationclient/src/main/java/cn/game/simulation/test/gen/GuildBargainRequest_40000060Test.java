package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildBargainRequest_40000060Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBargainRequest_40000060.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBargainRequest_40000060.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBargainRequest_40000060.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBargainRequest_40000060.newBuilder() ; 
		if (client.guildAllInfo == null) {
			return null; 
		}
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildBargainRequest_40000060Test instance = new GuildBargainRequest_40000060Test();
	    instance.start();
	}

}