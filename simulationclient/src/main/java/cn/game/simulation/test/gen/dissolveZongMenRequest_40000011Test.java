package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class dissolveZongMenRequest_40000011Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.dissolveZongMenRequest_40000011.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.dissolveZongMenRequest_40000011.newBuilder() ; 
		

		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.dissolveZongMenRequest_40000011.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.dissolveZongMenRequest_40000011.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    dissolveZongMenRequest_40000011Test instance = new dissolveZongMenRequest_40000011Test();
	    instance.start();
	}

}