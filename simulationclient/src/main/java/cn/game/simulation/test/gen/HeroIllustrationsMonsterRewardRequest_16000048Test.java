package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HeroIllustrationsMonsterRewardRequest_16000048Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsMonsterRewardRequest_16000048.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsMonsterRewardRequest_16000048.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsMonsterRewardRequest_16000048.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsMonsterRewardRequest_16000048.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HeroIllustrationsMonsterRewardRequest_16000048Test instance = new HeroIllustrationsMonsterRewardRequest_16000048Test();
	    instance.start();
	}

}