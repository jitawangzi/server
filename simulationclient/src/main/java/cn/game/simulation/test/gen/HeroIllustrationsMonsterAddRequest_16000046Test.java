package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HeroIllustrationsMonsterAddRequest_16000046Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsMonsterAddRequest_16000046.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsMonsterAddRequest_16000046.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsMonsterAddRequest_16000046.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsMonsterAddRequest_16000046.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HeroIllustrationsMonsterAddRequest_16000046Test instance = new HeroIllustrationsMonsterAddRequest_16000046Test();
	    instance.start();
	}

}