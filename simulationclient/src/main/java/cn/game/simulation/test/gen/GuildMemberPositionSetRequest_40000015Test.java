package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildMemberPositionSetRequest_40000015Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildMemberPositionSetRequest_40000015.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildMemberPositionSetRequest_40000015.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildMemberPositionSetRequest_40000015.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildMemberPositionSetRequest_40000015.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildMemberPositionSetRequest_40000015Test instance = new GuildMemberPositionSetRequest_40000015Test();
	    instance.start();
	}

}