package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ZongMenRankWorshipRequest_40000082Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenRankWorshipRequest_40000082.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenRankWorshipRequest_40000082.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenRankWorshipRequest_40000082.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenRankWorshipRequest_40000082.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ZongMenRankWorshipRequest_40000082Test instance = new ZongMenRankWorshipRequest_40000082Test();
	    instance.start();
	}

}