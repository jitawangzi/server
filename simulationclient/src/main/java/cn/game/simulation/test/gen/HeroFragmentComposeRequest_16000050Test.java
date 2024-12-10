package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HeroFragmentComposeRequest_16000050Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HeroFragmentComposeRequest_16000050Test instance = new HeroFragmentComposeRequest_16000050Test();
	    instance.start();
	}

}