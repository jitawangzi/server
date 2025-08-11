package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildDonateRequest_40000067Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildDonateRequest_40000067.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildDonateRequest_40000067.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildDonateRequest_40000067.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildDonateRequest_40000067.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildDonateRequest_40000067Test instance = new GuildDonateRequest_40000067Test();
	    instance.start();
	}

}