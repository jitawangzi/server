package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HeroIllustrationsRewardRequest_16000042Test extends ServerTest{

	@Override
	public Message buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsRewardRequest_16000042.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsRewardRequest_16000042.newBuilder() ; 
		builder.setHeroId(333003);
		
		
		return builder.build() ; 
	}
	
	@Override
public Message tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsRewardRequest_16000042.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroIllustrationsRewardRequest_16000042.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HeroIllustrationsRewardRequest_16000042Test instance = new HeroIllustrationsRewardRequest_16000042Test();
	    instance.start();
	}

}