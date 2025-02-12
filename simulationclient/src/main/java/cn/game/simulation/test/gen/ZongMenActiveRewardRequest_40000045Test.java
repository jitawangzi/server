package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ZongMenActiveRewardRequest_40000045Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenActiveRewardRequest_40000045.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenActiveRewardRequest_40000045.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenActiveRewardRequest_40000045.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenActiveRewardRequest_40000045.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ZongMenActiveRewardRequest_40000045Test instance = new ZongMenActiveRewardRequest_40000045Test();
	    instance.start();
	}

}