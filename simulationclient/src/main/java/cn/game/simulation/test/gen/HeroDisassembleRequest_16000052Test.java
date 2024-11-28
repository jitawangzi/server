package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HeroDisassembleRequest_16000052Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroDisassembleRequest_16000052.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroDisassembleRequest_16000052.newBuilder() ; 
		builder.setUid("8316022495425885696");
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroDisassembleRequest_16000052.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroDisassembleRequest_16000052.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HeroDisassembleRequest_16000052Test instance = new HeroDisassembleRequest_16000052Test();
	    instance.start();
	}

}