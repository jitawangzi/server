package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class getZongMenLogRequest_40000025Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.getZongMenLogRequest_40000025.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.getZongMenLogRequest_40000025.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.getZongMenLogRequest_40000025.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.getZongMenLogRequest_40000025.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    getZongMenLogRequest_40000025Test instance = new getZongMenLogRequest_40000025Test();
	    instance.start();
	}

}