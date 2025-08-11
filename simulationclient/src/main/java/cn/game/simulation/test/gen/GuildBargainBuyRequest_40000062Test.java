package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildBargainBuyRequest_40000062Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBargainBuyRequest_40000062.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBargainBuyRequest_40000062.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildBargainBuyRequest_40000062.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildBargainBuyRequest_40000062.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildBargainBuyRequest_40000062Test instance = new GuildBargainBuyRequest_40000062Test();
	    instance.start();
	}

}