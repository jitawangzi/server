package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class getZongMenActivityRequest_40000033Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.getZongMenActivityRequest_40000033.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.getZongMenActivityRequest_40000033.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.getZongMenActivityRequest_40000033.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.getZongMenActivityRequest_40000033.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    getZongMenActivityRequest_40000033Test instance = new getZongMenActivityRequest_40000033Test();
	    instance.start();
	}

}