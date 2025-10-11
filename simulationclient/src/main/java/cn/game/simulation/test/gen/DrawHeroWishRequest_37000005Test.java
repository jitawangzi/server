package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class DrawHeroWishRequest_37000005Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.DrawMsg.DrawHeroWishRequest_37000005.Builder builder = cn.game.protocol.protobuf.DrawMsg.DrawHeroWishRequest_37000005.newBuilder() ; 
		
		builder.setHeroId(331001);
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.DrawMsg.DrawHeroWishRequest_37000005.Builder builder = cn.game.protocol.protobuf.DrawMsg.DrawHeroWishRequest_37000005.newBuilder() ; 
		
		builder.setHeroId(331001);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    DrawHeroWishRequest_37000005Test instance = new DrawHeroWishRequest_37000005Test();
	    instance.start();
	}

}