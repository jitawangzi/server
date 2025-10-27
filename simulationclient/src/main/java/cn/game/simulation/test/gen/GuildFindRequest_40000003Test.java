package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.GuildMsg.GuildFindRequest_40000003;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class GuildFindRequest_40000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		GuildFindRequest_40000003.Builder builder = GuildFindRequest_40000003.newBuilder() ; 
		
		builder.setId(894990001);
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildFindRequest_40000003.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildFindRequest_40000003.newBuilder() ; 
		List<Integer> guildIds = client.guildIds; 
		if (guildIds.isEmpty()) {
			return null; 
		}
		builder.setId(Rnd.randomElement(guildIds)); 
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildFindRequest_40000003Test instance = new GuildFindRequest_40000003Test();
	    instance.start();
	}

}