package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildGVEBuyTicketRequest_40000090Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildGVEBuyTicketRequest_40000090.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildGVEBuyTicketRequest_40000090.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildGVEBuyTicketRequest_40000090.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildGVEBuyTicketRequest_40000090.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildGVEBuyTicketRequest_40000090Test instance = new GuildGVEBuyTicketRequest_40000090Test();
	    instance.start();
	}

}