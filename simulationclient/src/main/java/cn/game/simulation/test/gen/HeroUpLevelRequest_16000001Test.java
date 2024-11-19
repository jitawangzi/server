package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.BaseMsg.HeroInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class HeroUpLevelRequest_16000001Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroUpLevelRequest_16000001.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroUpLevelRequest_16000001.newBuilder() ; 
		List<HeroInfo> herosList = client.getPlayerAllInfo().getHerosList();
		
		builder.setUid(Rnd.randomOne(herosList).getUid());
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroUpLevelRequest_16000001.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroUpLevelRequest_16000001.newBuilder() ; 
		List<HeroInfo> herosList = client.getPlayerAllInfo().getHerosList();
		
		builder.setUid(Rnd.randomOne(herosList).getUid());
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new HeroUpLevelRequest_16000001Test().getMessage(client));

		
	}

}