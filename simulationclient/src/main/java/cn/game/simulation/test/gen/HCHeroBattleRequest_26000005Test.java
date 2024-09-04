package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.BaseMsg.HCHeroInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HCHeroBattleRequest_26000005Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.HCHeroMsg.HCHeroBattleRequest_26000005.Builder builder = cn.game.protocol.protobuf.HCHeroMsg.HCHeroBattleRequest_26000005.newBuilder() ; 
		HCHeroInfo hcHeros = client.getPlayerAllInfo().getHcHeros(0);
		builder.setUid(hcHeros.getUid());
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new HCHeroBattleRequest_26000005Test().getMessage(client));

		
	}

}