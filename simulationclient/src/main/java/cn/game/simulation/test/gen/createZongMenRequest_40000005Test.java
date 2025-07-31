package cn.game.simulation.test.gen;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;
import cn.game.util.Rnd;

@Component
public class createZongMenRequest_40000005Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.createZongMenRequest_40000005.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.createZongMenRequest_40000005.newBuilder() ; 
		
		builder.setName("名字"+Rnd.nextInt(10000));
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.createZongMenRequest_40000005.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.createZongMenRequest_40000005.newBuilder() ; 
		
		builder.setName(Rnd.nextInt(1000000)+"");
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    createZongMenRequest_40000005Test instance = new createZongMenRequest_40000005Test();
	    instance.start();
	}

}