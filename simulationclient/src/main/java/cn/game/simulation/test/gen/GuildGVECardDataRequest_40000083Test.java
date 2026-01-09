package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildGVECardDataRequest_40000083Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildGVECardDataRequest_40000083.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildGVECardDataRequest_40000083.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildGVECardDataRequest_40000083.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildGVECardDataRequest_40000083.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildGVECardDataRequest_40000083Test instance = new GuildGVECardDataRequest_40000083Test();
	    instance.start();
	}

}