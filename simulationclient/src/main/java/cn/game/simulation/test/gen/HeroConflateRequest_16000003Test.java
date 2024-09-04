package cn.game.simulation.test.gen;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.protobuf.BaseMsg.HeroInfo;
import cn.game.protocol.protobuf.HeroMsg.HeroConflateInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class HeroConflateRequest_16000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroConflateRequest_16000003.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroConflateRequest_16000003.newBuilder() ; 

		PlayerAllInfo playerAllInfo = client.getPlayerAllInfo();
		List<HeroInfo> herosList = playerAllInfo.getHerosList();
		HeroConflateInfo.Builder heroConflateInfo = HeroConflateInfo.newBuilder();
		heroConflateInfo.setUid(Rnd.randomOne(herosList).getUid());
		
		builder.addHeroConflateInfo(heroConflateInfo);

		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception{
		
		ServerTestContext.init(); 
		
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId, ServerTestContext.version) ; 
		
		ServerTestContext.send(client, () -> new HeroConflateRequest_16000003Test().getMessage(client));

		
	}

}