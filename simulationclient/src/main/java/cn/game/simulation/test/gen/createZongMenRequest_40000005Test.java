package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class createZongMenRequest_40000005Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.createZongMenRequest_40000005.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.createZongMenRequest_40000005.newBuilder() ; 
		
		builder.setName("测试宗门08");
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.createZongMenRequest_40000005.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.createZongMenRequest_40000005.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    createZongMenRequest_40000005Test instance = new createZongMenRequest_40000005Test();
	    instance.start();
	}

}