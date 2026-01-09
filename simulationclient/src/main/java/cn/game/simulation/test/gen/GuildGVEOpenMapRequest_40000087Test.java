package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildGVEOpenMapRequest_40000087Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildGVEOpenMapRequest_40000087.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildGVEOpenMapRequest_40000087.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildGVEOpenMapRequest_40000087.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildGVEOpenMapRequest_40000087.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildGVEOpenMapRequest_40000087Test instance = new GuildGVEOpenMapRequest_40000087Test();
	    instance.start();
	}

}