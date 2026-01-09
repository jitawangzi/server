package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildLogRequest_40000025Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildLogRequest_40000025.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildLogRequest_40000025.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildLogRequest_40000025.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildLogRequest_40000025.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildLogRequest_40000025Test instance = new GuildLogRequest_40000025Test();
	    instance.start();
	}

}