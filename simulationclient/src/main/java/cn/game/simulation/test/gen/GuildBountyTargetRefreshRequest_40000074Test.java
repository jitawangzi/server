package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildBountyTargetRefreshRequest_40000074Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBountyTargetRefreshRequest_40000074.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBountyTargetRefreshRequest_40000074.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBountyTargetRefreshRequest_40000074.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBountyTargetRefreshRequest_40000074.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildBountyTargetRefreshRequest_40000074Test instance = new GuildBountyTargetRefreshRequest_40000074Test();
	    instance.start();
	}

}