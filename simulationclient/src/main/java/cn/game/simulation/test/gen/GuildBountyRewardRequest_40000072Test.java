package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildBountyRewardRequest_40000072Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBountyRewardRequest_40000072.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBountyRewardRequest_40000072.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBountyRewardRequest_40000072.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBountyRewardRequest_40000072.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildBountyRewardRequest_40000072Test instance = new GuildBountyRewardRequest_40000072Test();
	    instance.start();
	}

}