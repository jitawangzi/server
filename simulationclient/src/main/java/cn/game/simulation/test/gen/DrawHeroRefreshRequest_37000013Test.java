package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class DrawHeroRefreshRequest_37000013Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.DrawMsg.DrawHeroRefreshRequest_37000013.Builder builder = cn.game.protocol.protobuf.DrawMsg.DrawHeroRefreshRequest_37000013.newBuilder() ; 
		
		builder.setGold(50000);
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.DrawMsg.DrawHeroRefreshRequest_37000013.Builder builder = cn.game.protocol.protobuf.DrawMsg.DrawHeroRefreshRequest_37000013.newBuilder() ; 
		
		builder.setGold(50000);
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    DrawHeroRefreshRequest_37000013Test instance = new DrawHeroRefreshRequest_37000013Test();
	    instance.start();
	}

}