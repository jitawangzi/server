package cn.game.simulation.test.gen;

import com.google.protobuf.Message;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class HeroSkinRequest_16000060Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroSkinRequest_16000060.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroSkinRequest_16000060.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroSkinRequest_16000060.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroSkinRequest_16000060.newBuilder() ; 
		List<Integer> heroSkinIdsList = client.getPlayerAllInfo().getHeroSkinIdsList(); 
		if (heroSkinIdsList.isEmpty()) {
			return null; 
		}
		builder.setId(Rnd.randomElement(heroSkinIdsList));
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HeroSkinRequest_16000060Test instance = new HeroSkinRequest_16000060Test();
	    instance.start();
	}

}