package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildBountyAcceptRequest_40000070Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBountyAcceptRequest_40000070.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBountyAcceptRequest_40000070.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBountyAcceptRequest_40000070.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBountyAcceptRequest_40000070.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildBountyAcceptRequest_40000070Test instance = new GuildBountyAcceptRequest_40000070Test();
	    instance.start();
	}

}