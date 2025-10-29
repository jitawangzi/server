package cn.game.simulation.test.gen;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.protobuf.BaseMsg.HeroInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HeroFragmentComposeRequest_16000050Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050.newBuilder() ; 
		
		builder.addHeroId(331001);
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050.newBuilder() ; 
		
		int heroId = 0 ; 
		Collection<HeroConfig> list = HeroManager.instance().list(); 
		
		for (HeroConfig heroConfig : list) {
			if (client.hasHero(heroConfig.ID)) {
				continue ;
			}
			if (!client.hasItem(heroConfig.Fragment, 10)) {
				continue; 
			}
			heroId = heroConfig.ID; 
			break; 
		}
		if (heroId == 0) {
			return null; 
		}
		builder.addHeroId(heroId); 
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HeroFragmentComposeRequest_16000050Test instance = new HeroFragmentComposeRequest_16000050Test();
	    instance.start();
	}

}