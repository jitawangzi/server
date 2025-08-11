package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class applyJoinZongMenRequest_40000007Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.applyJoinZongMenRequest_40000007.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.applyJoinZongMenRequest_40000007.newBuilder() ; 
		builder.setId(894990001); 
		
		return builder.build() ; 
	}
	
	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ZongMenMsg.applyJoinZongMenRequest_40000007.Builder builder = cn.game.protocol.protobuf.ZongMenMsg.applyJoinZongMenRequest_40000007.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    applyJoinZongMenRequest_40000007Test instance = new applyJoinZongMenRequest_40000007Test();
	    instance.start();
	}

}