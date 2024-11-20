package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HeroUpLevelBatchRequest_16000023Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroUpLevelBatchRequest_16000023.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroUpLevelBatchRequest_16000023.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroUpLevelBatchRequest_16000023.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroUpLevelBatchRequest_16000023.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    HeroUpLevelBatchRequest_16000023Test instance = new HeroUpLevelBatchRequest_16000023Test();
	    instance.start();
	}

}