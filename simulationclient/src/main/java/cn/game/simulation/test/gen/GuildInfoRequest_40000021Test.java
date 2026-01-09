package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildInfoRequest_40000021Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildInfoRequest_40000021.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildInfoRequest_40000021.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildInfoRequest_40000021.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildInfoRequest_40000021.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildInfoRequest_40000021Test instance = new GuildInfoRequest_40000021Test();
	    instance.start();
	}

}