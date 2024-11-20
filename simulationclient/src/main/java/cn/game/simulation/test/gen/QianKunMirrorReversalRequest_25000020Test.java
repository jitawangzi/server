package cn.game.simulation.test.gen;

import com.google.protobuf.Message;
import org.springframework.stereotype.Component;

import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class QianKunMirrorReversalRequest_25000020Test extends ServerTest{

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorReversalRequest_25000020.Builder builder = cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorReversalRequest_25000020.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	@Override
public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorReversalRequest_25000020.Builder builder = cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorReversalRequest_25000020.newBuilder() ; 
		
		
		
		return builder.build() ; 
	}
	
	public static void main(String args[]) throws Exception {
	    QianKunMirrorReversalRequest_25000020Test instance = new QianKunMirrorReversalRequest_25000020Test();
	    instance.start();
	}

}