package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class quitZongMenRequest_40000017Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.quitZongMenRequest_40000017.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.quitZongMenRequest_40000017.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.quitZongMenRequest_40000017.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.quitZongMenRequest_40000017.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    quitZongMenRequest_40000017Test instance = new quitZongMenRequest_40000017Test();
	    instance.start();
	}

}