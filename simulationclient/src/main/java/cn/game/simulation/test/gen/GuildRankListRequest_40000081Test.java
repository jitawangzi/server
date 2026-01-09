package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class GuildRankListRequest_40000081Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildRankListRequest_40000081.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildRankListRequest_40000081.newBuilder() ; 
		builder.setPage(1); 
		builder.setPageSize(20); 
		return builder.build() ; 
	}
	
	@Override
	public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.GuildMsg.GuildRankListRequest_40000081.Builder builder = cn.game.protocol.protobuf.GuildMsg.GuildRankListRequest_40000081.newBuilder() ; 
		builder.setPage(1); 
		builder.setPageSize(20); 
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    GuildRankListRequest_40000081Test instance = new GuildRankListRequest_40000081Test();
	    instance.start();
	}

}