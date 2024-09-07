package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.enume.RankType;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class RankListRequest_35000001Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.RankMsg.RankListRequest_35000001.Builder builder = cn.game.protocol.protobuf.RankMsg.RankListRequest_35000001.newBuilder() ; 
		
		builder.setType(RankType.Battle.ID);
		builder.setPage(1);
		builder.setPageSize(30);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new RankListRequest_35000001Test().getMessage(client));

		
	}

}