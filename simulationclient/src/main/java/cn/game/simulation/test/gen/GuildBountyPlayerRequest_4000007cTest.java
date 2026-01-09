package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildBountyPlayerRequest_4000007cTest extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBountyPlayerRequest_4000007c.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBountyPlayerRequest_4000007c.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBountyPlayerRequest_4000007c.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBountyPlayerRequest_4000007c.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildBountyPlayerRequest_4000007cTest instance = new GuildBountyPlayerRequest_4000007cTest();
	    instance.start();
	}

}