package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildMsgRequest_41000045Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildCrossMsg.GuildMsgPush_41000045.Builder builder = cn.game.protocol.protobuf.GuildCrossMsg.GuildMsgPush_41000045.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GuildCrossMsg.GuildMsgPush_41000045.Builder builder = cn.game.protocol.protobuf.GuildCrossMsg.GuildMsgPush_41000045.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildMsgRequest_41000045Test instance = new GuildMsgRequest_41000045Test();
	    instance.start();
	}

}