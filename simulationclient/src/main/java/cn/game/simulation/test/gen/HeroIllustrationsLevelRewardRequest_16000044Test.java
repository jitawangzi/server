package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HeroIllustrationsLevelRewardRequest_16000044Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsLevelRewardRequest_16000044.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsLevelRewardRequest_16000044.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsLevelRewardRequest_16000044.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsLevelRewardRequest_16000044.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HeroIllustrationsLevelRewardRequest_16000044Test instance = new HeroIllustrationsLevelRewardRequest_16000044Test();
	    instance.start();
	}

}