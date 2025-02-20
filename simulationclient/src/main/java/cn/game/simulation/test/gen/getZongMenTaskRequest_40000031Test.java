package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class getZongMenTaskRequest_40000031Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.getZongMenTaskRequest_40000031.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.getZongMenTaskRequest_40000031.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.getZongMenTaskRequest_40000031.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.getZongMenTaskRequest_40000031.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    getZongMenTaskRequest_40000031Test instance = new getZongMenTaskRequest_40000031Test();
	    instance.start();
	}

}