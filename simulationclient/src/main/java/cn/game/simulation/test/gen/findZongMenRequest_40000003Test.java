package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class findZongMenRequest_40000003Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.findZongMenRequest_40000003.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.findZongMenRequest_40000003.newBuilder() ; 
		
		builder.setId(88800010008L);
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.findZongMenRequest_40000003.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.findZongMenRequest_40000003.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    findZongMenRequest_40000003Test instance = new findZongMenRequest_40000003Test();
	    instance.start();
	}

}