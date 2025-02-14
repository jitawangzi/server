package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ZongMenBargainRequest_40000060Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenBargainRequest_40000060.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenBargainRequest_40000060.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.ZongMenBargainRequest_40000060.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.ZongMenBargainRequest_40000060.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    ZongMenBargainRequest_40000060Test instance = new ZongMenBargainRequest_40000060Test();
	    instance.start();
	}

}