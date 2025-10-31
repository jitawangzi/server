package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class GuildCreateRequest_40000005Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildCreateRequest_40000005.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildCreateRequest_40000005.newBuilder() ; 
		
		builder.setName(client.getPlayerAllInfo().getPlayer().getName());
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildCreateRequest_40000005.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildCreateRequest_40000005.newBuilder() ; 
		builder.setName(client.getPlayerAllInfo().getPlayer().getName());
		return builder.build() ; 
		
	}
	
	public static void main(String args[]) throws Exception {
	    GuildCreateRequest_40000005Test instance = new GuildCreateRequest_40000005Test();
	    instance.start();
	}

}