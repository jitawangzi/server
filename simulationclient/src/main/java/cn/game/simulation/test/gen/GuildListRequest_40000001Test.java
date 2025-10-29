package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildListRequest_40000001Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildListRequest_40000001.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildListRequest_40000001.newBuilder() ; 

		builder.setPage(0);
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildListRequest_40000001.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildListRequest_40000001.newBuilder() ; 
		
		builder.setPage(1);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildListRequest_40000001Test instance = new GuildListRequest_40000001Test();
	    instance.start();
	}

}